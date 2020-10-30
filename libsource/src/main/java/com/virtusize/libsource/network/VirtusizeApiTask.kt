package com.virtusize.libsource.network

import android.os.Build
import com.virtusize.libsource.ErrorResponseHandler
import com.virtusize.libsource.SharedPreferencesHelper
import com.virtusize.libsource.SuccessResponseHandler
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.local.VirtusizeErrorType
import com.virtusize.libsource.data.local.virtusizeError
import com.virtusize.libsource.data.parsers.VirtusizeJsonParser
import com.virtusize.libsource.data.remote.ProductCheck
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

/**
 * The asynchronous task to make an API request in the background thread
 * @param urlConnection the HTTP URL connection that is used to make a single request
 */
internal class VirtusizeApiTask(private var urlConnection: HttpsURLConnection? = null) {

    companion object {
        // The read timeout to use for all the requests, which is 80 seconds
        private val READ_TIMEOUT = TimeUnit.SECONDS.toMillis(80).toInt()
        // The connection timeout to use for all the requests, which is 60 seconds
        private val CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(60).toInt()
        // The request header keys
        private const val HEADER_BROWSER_ID = "x-vs-bid"
        private const val HEADER_AUTH = "x-vs-auth"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        private const val HEADER_COOKIE = "Cookie"
    }

    // The helper to save data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper? = null
    // The Json parser interface for converting the JSON response to a given type of Java object
    private var jsonParser: VirtusizeJsonParser<Any>? = null
    // The callback for a successful API response
    private var successHandler: SuccessResponseHandler? = null
    // The error callback for a unsuccessful API response
    private var errorHandler: ErrorResponseHandler? = null

