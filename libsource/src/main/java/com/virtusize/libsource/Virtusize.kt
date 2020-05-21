package com.virtusize.libsource

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.util.Log
import android.view.WindowManager
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
 *
 * @param userId the user id that is an unique user ID from the client system
 * @param apiKey the API key that is unique to every Virtusize Client
 * @param env the Virtusize environment
 * @param context Android Application Context
 */
class Virtusize(
    userId: Int?,
    apiKey: String,
    env: VirtusizeEnvironment,
    private val context: Context
) {

    // Registered message handlers
    private val messageHandlers = mutableListOf<VirtusizeMessageHandler>()

    // The Virtusize message handler passes received errors and events to registered message handlers
    private val messageHandler = object : VirtusizeMessageHandler {
        override fun virtusizeControllerShouldClose(fitIllustratorButton: FitIllustratorButton) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.virtusizeControllerShouldClose(fitIllustratorButton)
            }
        }

        override fun onEvent(fitIllustratorButton: FitIllustratorButton?, event: VirtusizeEvents) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onEvent(fitIllustratorButton, event)
            }
        }

        override fun onError(fitIllustratorButton: FitIllustratorButton?, error: VirtusizeError) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onError(fitIllustratorButton, error)
            }
        }

    }

    // The BrowserIdentifier contains the browser identifier for the current SDK
    private var browserIdentifier: BrowserIdentifier = BrowserIdentifier(
        sharedPrefs =
        context.getSharedPreferences(
            Constants.SHARED_PREFS_NAME,
            MODE_PRIVATE
        )
    )

    // The language code that defaults to the device's default language
    private val language = Locale.getDefault().language

    // Device screen resolution
    private lateinit var resolution: String

    init {
        // Virtusize API for building API requests
        VirtusizeApi.init(
            env = env,
            key = apiKey,
            browserID = browserIdentifier.getBrowserId(),
            userId = userId?.toString() ?: "",
            language = language
        )
    }

    // Volley request queue
    private val requestQueue = Volley.newRequestQueue(context)

    // GSON for JSON to Java serialization
    private val gson = GsonBuilder().create()

    /**
     * This method is used to perform API requests.
     * Needs Internet permission
     * Makes network calls
     * @param url the request URL
     * @param callback the callback which is used to send data back when the request is successful and completed
     * @param method the API request method type
     * @param dataType the Class for converting the JSON response to a Java object of type dataType
     * @param params the MutableMap of query parameters to be sent to the server
     * @param errorHandler optional error handler
     */
    private fun perform(
        url: String,
        callback: CallbackHandler?,
        method: Int,
        dataType: Class<*>?,
        params: MutableMap<String, Any> = mutableMapOf(),
        errorHandler: ErrorHandler? = null
    ) {
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
                    errorHandler?.onError(VirtusizeError.NetworkError)
                })

            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest)
        } else {
            val jsonBody = JSONObject()

            for ((key, value) in params) {
                jsonBody.put(key, value)
            }
            val jsonObjectRequest = object: JsonObjectRequest(method, url, jsonBody,
                Response.Listener { response ->
                    Log.d(Constants.LOG_TAG, response.toString())
                },
                Response.ErrorListener { error ->
                    if (error != null)
                        handleVolleyError(error)
                    errorHandler?.onError(VirtusizeError.NetworkError)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["x-vs-bid"] = browserIdentifier.getBrowserId()
                    return headers
                }
            }
            requestQueue.add(jsonObjectRequest)
        }

    }

    /**
     * Handles errors received when performing network requests using Volley
     * It Logs error.
     */
    private fun handleVolleyError(err: VolleyError) {
        Log.e(Constants.LOG_TAG, err.toString() + err.localizedMessage)
    }

    /**
     * Sets up the Fit Illustrator button by setting VirtusizeProduct to this button
     * @param fitIllustratorButton FitIllustratorButton that is being set up
     * @param virtusizeProduct VirtusizeProduct that is being set to this button
     * @throws IllegalArgumentException throws an error if FitIllustratorButton is null or the image URL of VirtusizeProduct is invalid
     */
    fun setupFitButton(
        fitIllustratorButton: FitIllustratorButton?,
        virtusizeProduct: VirtusizeProduct
    ) {

        // Throws VirtusizeError.NullFitButtonError error if button is null
        if (fitIllustratorButton == null) {
            messageHandler.onError(null, VirtusizeError.NullFitButtonError)
            throwError(error = VirtusizeError.NullFitButtonError)
            return
        }

        // to handle network errors
        val errorHandler = object : ErrorHandler {
            override fun onError(error: VirtusizeError) {
                messageHandler.onError(fitIllustratorButton, error)
            }
        }

        // Set virtusizeProduct to fitIllustratorButton
        fitIllustratorButton.setup(product = virtusizeProduct, messageHandler = messageHandler)
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
                sendEventToApi(
                    event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
                    withDataProduct = productCheckResponse,
                    errorHandler = errorHandler
                )
                messageHandler.onEvent(fitIllustratorButton, VirtusizeEvents.UserSawProduct)
                if (productCheckResponse.data.validProduct) {
                    if (productCheckResponse.data.fetchMetaData) {
                        if (fitIllustratorButton.virtusizeProduct?.imageUrl != null) {
                            // If image URL is valid, send image URL to server
                            sendProductImageToBackend(
                                product = fitIllustratorButton.virtusizeProduct!!,
                                errorHandler = errorHandler
                            )
                        } else {
                            messageHandler.onError(
                                fitIllustratorButton,
                                VirtusizeError.ImageUrlNotValid
                            )
                            throwError(VirtusizeError.ImageUrlNotValid)
                        }
                    }
                    // Send API Event UserSawWidgetButton
                    sendEventToApi(
                        event = VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()),
                        withDataProduct = productCheckResponse,
                        errorHandler = errorHandler
                    )
                    messageHandler.onEvent(
                        fitIllustratorButton,
                        VirtusizeEvents.UserSawWidgetButton
                    )
                }
            }
        }

        // Perform the network request for Product Check
        perform(
            url = apiRequest.url,
            callback = productValidCheckListener,
            method = apiRequest.method,
            dataType = ProductCheckResponse::class.java,
            errorHandler = errorHandler
        )
    }

    /**
     * Sends an image URL of VirtusizeProduct to the Virtusize server
     * @param product VirtusizeProduct
     * @param errorHandler
     * @see VirtusizeProduct
     * @see ErrorHandler
     */
    private fun sendProductImageToBackend(product: VirtusizeProduct, errorHandler: ErrorHandler) {
        val apiRequest = VirtusizeApi.sendProductImageToBackend(product = product)
        perform(
            url = apiRequest.url,
            callback = null,
            method = apiRequest.method,
            dataType = ProductMetaDataHintsResponse::class.java,
            params = apiRequest.params,
            errorHandler = errorHandler
        )
    }

    /**
     * Send an event to the Virtusize server
     * @param event VirtusizeEvent
     * @param withDataProduct ProductCheckResponse corresponding to VirtusizeProduct
     * @param errorHandler
     * @see ErrorHandler
     */
    private fun sendEventToApi(
        event: VirtusizeEvent,
        withDataProduct: ProductCheckResponse? = null,
        errorHandler: ErrorHandler
    ) {
        val defaultDisplay =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        resolution = "${defaultDisplay.height}x${defaultDisplay.width}"

        val apiRequest = VirtusizeApi.sendEventToAPI(
            virtusizeEvent = event,
            productCheckResponse = withDataProduct,
            deviceOrientation = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) context.getString(
                R.string.landscape
            )
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
            params = apiRequest.params,
            errorHandler = errorHandler
        )
    }

    /**
     * Registers a message handler.
     * The registered message handlers will receive Virtusize errors, events, and the close action for Fit Illustrator.
     * @param messageHandler an instance of VirtusizeMessageHandler
     * @see VirtusizeMessageHandler
     */
    fun registerMessageHandler(messageHandler: VirtusizeMessageHandler) {
        messageHandlers.add(messageHandler)
    }

    /**
     * Unregisters a message handler.
     * If a message handler is not unregistered when the associated activity or fragment dies,
     * then when the activity or fragment opens again, it will keep listening to the events along with newly registered message handlers.
     * @param messageHandler an instance of {@link VirtusizeMessageHandler}
     * @see VirtusizeMessageHandler
     */
    fun unregisterMessageHandler(messageHandler: VirtusizeMessageHandler) {
        messageHandlers.remove(messageHandler)
    }
}

