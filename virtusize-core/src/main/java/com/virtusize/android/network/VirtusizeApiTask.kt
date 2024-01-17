package com.virtusize.android.network

import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.virtusizeError
import com.virtusize.android.data.parsers.VirtusizeJsonParser
import com.virtusize.android.data.remote.ProductCheck
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

/**
 * The asynchronous task to make an API request in the background thread
 * @param urlConnection the HTTP URL connection that is used to make a single request
 * @param sharedPreferencesHelper the helper to store data locally using Shared Preferences
 * @param messageHandler pass VirtusizeMessageHandler to listen to any Virtusize-related messages
 */
class VirtusizeApiTask(
    private var urlConnection: HttpsURLConnection?,
    private var sharedPreferencesHelper: SharedPreferencesHelper,
    private var messageHandler: VirtusizeMessageHandler?
) {

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

    // The Json parser interface for converting the JSON response to a given type of Java object
    private var jsonParser: VirtusizeJsonParser<Any>? = null

    /**
     * Sets up the JSON parser for converting the JSON response to a given type of Java object
     * @return the [VirtusizeApiTask] with the JSON parser set up
     */
    fun setJsonParser(jsonParser: VirtusizeJsonParser<Any>?): VirtusizeApiTask {
        this.jsonParser = jsonParser
        return this
    }

    /**
     * Executes the API request and returns the response
     * @param apiRequest [ApiRequest]
     * @return VirtusizeApiResponse of the generic type T
     */
    fun <T> execute(apiRequest: ApiRequest): VirtusizeApiResponse<T> {
        var urlConnection: HttpsURLConnection? = urlConnection
        var inputStream: InputStream? = null
        var errorStream: InputStream? = null
        try {
            if (urlConnection == null) {
                val url = URL(apiRequest.url + getQueryString(apiRequest))
                urlConnection = (url.openConnection() as HttpsURLConnection).apply {
                    readTimeout = READ_TIMEOUT
                    connectTimeout = CONNECT_TIMEOUT
                    requestMethod = apiRequest.method.name

                    setRequestProperty(
                        HEADER_BROWSER_ID,
                        sharedPreferencesHelper.getBrowserId()
                    )

                    // Set the access token in the header if the request needs authentication
                    if (apiRequest.authorization) {
                        setRequestProperty(
                            HEADER_AUTHORIZATION,
                            "Token ${sharedPreferencesHelper.getAccessToken()}"
                        )
                    }

                    // Send the POST request
                    if (apiRequest.method == HttpMethod.POST) {
                        doOutput = true
                        setRequestProperty(HEADER_CONTENT_TYPE, "application/json")

                        // Set up the request header for the sessions API
                        if (apiRequest.url.contains(VirtusizeEndpoint.Sessions.getPath())) {
                            sharedPreferencesHelper.getAuthToken()?.let {
                                setRequestProperty(HEADER_AUTH, it)
                                setRequestProperty(HEADER_COOKIE, "")
                            }
                        }

                        // Write the byte array of the request body to the output stream
                        if (apiRequest.params.isNotEmpty()) {
                            val outStream = DataOutputStream(outputStream)
                            outStream.write(
                                JSONObject(apiRequest.params).toString().toByteArray()
                            )
                            outStream.close()
                        }
                    }
                }
            }

            when {
                // If the request was successful, then read the input stream and parse the response.
                urlConnection.isSuccessful() -> {
                    inputStream = urlConnection.inputStream
                    return try {
                        val inputStreamString = readInputStreamAsString(inputStream)
                        val response = parseInputStreamStringToObject(
                            apiRequest.url,
                            inputStreamString
                        )
                        VirtusizeApiResponse.Success(response) as VirtusizeApiResponse<T>
                    } catch (e: JSONException) {
                        VirtusizeApiResponse.Error(
                            VirtusizeErrorType.JsonParsingError.virtusizeError(
                                extraMessage = "${apiRequest.url} ${e.localizedMessage}"
                            )
                        )
                    }
                }
                // If the request fails but it has a error response, then read the error stream and parse the response.
                urlConnection.errorStream != null -> {
                    errorStream = urlConnection.errorStream
                    val errorStreamString = readInputStreamAsString(errorStream)
                    val response = parseErrorStreamStringToObject(errorStreamString)
                    val error = when (urlConnection.responseCode) {
                        HttpURLConnection.HTTP_FORBIDDEN -> {
                            // If the API key is empty or invalid
                            VirtusizeErrorType.ApiKeyNullOrInvalid.virtusizeError()
                        }

                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            // If the product cannot be found in the Virtusize Server
                            if (response is ProductCheck) {
                                return VirtusizeApiResponse.Error(
                                    VirtusizeErrorType.UnParsedProduct.virtusizeError(
                                        extraMessage = response.productId
                                    )
                                )
                            }
                            VirtusizeErrorType.APIError.virtusizeError(
                                urlConnection.responseCode,
                                getAPIErrorMessage(
                                    urlConnection.url?.path,
                                    response ?: urlConnection.responseMessage
                                )
                            )
                        }

                        else -> {
                            VirtusizeErrorType.APIError.virtusizeError(
                                urlConnection.responseCode,
                                getAPIErrorMessage(
                                    urlConnection.url?.path,
                                    response ?: urlConnection.responseMessage
                                )
                            )
                        }
                    }
                    return VirtusizeApiResponse.Error(error)
                }

                else -> return VirtusizeApiResponse.Error(
                    VirtusizeErrorType.APIError.virtusizeError(
                        urlConnection.responseCode,
                        getAPIErrorMessage(
                            urlConnection.url?.path,
                            urlConnection.responseMessage
                        )
                    )
                )
            }
        } catch (e: IOException) {
            return VirtusizeApiResponse.Error(
                VirtusizeErrorType.APIError.virtusizeError(
                    extraMessage = getAPIErrorMessage(
                        urlConnection?.url?.path,
                        e.localizedMessage
                    )
                )
            )
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
            errorStream?.close()
        }
    }

    /**
     * Gets the url query string from the API request object
     * @param apiRequest the API request
     * @return a query string that will be concatenated at the end of the base API URL
     */
    private fun getQueryString(apiRequest: ApiRequest): String {
        var queryString = ""
        if (apiRequest.method == HttpMethod.GET) {
            if (apiRequest.params.isNotEmpty()) {
                queryString += "?" + apiRequest.params
                    .map { paramMapEntry -> "${paramMapEntry.key}=${paramMapEntry.value}" }
                    .joinToString("&")
            }
        }
        return queryString
    }

    /**
     * Parses the string of the input stream to a data object
     * @param apiRequestUrl the API request URL
     * @param inputStreamString the string of the input stream
     * @return either the object that contains the content of the string of the input stream or null
     */
    private fun parseInputStreamStringToObject(
        apiRequestUrl: String? = null,
        inputStreamString: String? = null
    ): Any? {
        var result: Any? = null
        if (inputStreamString != null) {
            try {
                result = parseStringToObject(apiRequestUrl, inputStreamString)
            } catch (e: JSONException) {
                messageHandler?.onError(
                    VirtusizeErrorType.JsonParsingError.virtusizeError(
                        extraMessage = e.localizedMessage
                    )
                )
            }
        }
        return result
    }

    /**
     * Parses the string of the error stream to a data object
     * @param errorStreamString the string of the error stream
     * @return either an object that contains the content of the string of the input stream, the string of the error stream, or null
     */
    private fun parseErrorStreamStringToObject(
        errorStreamString: String? = null
    ): Any? {
        var result: Any? = null
        if (errorStreamString != null) {
            result = try {
                parseStringToObject(streamString = errorStreamString) ?: errorStreamString
            } catch (e: JSONException) {
                errorStreamString
            }
        }
        return result
    }

    /**
     * Parses the string of an input stream to an object
     * @param apiRequestUrl the API request URL
     * @param streamString the string of the input stream
     * @return either the data object that is converted from streamString or null
     */
    private fun parseStringToObject(
        apiRequestUrl: String? = null,
        streamString: String
    ): Any? {
        var result: Any? = null
        jsonParser?.let { jsonParser ->
            result = if (apiRequestUrl != null && responseIsJsonArray(apiRequestUrl)) {
                val jsonArray = JSONArray(streamString)
                (0 until jsonArray.length())
                    .map { idx -> jsonArray.getJSONObject(idx) }
                    .mapNotNull { jsonParser.parse(it) }
            } else {
                val jsonObject = JSONObject(streamString)
                jsonParser.parse(jsonObject)
            }
        }
        return result
    }

    /**
     * Check if the response of the API request is a JSON array
     * @param apiRequestUrl The input stream of bytes
     * @return the boolean value to tell whether the response of the apiRequestUrl is a JSON array.
     */
    private fun responseIsJsonArray(apiRequestUrl: String): Boolean {
        return apiRequestUrl.contains(VirtusizeEndpoint.ProductType.getPath()) || apiRequestUrl.contains(
            VirtusizeEndpoint.UserProducts.getPath()
        ) ||
                apiRequestUrl.contains(VirtusizeEndpoint.GetSize.getPath())
    }

    /**
     * Returns the contents of an [InputStream] as a String.
     * @param inputStream The input stream of bytes
     * @return the string from scanning through the inputStream
     */
    private fun readInputStreamAsString(inputStream: InputStream): String? {
        val scanner: Scanner = Scanner(inputStream).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else null
    }

    /**
     * Gets the API error message based on the path part of the request url
     * @param requestPath the path part of the request URL
     * @param response the response from an API request
     * @return the message with the info of the request's path and response
     */
    private fun getAPIErrorMessage(requestPath: String?, response: Any?): String {
        return "$requestPath - ${response?.toString()}"
    }
}