    /**
     * Sets up the JSON parser for converting the JSON response to a given type of Java object
     */
    fun setJsonParser(jsonParser: VirtusizeJsonParser<Any>?): VirtusizeApiTask {
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

    fun setSharedPreferencesHelper(sharedPreferencesHelper: SharedPreferencesHelper): VirtusizeApiTask {
        this.sharedPreferencesHelper = sharedPreferencesHelper
        return this
    }

    /**
     * Executes the API request and returns the response
     * @param apiRequest [ApiRequest]
     */
    fun execute(apiRequest: ApiRequest): VirtusizeApiResponse<Any?> {
        var urlConnection: HttpsURLConnection? = urlConnection
        var inputStream: InputStream? = null
        var errorStream: InputStream? = null
        try {
            if (urlConnection == null) {

                urlConnection = (URL(apiRequest.url).openConnection() as HttpsURLConnection).apply {
                    // Enable TLS 1.2 for the devices whose API is below 21 (pre-lollipop)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        sslSocketFactory = TLSSocketFactory()
                    }

                    readTimeout = READ_TIMEOUT
                    connectTimeout = CONNECT_TIMEOUT
                    requestMethod = apiRequest.method.name

                    sharedPreferencesHelper?.getBrowserId()?.let {
                        setRequestProperty(HEADER_BROWSER_ID, it)
                    }

                    // Set the access token in the header if the request needs authentication
                    if (apiRequest.authorization) {
                        sharedPreferencesHelper?.getAccessToken()?.let {
                            setRequestProperty(HEADER_AUTHORIZATION, "Token $it")
                        }
                    }

                    // Send the POST request
                    if (apiRequest.method == HttpMethod.POST) {
                        doOutput = true
                        setRequestProperty(HEADER_CONTENT_TYPE, "application/json")

                        // Set up the request header for the sessions API
                        if(apiRequest.url.contains(VirtusizeEndpoint.Sessions.getPath())) {
                            sharedPreferencesHelper?.getAuthToken()?.let {
                                setRequestProperty(HEADER_AUTH, it)
                                setRequestProperty(HEADER_COOKIE, "")
                            }
                        }

                        // Write the byte array of the request body to the output stream
                        if (apiRequest.params.isNotEmpty()) {
                            val outStream = DataOutputStream(outputStream)
                            outStream.write(JSONObject(apiRequest.params).toString().toByteArray())
                            outStream.close()
                        }
                    }
                }
            }

            when {
                // If the request was successful, then read the input stream and parse the response.
                urlConnection.isSuccessful() -> {
                    inputStream = urlConnection.inputStream
                    return VirtusizeApiResponse.Success(
                        parseInputStreamAsObject(
                            apiRequest.url,
                            inputStream
                        )
                    )
                }
                // If the request fails but it has a error response, then read the error stream and parse the response.
                urlConnection.errorStream != null -> {
                    errorStream = urlConnection.errorStream
                    val response = parseInputStreamAsObject(apiRequest.url, errorStream, true)
                    val error = when (urlConnection.responseCode) {
                        HttpURLConnection.HTTP_FORBIDDEN -> {
                            // If the API key is empty or invalid
                            VirtusizeErrorType.ApiKeyNullOrInvalid.virtusizeError()
                        }
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            // If the product cannot be found in the Virtusize Server
                            if (response is ProductCheck) {
                                errorHandler?.onError(VirtusizeErrorType.InvalidProduct.virtusizeError(response.name))
                                return VirtusizeApiResponse.Success(response)
                            }
                            virtusizeNetworkError(urlConnection, response)
                        }
                        else -> {
                            virtusizeNetworkError(urlConnection, response)
                        }
                    }
                    return VirtusizeApiResponse.Error(error)
                }
                else -> return VirtusizeApiResponse.Error(virtusizeNetworkError(urlConnection, urlConnection.responseMessage))
            }
        } catch (e: IOException) {
            return VirtusizeApiResponse.Error(virtusizeNetworkError(urlConnection, e.localizedMessage))
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
            errorStream?.close()
        }
    }

    /**
     * Asynchronously executes the API request and passes the response to callbacks
     * @param apiRequest [ApiRequest]
     * @param coroutineDispatcher the dispatcher that determines what thread the corresponding coroutine uses for its execution
     */
    fun executeAsync(apiRequest: ApiRequest, coroutineDispatcher: CoroutineDispatcher) {
        CoroutineScope(coroutineDispatcher).launch {
            val apiResponse = execute(apiRequest)
            if (apiResponse is VirtusizeApiResponse.Success) {
                withContext(Main) {
                    successHandler?.onSuccess(apiResponse.data)
                }
            } else {
                (apiResponse as? VirtusizeApiResponse.Error)?.error?.let {
                    withContext(Main) {
                        errorHandler?.onError(it)
                    }
                }
            }
        }
    }

    /**
     * Parses the contents of an InputStream
     * @param apiRequestUrl the API request URL
     * @param inputStream The input stream of bytes
     * @param isErrorStream pass true if it's parsing an error InputStream
     */
    private fun parseInputStreamAsObject(
        apiRequestUrl: String? = null,
        stream: InputStream,
        isErrorStream: Boolean = false
    ): Any? {
        var result: Any? = null
        readInputStreamAsString(stream)?.let { streamString ->
            jsonParser?.let { jsonParser ->
                try {
                    result =
                        if (apiRequestUrl != null && responseIsJsonArray(apiRequestUrl)) {
                            val productTypeJsonArray = JSONArray(streamString)
                            (0 until productTypeJsonArray.length())
                                .map { idx -> productTypeJsonArray.getJSONObject(idx) }
                                .mapNotNull { jsonParser.parse(it) }
                        } else {
                            val jsonObject = JSONObject(streamString)
                            jsonParser.parse(jsonObject)
                        }
                } catch (e: JSONException) {
                    errorHandler?.onError(VirtusizeErrorType.JsonParsingError.virtusizeError("JSONException: $e"))
                }
            }
            if(isErrorStream && result == null) {
                result = streamString
            }
        }
        return result
    }

    /**
     * Check if the response of the API request is a JSON array
     * @param apiRequestUrl The input stream of bytes
     */
    private fun responseIsJsonArray(apiRequestUrl: String): Boolean {
        return apiRequestUrl.contains(VirtusizeEndpoint.ProductType.getPath())
                || apiRequestUrl.contains(VirtusizeEndpoint.UserProducts.getPath())
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
    private fun virtusizeNetworkError(urlConnection: HttpURLConnection?, response: Any?): VirtusizeError {
        return VirtusizeError(
            VirtusizeErrorType.NetworkError,
            urlConnection?.responseCode,
            "${urlConnection?.url?.path} - ${response?.toString() ?: urlConnection?.responseMessage}"
        )
    }
}