/**
 * Throws a VirtusizeError. It logs the error information and exits the normal app flow by throwing an error
 * @param error VirtusizeError
 * @throws IllegalArgumentException
 * @see VirtusizeError
 */
fun throwError(error: VirtusizeError) {
    Log.e(Constants.LOG_TAG, error.message())
    error.throwError()
}

/**
 * This class utilizes the builder pattern to build and return a Virtusize object
 * @param userId the user id that is the unique user id from the client system
 * @param apiKey the API key that is unique to every Virtusize Client
 * @param env the Virtusize environment
 * @param context Android Application Context
 */
class VirtusizeBuilder {
    private var userId: Int? = null
    private var apiKey: String? = null
    private var env = VirtusizeEnvironment.GLOBAL
    private var context: Context? = null

    /**
     * This method is used to add the application context to the Virtusize builder
     * Context is required for this Virtusize builder to function properly
     * @param ctx Application Context
     * @return VirtusizeBuilder
     */
    fun init(ctx: Context): VirtusizeBuilder {
        context = ctx
        return this
    }

    /**
     * Sets up the user ID from the client system
     * @param id the id that is an unique user ID from the client system
     * @return VirtusizeBuilder
     */
    fun setAppId(id: Int): VirtusizeBuilder {
        this.userId = id
        return this
    }

    /**
     * Sets up the API key provided to Virtusize clients to the Virtusize object
     * The API key is required for the Virtusize object to function properly
     * @param key the API Key
     * @return VirtusizeBuilder
     */
    fun setApiKey(key: String): VirtusizeBuilder {
        this.apiKey = key
        return this
    }

    /**
     * Sets up the Virtusize environment to the Virtusize object
     * By default, the environment value is GLOBAL
     * @param environment VirtusizeEnvironment
     * @return VirtusizeBuilder
     */
    fun setEnv(environment: VirtusizeEnvironment): VirtusizeBuilder {
        this.env = environment
        return this
    }

    /**
     * Builds the Virtusize object from the passed data and returns the Virtusize object
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
 * Constants used in the Virtusize SDK
 */
object Constants {
    const val FRAG_TAG = "FIT_FRAG_TAG"
    const val URL_KEY = "URL_KEY"
    const val LOG_TAG = "VIRTUSIZE"
    const val SHARED_PREFS_NAME = "VIRTUSIZE_SHARED_PREFS"
    const val BID_KEY = "BID_KEY_VIRTUSIZE"
    const val JSBridgeName = "VirtusizeAndroid"
}
