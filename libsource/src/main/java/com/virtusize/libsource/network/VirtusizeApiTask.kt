package com.virtusize.libsource.network

import android.os.AsyncTask
import android.util.Log
import com.virtusize.libsource.CallbackHandler
import com.virtusize.libsource.Constants
import com.virtusize.libsource.ErrorHandler
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.remote.parsers.ProductCheckJsonParser
import com.virtusize.libsource.data.remote.parsers.ProductMetaDataHintsJsonParser
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * The asynchronous task to make an API request in the background thread
 */
internal class VirtusizeApiTask : AsyncTask<ApiRequest, Void, Any?>() {

    companion object {
        // The read timeout to use for all the requests, which is 80 seconds
        private val READ_TIMEOUT = TimeUnit.SECONDS.toMillis(80).toInt()
        // The connection timeout to use for all the requests, which is 60 seconds
        private val CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(60).toInt()
        // The request header keys
        private const val HEADER_BROWSER_ID = "x-vs-bid"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
    }

    // The Browser ID for the request header
    private var browserID: String? = null
    // The callback for the event
    private var callback: CallbackHandler? = null
    // The class for converting the JSON response to a Java object of type dataType
    private var dataType: Class<*>? = null
    // The optional error callback
    private var errorHandler: ErrorHandler? = null

    /**
     * Sets up the browser ID
     */
    fun setBrowserID(browserID: String?): VirtusizeApiTask {
        this.browserID = browserID
        return this
    }

    /**
     * Sets up the callback
     */
    fun setCallback(callback: CallbackHandler?): VirtusizeApiTask {
        this.callback = callback
        return this
    }

    /**
     * Sets up the data type for converting the JSON response
     */
    fun setDataType(dataType: Class<*>?): VirtusizeApiTask {
        this.dataType = dataType
        return this
    }

    /**
     * Sets up the error callback
     */
    fun setErrorHandler(errorHandler: ErrorHandler?): VirtusizeApiTask {
        this.errorHandler = errorHandler
        return this
    }

    override fun doInBackground(vararg apiRequests: ApiRequest): Any? {
        var result: Any? = null
        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            urlConnection = (URL(apiRequests[0].url).openConnection() as HttpURLConnection).apply {
                readTimeout = READ_TIMEOUT
                connectTimeout = CONNECT_TIMEOUT
                requestMethod = apiRequests[0].method.name

                // Send the POST request
                if(apiRequests[0].method == HttpMethod.POST) {
                    doOutput = true
                    browserID?.let {
                        setRequestProperty (HEADER_BROWSER_ID, browserID)
                    }
                    setRequestProperty(HEADER_CONTENT_TYPE, "application/json")

                    // Write the byte array of the request body to the output stream
                    if (apiRequests[0].params.isNotEmpty()) {
                        val outStream = DataOutputStream(outputStream)
                        outStream.write(JSONObject(apiRequests[0].params as Map<String, *>).toString().toByteArray())
                        outStream.close()
                    }
                }
            }

            // If the request was successful, then read the input stream and parse the response.
            if (urlConnection.isSuccessful()) {
                inputStream = urlConnection.inputStream
                readFromStream(inputStream)?.let { response ->
                    Log.d(Constants.LOG_TAG, response)
                    val jsonObject = JSONObject(response)
                    if(dataType != null) {
                        when(dataType) {
                            ProductCheck::class.java -> result = ProductCheckJsonParser().parse(jsonObject)
                            ProductMetaDataHintsJsonParser::class.java -> result = ProductMetaDataHintsJsonParser().parse(jsonObject)
                        }
                    }
                }
            } else {
                logHTTPConnectionError("Status code ${urlConnection.responseCode}, message: ${urlConnection.responseMessage}", null)
                errorHandler?.onError(VirtusizeError.NetworkError)
            }
        } catch (e: IOException) {
            logHTTPConnectionError(null, e)
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
            return result
        }
    }

    override fun onPostExecute(result: Any?) {
        result?.let {
            callback?.handleEvent(it)
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