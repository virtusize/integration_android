package com.virtusize.libsource.network

import android.util.Log
import com.virtusize.libsource.Constants
import com.virtusize.libsource.ErrorResponseHandler
import com.virtusize.libsource.SuccessResponseHandler
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.local.VirtusizeErrorType
import com.virtusize.libsource.data.local.message
import com.virtusize.libsource.data.local.virtusizeError
import com.virtusize.libsource.data.parsers.VirtusizeJsonParser
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.remote.Store
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * The asynchronous task to make an API request in the background thread
 */
internal class VirtusizeApiTask {


    companion object {
        // The read timeout to use for all the requests, which is 80 seconds
        private val READ_TIMEOUT = TimeUnit.SECONDS.toMillis(80).toInt()
        // The connection timeout to use for all the requests, which is 60 seconds
        private val CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(60).toInt()
        // The request header keys
        private const val HEADER_BROWSER_ID = "x-vs-bid"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
    }

    // The dispatcher that determines what thread the corresponding coroutine uses for its execution
    private var coroutineDispatcher: CoroutineDispatcher = IO
    // The HTTP URL connection that is used to make a single request
    private var urlConnection: HttpURLConnection? = null
    // The Browser ID for the request header
    private var browserID: String? = null
    // The Json parser interface for converting the JSON response to a given type of Java object
    private var jsonParser: VirtusizeJsonParser? = null
    // The callback for a successful API response
    private var successHandler: SuccessResponseHandler? = null
    // The error callback for a unsuccessful API response
    private var errorHandler: ErrorResponseHandler? = null

    /**
     * Sets up the browser ID
     */
    fun setBrowserID(browserID: String?): VirtusizeApiTask {
        this.browserID = browserID
        return this
    }

    /**
     * Sets up the JSON parser for converting the JSON response to a given type of Java object
     */
    fun setJsonParser(jsonParser: VirtusizeJsonParser?): VirtusizeApiTask {
        this.jsonParser = jsonParser
        return this
    }

    /**
     * Sets up the error callback
     */
    fun setErrorHandler(errorHandler: ErrorResponseHandler?): VirtusizeApiTask {
        this.errorHandler = errorHandler
        return this
    }

    /**
     * Sets up the success callback
     */
    fun setSuccessHandler(successHandler: SuccessResponseHandler?): VirtusizeApiTask {
        this.successHandler = successHandler
        return this
    }

    /**
     * Sets up the HTTP URL connection
     */
    fun setHttpURLConnection(urlConnection: HttpURLConnection?): VirtusizeApiTask {
        this.urlConnection = urlConnection
        return this
    }

    /**
     * Sets up the Coroutine dispatcher
     */
    fun setCoroutineDispatcher(dispatcher: CoroutineDispatcher): VirtusizeApiTask {
        this.coroutineDispatcher = dispatcher
        return this
    }

    fun execute(apiRequest: ApiRequest) {
        CoroutineScope(coroutineDispatcher).launch {
            var urlConnection: HttpURLConnection? = urlConnection
            var inputStream: InputStream? = null
            var errorStream: InputStream? = null
            try {
                if (urlConnection == null) {
                    urlConnection = (URL(apiRequest.url).openConnection() as HttpURLConnection).apply {
                            readTimeout = READ_TIMEOUT
                            connectTimeout = CONNECT_TIMEOUT
                            requestMethod = apiRequest.method.name

                            // Send the POST request
                            if (apiRequest.method == HttpMethod.POST) {
                                doOutput = true
                                browserID?.let {
                                    setRequestProperty(HEADER_BROWSER_ID, browserID)
                                }
                                setRequestProperty(HEADER_CONTENT_TYPE, "application/json")

                                // Write the byte array of the request body to the output stream
                                if (apiRequest.params.isNotEmpty()) {
                                    val outStream = DataOutputStream(outputStream)
                                    outStream.write(JSONObject(apiRequest.params as Map<String, *>).toString().toByteArray())
                                    outStream.close()
                                }
                            }
                        }
                }

                when {
                    // If the request was successful, then read the input stream and parse the response.
                    urlConnection.isSuccessful() -> {
                        inputStream = urlConnection.inputStream
                        val result = parseURLConnectionStream(inputStream)
                        withContext(Main) {
                            successHandler?.onSuccess(result)
                        }
                    }
                    // If the request was failed but it has a error response, then read the error stream and parse the response.
                    urlConnection.errorStream != null -> {
                        errorStream = urlConnection.errorStream
                        val response = parseURLConnectionStream(errorStream)
                        val error = when (urlConnection.responseCode) {
                            HttpURLConnection.HTTP_FORBIDDEN -> {
                                // If the API key is empty or invalid
                                VirtusizeErrorType.ApiKeyNullOrInvalid.virtusizeError()
                            }
                            HttpURLConnection.HTTP_NOT_FOUND -> {
                                // If the product cannot be found in the Virtusize Server
                                if (response is ProductCheck) {
                                    Log.d(
                                        Constants.LOG_TAG,
                                        VirtusizeErrorType.InvalidProduct.message(response.productId)
                                    )
                                    withContext(Main) {
                                        successHandler?.onSuccess(response)
                                        return@withContext
                                    }
                                }
                                virtusizeNetworkError(urlConnection, response)
                            }
                            else -> {
                                virtusizeNetworkError(urlConnection, response)
                            }
                        }
                        withContext(Main) {
                            errorHandler?.onError(error)
                        }
                    }
                    else -> {
                        withContext(Main) {
                            errorHandler?.onError(virtusizeNetworkError(urlConnection, urlConnection.responseMessage))
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(Constants.LOG_TAG, "Virtusize API task failed. Error: $e")
            } finally {
                urlConnection?.disconnect()
                inputStream?.close()
                errorStream?.close()
            }
        }
    }

    /**
     * Parses the contents of an InputStream
     * @param inputStream The input stream of bytes
     */
    private fun parseURLConnectionStream(stream: InputStream): Any? {
        var result: Any? = null
        readInputStreamAsString(stream)?.let { streamString ->
            jsonParser?.let {
                try {
                    val jsonObject = JSONObject(streamString)
                    result = it.parse(jsonObject)
                } catch (e: JSONException) {
                    Log.d(Constants.LOG_TAG, "JSONException: $e")
                }
            }
            if(result == null) {
                result = streamString
            }
        }
        return result
    }

    /**
     * Returns the contents of an InputStream as a String.
     * @param inputStream The input stream of bytes
     */
    private fun readInputStreamAsString(inputStream: InputStream): String? {
        val scanner: Scanner = Scanner(inputStream).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else null
    }

    /**
     * Returns the VirtusizeError that is associated with an API error
     * @param urlConnection The HTTP URL connection
     * @param response The response from the API request
     */
    private fun virtusizeNetworkError(urlConnection: HttpURLConnection, response: Any?): VirtusizeError {
        return VirtusizeError(VirtusizeErrorType.NetworkError, urlConnection.responseCode, "Virtusize API error: ${urlConnection.url.path} - ${response?.toString() ?: urlConnection.responseMessage}")
    }
}