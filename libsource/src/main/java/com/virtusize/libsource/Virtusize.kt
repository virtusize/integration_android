package com.virtusize.libsource

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.virtusize.libsource.data.VirtusizeApi
import com.virtusize.libsource.data.pojo.ProductCheckResponse
import com.virtusize.libsource.data.pojo.ProductMetaDataHintsResponse
import com.virtusize.libsource.model.*
import com.virtusize.libsource.ui.FitIllustratorButton
import org.json.JSONObject
import java.util.*

/**
 * This is the main class that can be used by Virtusize Clients to perform all available operations related to fit check
 */
class Virtusize
    (
    userId: Int?, // User Id userId corresponding to the app user
    apiKey: String, // API Key unique to every Virtusize Client
    env: VirtusizeEnvironment, // Virtusize environment
    private val context: Context) // Application Context
{

    /**
     * BrowserIdentifier contains the browser identifier for current application
     */
    private var browserIdentifier: BrowserIdentifier = BrowserIdentifier( sharedPrefs =
        context.getSharedPreferences(
            "VIRTUSIZE_SHARED_PREFS",
            MODE_PRIVATE
        )
    )

    /**
     * Device language
     */
    private val language = Locale.getDefault().displayLanguage
    /**
     * Device screen resolution
     */
    private lateinit var resolution: String

    init {
        /**
         * Virtusize API for building API requests
         */
        VirtusizeApi.init(env = env, key = apiKey, browserID = browserIdentifier.getBrowserId(), userId = userId?.toString()?:"", language = language)
    }

    /**
     * Volley request queue
     */
    private val requestQueue = Volley.newRequestQueue(context)
    /**
     * GSON for JSON to Kotlin serialization
     */
    private val gson = GsonBuilder().create()

    /**
     * This method is used to perform API requests.
     * Needs Internet permission
     * Makes network call
     * @param url Request URL
     * @param callback Completion handler which is used to send data back on request's successful completion
     * @param method API Request type
     * @param dataType Class for converting JSON response to Java object of type dataType
     * @param params Map of parameters to be sent as request body
     */
    private fun perform(url: String, callback: CallbackHandler?, method: Int, dataType: Class<*>?, params: MutableMap<String, String> = mutableMapOf()) {
        if (method == Request.Method.GET) {
            val stringRequest = StringRequest(
                method, url,
                Response.Listener<String> { response ->
                    if (response != null && dataType != null) {
                        val result = gson.fromJson(response, dataType)
                        callback?.handleEvent(result)
                    }
                },
                Response.ErrorListener { error ->
                    if (error != null)
                        handleVolleyError(error)
                })

            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest)
        }
        else {
            val jsonBody = JSONObject()

            for ((key, value) in  params) {
                jsonBody.put(key, value)
            }
            val jsonObjectRequest = JsonObjectRequest(method, url, jsonBody,
                Response.Listener { response ->
                    Log.d(Constants.LOG_TAG, response.toString())
                },
                Response.ErrorListener {error ->
                if (error != null)
                    handleVolleyError(error)
                })
            requestQueue.add(jsonObjectRequest)
        }

    }

    /**
     * This method is used to handle errors received when performing network requests using Volley
     * It Logs error and shows error toast to user
     */
    private fun handleVolleyError(err: VolleyError) {
        Log.e(Constants.LOG_TAG, err.toString() + err.localizedMessage)
        Toast.makeText(context, "Oops! Something went wrong! $err", Toast.LENGTH_LONG).show()
    }

    /**
     * This method is used to setup fit button by setting VirtusizeProduct to the button
     * @param fitIllustratorButton FitIllustratorButton that is being set up
     * @param virtusizeProduct VirtusizeProduct that is being set to button
     * @throws IllegalArgumentException if FitIllustratorButton is null or VirtusizeProduct image URL is invalid
     */
    fun setupFitButton(fitIllustratorButton: FitIllustratorButton?,
                       virtusizeProduct: VirtusizeProduct) {

        // Throws VirtusizeError.NullFitButtonError error if button is null
        if (fitIllustratorButton == null) {
            throwError(error = VirtusizeError.NullFitButtonError)
            return
        }
        // Set virtusizeProduct to fitIllustratorButton
        fitIllustratorButton.setup(product = virtusizeProduct)
        // API Request to perform Product check on Virtusize server
        val apiRequest = VirtusizeApi.productCheck(product = virtusizeProduct)
        // Callback Handler for Product Check request
        val productValidCheckListener = object : ValidProductCheckHandler {

            /**
             * This method returns ProductCheckResponse from Virtusize when Product check Request is performed on server on Virtusize server
             */
            override fun onValidProductCheckCompleted(productCheckResponse: ProductCheckResponse) {
                // Set up Product check response data to VirtusizeProduct in FitIllustratorButton
                fitIllustratorButton.setupProductCheckResponseData(productCheckResponse)
                // Send API Event UserSawProduct
                sendEventToApi(event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()), withDataProduct = productCheckResponse)
                if (productCheckResponse.data.validProduct) {
                    if (productCheckResponse.data.fetchMetaData) {
                        if (fitIllustratorButton.virtusizeProduct?.imageUrl != null) {
                            // If image URL is valid, send image URL to server
                            sendProductImageToBackend(product = fitIllustratorButton.virtusizeProduct!!)
                        }
                        else
                            throwError(VirtusizeError.ImageUrlNotValid)
                    }
                    // Send API Event UserSawWidgetButton
                    sendEventToApi(event = VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()), withDataProduct = productCheckResponse)
                }
            }
        }

        // Perform network request for Product Check
        perform(
            url = apiRequest.url,
            callback = productValidCheckListener,
            method = apiRequest.method,
            dataType = ProductCheckResponse::class.java
        )
    }

    /**
     * This method is used to send VirtusizeProduct's image URL to server
     * @param product VirtusizeProduct
     * @see VirtusizeProduct
     */
    private fun sendProductImageToBackend(product: VirtusizeProduct) {
        val apiRequest = VirtusizeApi.sendProductImageToBackend(product = product)
        perform(
            url = apiRequest.url,
            callback = null,
            method = apiRequest.method,
            dataType = ProductMetaDataHintsResponse::class.java,
            params = apiRequest.params
        )
    }

    /**
     * This method is used to send Event to Virtusize server
     * @param event VirtusizeEvent
     * @param withDataProduct ProductCheckResponse corresponding to VirtusizeProduct
     */
    private fun sendEventToApi(event: VirtusizeEvent, withDataProduct: ProductCheckResponse? = null) {
        val defaultDisplay = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        resolution = "${defaultDisplay.height}x${defaultDisplay.width}"

        val apiRequest = VirtusizeApi.sendEventToAPI(
            virtusizeEvent = event,
            productCheckResponse = withDataProduct,
            deviceOrientation = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) context.getString(R.string.landscape)
            else context.getString(R.string.portrait),
            screenResolution = resolution,
            versionCode = context.packageManager
                .getPackageInfo(context.packageName, 0).versionCode
        )
        perform(
            url = apiRequest.url,
            callback = null,
            method = apiRequest.method,
            dataType = null,
            params = apiRequest.params
        )
    }
}

