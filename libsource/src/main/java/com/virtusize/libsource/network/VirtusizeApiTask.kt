package com.virtusize.libsource.network

import android.util.Log
import com.virtusize.libsource.ErrorResponseHandler
import com.virtusize.libsource.SuccessResponseHandler
import com.virtusize.libsource.Constants
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.remote.parsers.VirtusizeJsonParser
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONObject
import java.io.*
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
            var result: Any? = null
            var urlConnection: HttpURLConnection? = urlConnection
            var inputStream: InputStream? = null
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

                // If the request was successful, then read the input stream and parse the response.
                if (urlConnection.isSuccessful()) {
                    inputStream = urlConnection.inputStream
                    readFromStream(inputStream)?.let { response ->
                        Log.d(Constants.LOG_TAG, response)
                        jsonParser?.let {
                            val jsonObject = JSONObject(response)
                            result = it.parse(jsonObject)
                        }
                    }
                    withContext(Main) {
                        successHandler?.onSuccess(result)
                    }
                } else {
                    logHTTPConnectionError("Status code ${urlConnection.responseCode}, message: ${urlConnection.responseMessage}", null)
                    withContext(Main) {
                        errorHandler?.onError(urlConnection.responseCode, urlConnection.responseMessage, VirtusizeError.NetworkError)
                    }
                }
            } catch (e: IOException) {
                logHTTPConnectionError(null, e)
            } finally {
                urlConnection?.disconnect()
                inputStream?.close()
            }
        }
    }

    /**
     * Returns the contents of an InputStream as a String.
     * @param inputStream The input stream of bytes
     */
    private fun readFromStream(inputStream: InputStream): String? {
        val scanner: Scanner = Scanner(inputStream).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else null
    }

    /**
     * Handles errors received when performing network requests
     * It Logs error.
     * @param message the error message
     * @param exception the error Exception that got thrown
     */
    private fun logHTTPConnectionError(message: String?, exception: Exception?) {
        Log.e(Constants.LOG_TAG, "Virtusize API request failed. " + (message ?: "") + if (exception != null) "Exception: $exception" else "")
    }

}