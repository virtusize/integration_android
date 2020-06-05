package com.virtusize.libsource

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.util.Log
import android.view.WindowManager
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.remote.Store
import com.virtusize.libsource.data.local.VirtusizeOrder
import com.virtusize.libsource.data.remote.parsers.ProductCheckJsonParser
import com.virtusize.libsource.data.remote.parsers.ProductMetaDataHintsJsonParser
import com.virtusize.libsource.data.remote.parsers.StoreJsonParser
import com.virtusize.libsource.network.VirtusizeApiTask
import com.virtusize.libsource.ui.FitIllustratorButton
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
    private val userId: String?,
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
            userId = userId ?: "",
            language = language
        )
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
        val errorHandler: ErrorResponseHandler = object: ErrorResponseHandler {
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
             * This method returns ProductCheckResponse from Virtusize
             * when Product check Request is performed on server on Virtusize server
             */
            override fun onValidProductCheckCompleted(productCheck: ProductCheck) {
                // Set up Product check response data to VirtusizeProduct in FitIllustratorButton
                fitIllustratorButton.setupProductCheckResponseData(productCheck)
                // Send API Event UserSawProduct
                sendEventToApi(
                    event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
                    withDataProduct = productCheck,
                    errorHandler = errorHandler
                )
                messageHandler.onEvent(fitIllustratorButton, VirtusizeEvents.UserSawProduct)
                productCheck.data?.apply {
                    if (validProduct) {
                        if (fetchMetaData) {
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
                            withDataProduct = productCheck,
                            errorHandler = errorHandler
                        )
                        messageHandler.onEvent(
                            fitIllustratorButton,
                            VirtusizeEvents.UserSawWidgetButton
                        )
                    }
                }

            }
        }

        // Execute the API task to make a network request for Product Check
        VirtusizeApiTask()
            .setBrowserID(browserIdentifier.getBrowserId())
            .setResponseCallback(productValidCheckListener)
            .setJsonParser(ProductCheckJsonParser())
            .setErrorHandler(errorHandler)
            .execute(apiRequest)
    }

    /**
     * Sends an image URL of VirtusizeProduct to the Virtusize server
     * @param product VirtusizeProduct
     * @param errorHandler
     * @see VirtusizeProduct
     */
    private fun sendProductImageToBackend(
        product: VirtusizeProduct,
        errorHandler: ErrorResponseHandler) {
        val apiRequest = VirtusizeApi.sendProductImageToBackend(product = product)
        VirtusizeApiTask()
            .setBrowserID(browserIdentifier.getBrowserId())
            .setJsonParser(ProductMetaDataHintsJsonParser())
            .setErrorHandler(errorHandler)
            .execute(apiRequest)
    }

    /**
     * Sends an event to the Virtusize server
     * @param event VirtusizeEvent
     * @param withDataProduct ProductCheckResponse corresponding to VirtusizeProduct
     * @param errorHandler the error callback to get the [VirtusizeError] in the API task
     */
    private fun sendEventToApi(
        event: VirtusizeEvent,
        withDataProduct: ProductCheck? = null,
        errorHandler: ErrorResponseHandler
    ) {
        val defaultDisplay =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        resolution = "${defaultDisplay.height}x${defaultDisplay.width}"

        val apiRequest = VirtusizeApi.sendEventToAPI(
            virtusizeEvent = event,
            productCheck = withDataProduct,
            deviceOrientation = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) context.getString(
                R.string.landscape
            )
            else context.getString(R.string.portrait),
            screenResolution = resolution,
            versionCode = context.packageManager
                .getPackageInfo(context.packageName, 0).versionCode
        )
        VirtusizeApiTask()
            .setBrowserID(browserIdentifier.getBrowserId())
            .setErrorHandler(errorHandler)
            .execute(apiRequest)
    }

    /**
     * Retrieves the specific store info
     * @param onSuccess the success callback to get the [Store] in the API task
     * @param errorHandler the error callback to get the [VirtusizeError] in the API task
     */
    private fun retrieveStoreInfo(
        onSuccess: SuccessResponseHandler? = null,
        onError: ErrorResponseHandler? = null) {
        val apiRequest = VirtusizeApi.retrieveStoreInfo()
        VirtusizeApiTask()
            .setJsonParser(StoreJsonParser())
            .setSuccessHandler(onSuccess)
            .setErrorHandler(onError)
            .execute(apiRequest)
    }

    /**
     * Sends an order to the Virtusize server for Kotlin apps
     * @param order
     * @param onSuccess the optional success callback to notify [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    fun sendOrder(order: VirtusizeOrder,
                  onSuccess: (() -> Unit)? = null,
                  onError: ((VirtusizeError) -> Unit)? = null) {
        retrieveStoreInfo(object : SuccessResponseHandler{
            override fun onSuccess(data: Any?) {
                /**
                 * Throws the error if the user id is not set up or empty during the initialization of the [Virtusize] class
                 */
                if(userId.isNullOrEmpty()) {
                    throwError(VirtusizeError.UserIdNullOrEmpty)
                }
                // Sets the region from the store info
                if(data is Store) {
                    data.region?.let { order.setRegion(it) }
                }
                val apiRequest = VirtusizeApi.sendOrder(order)
                VirtusizeApiTask()
                    .setBrowserID(browserIdentifier.getBrowserId())
                    .setSuccessHandler(object : SuccessResponseHandler {
                        override fun onSuccess(data: Any?) {
                            onSuccess?.invoke()
                        }
                    })
                    .setErrorHandler(object : ErrorResponseHandler {
                        override fun onError(error: VirtusizeError) {
                            onError?.invoke(error)
                        }
                    })
                    .execute(apiRequest)
            }
        })
    }

    /**
     * Sends an order to the Virtusize server for Java apps
     * @param order
     * @param onSuccess the optional success callback to pass the [Store] from the response when [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: SuccessResponseHandler? = null,
        onError: ErrorResponseHandler? = null) {
        retrieveStoreInfo(object : SuccessResponseHandler{
            override fun onSuccess(data: Any?) {
                /**
                 * Throws the error if the user id is not set up or empty during the initialization of the [Virtusize] class
                 */
                if(userId.isNullOrEmpty()) {
                    throwError(VirtusizeError.UserIdNullOrEmpty)
                }
                // Sets the region from the store info
                if(data is Store) {
                    data.region?.let { order.setRegion(it) }
                }
                val apiRequest = VirtusizeApi.sendOrder(order)
                VirtusizeApiTask()
                    .setBrowserID(browserIdentifier.getBrowserId())
                    .setSuccessHandler(onSuccess)
                    .setErrorHandler(onError)
                    .execute(apiRequest)
            }

        })
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
     * then when the activity or fragment opens again,
     * it will keep listening to the events along with newly registered message handlers.
     * @param messageHandler an instance of {@link VirtusizeMessageHandler}
     * @see VirtusizeMessageHandler
     */
    fun unregisterMessageHandler(messageHandler: VirtusizeMessageHandler) {
        messageHandlers.remove(messageHandler)
    }
}

/**
 * Throws a VirtusizeError.
 * It logs the error information and exits the normal app flow by throwing an error
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
    private var userId: String? = null
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
    fun setUserId(id: String): VirtusizeBuilder {
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