/**
 * This method throws VirtusizeError. It logs error information and exits normal app flow by throwing error
 * @param error VirtusizeError
 * @throws IllegalArgumentException
 * @see VirtusizeError
 */
fun throwError(error: VirtusizeError) {
    Log.e(Constants.LOG_TAG, error.message())
    error.throwError()
}

/**
 * This class utilizes builder pattern to Build and return Virtusize object
 */
class VirtusizeBuilder {
    private var userId: Int? = null // user Id corresponding to the app user
    private var apiKey: String? = null // Virtusize API Key Unique to each Virtusize client
    private var env = VirtusizeEnvironment.GLOBAL // VirtusizeEnvironment
    private var context: Context? = null // Application Context

    /**
     * This method is used to add application context to Virtusize object
     * Context is required for Virtusize object to function properly
     * @param ctx Application Context
     * @return VirtusizeBuilder
     */
    fun init(ctx: Context): VirtusizeBuilder {
        context = ctx
        return this
    }

    /**
     * This method is used to add user id corresponding to the app user
     * @param id userId corresponding to the app user
     * @return VirtusizeBuilder
     */
    fun setAppId(id: Int): VirtusizeBuilder {
        this.userId = id
        return this
    }

    /**
     * This method is used to set api key provided to Virtusize clients to Virtusize objects
     * API Key is required for Virtusize object to function properly
     * @param key API Key
     * @return VirtusizeBuilder
     */
    fun setApiKey(key: String): VirtusizeBuilder {
        this.apiKey = key
        return this
    }

    /**
     * This method is used to set environment to Virtusize objects
     * By default environment is GLOBAL
     * @param environment VirtusizeEnvironment
     * @return VirtusizeBuilder
     */
    fun setEnv(environment: VirtusizeEnvironment): VirtusizeBuilder {
        this.env = environment
        return this
    }

    /**
     * This method builds the Virtusize object from data passed and returns the Virtusize object
     * @return Virtusize
     * @see Virtusize
     */
    fun build(): Virtusize {
        if (apiKey.isNullOrEmpty()) {
            throwError(VirtusizeError.ApiKeyNullOrEmpty)
        }
        if (context == null) {
            throwError(VirtusizeError.NullContext)
        }
        return Virtusize(userId = userId, apiKey = apiKey!!, env = env, context = context!!)
    }
}

/**
 * Constants used in Virtusize library
 */
object Constants {
    const val FRAG_TAG = "FIT_FRAG_TAG"
    const val URL_KEY = "URL_KEY"
    const val LOG_TAG = "VIRTUSIZE"
    const val BID_KEY = "BID_KEY_VIRTUSIZE"
    const val JSBridgeName = "VirtusizeAndroid"
}