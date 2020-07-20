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
import com.virtusize.libsource.data.local.VirtusizeParams
import com.virtusize.libsource.data.parsers.ProductCheckJsonParser
import com.virtusize.libsource.data.parsers.ProductMetaDataHintsJsonParser
import com.virtusize.libsource.data.parsers.StoreJsonParser
import com.virtusize.libsource.network.ApiRequest
import com.virtusize.libsource.network.VirtusizeApiTask
import com.virtusize.libsource.ui.VirtusizeButton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import java.net.HttpURLConnection

/**
 * This is the main class that can be used by Virtusize Clients to perform all available operations related to fit check
 *
 * @param context Android Application Context
 * @param params [VirtusizeParams] that contains userId, apiKey, env and other parameters to be passed to the Virtusize web app
 */
class Virtusize(
    private val context: Context,
    private val params: VirtusizeParams
) {
    // Registered message handlers
    private val messageHandlers = mutableListOf<VirtusizeMessageHandler>()

    // The Virtusize message handler passes received errors and events to registered message handlers
    private val messageHandler = object : VirtusizeMessageHandler {
        override fun virtusizeControllerShouldClose(virtusizeButton: VirtusizeButton) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.virtusizeControllerShouldClose(virtusizeButton)
            }
        }

        override fun onEvent(virtusizeButton: VirtusizeButton?, event: VirtusizeEvent) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onEvent(virtusizeButton, event)
            }
        }

        override fun onError(virtusizeButton: VirtusizeButton?, error: VirtusizeError) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onError(virtusizeButton, error)
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

    // Device screen resolution
    private lateinit var resolution: String

    // The HTTP URL connection that is used to make a single request
    private var httpURLConnection: HttpURLConnection? = null

    // The dispatcher that determines what thread the corresponding coroutine uses for its execution
    private var coroutineDispatcher: CoroutineDispatcher = IO

    init {
        // Virtusize API for building API requests
        VirtusizeApi.init(
            env = params.environment,
            key = params.apiKey!!,
            userId = params.externalUserId ?: ""
        )
    }

    /**
     * Sets up the Virtusize button by setting VirtusizeProduct to this button
     * @param virtusizeButton VirtusizeButton that is being set up
     * @param virtusizeProduct VirtusizeProduct that is being set to this button
     * @throws IllegalArgumentException throws an error if VirtusizeButton is null or the image URL of VirtusizeProduct is invalid
     */
    fun setupVirtusizeButton(virtusizeButton: VirtusizeButton?, virtusizeProduct: VirtusizeProduct?) {

        // Throws VirtusizeError.NullVirtusizeButtonError error if button is null
        if (virtusizeButton == null) {
            messageHandler.onError(null, VirtusizeErrorType.NullVirtusizeButtonError.virtusizeError())
            throwError(errorType = VirtusizeErrorType.NullVirtusizeButtonError)
            return
        }

        if (virtusizeProduct == null) {
            messageHandler.onError(null, VirtusizeErrorType.NullProduct.virtusizeError())
            throwError(errorType = VirtusizeErrorType.NullProduct)
            return
        }

        // to handle network errors
        val errorHandler: ErrorResponseHandler = object: ErrorResponseHandler {
            override fun onError(error: VirtusizeError) {
                messageHandler.onError(virtusizeButton, error)
            }
        }

        params.bid = browserIdentifier.getBrowserId()
        params.virtusizeProduct = virtusizeProduct
        // Set virtusizeProduct to VirtusizeButton
        virtusizeButton.setup(params = params, messageHandler = messageHandler)
        // API Request to perform Product check on Virtusize server
        val apiRequest = VirtusizeApi.productCheck(product = virtusizeProduct)
        // Callback Handler for Product Check request
        val productValidCheckListener = object : ValidProductCheckHandler {

            /**
             * This method returns ProductCheckResponse from Virtusize
             * when Product check Request is performed on server on Virtusize server
             */
            override fun onValidProductCheckCompleted(productCheck: ProductCheck) {
                // Set up Product check response data to VirtusizeProduct in VirtusizeButton
                virtusizeButton.setupProductCheckResponseData(productCheck)
                // Send API Event UserSawProduct
                sendEventToApi(
                    event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
                    withDataProduct = productCheck,
                    errorHandler = errorHandler
                )
                messageHandler.onEvent(virtusizeButton, VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()))
                productCheck.data?.apply {
                    if (validProduct) {
                        if (fetchMetaData) {
                            if (virtusizeButton.virtusizeParams?.virtusizeProduct?.imageUrl != null) {
                                // If image URL is valid, send image URL to server
                                sendProductImageToBackend(
                                    product = virtusizeProduct,
                                    errorHandler = errorHandler
                                )
                            } else {
                                messageHandler.onError(
                                    virtusizeButton,
                                    VirtusizeErrorType.ImageUrlNotValid.virtusizeError()
                                )
                                throwError(VirtusizeErrorType.ImageUrlNotValid)
                            }
                        }
                        // Send API Event UserSawWidgetButton
                        sendEventToApi(
                            event = VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()),
                            withDataProduct = productCheck,
                            errorHandler = errorHandler
                        )
                        messageHandler.onEvent(
                            virtusizeButton,
                            VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName())
                        )
                    }
                }

            }
        }

        productDataCheck(productValidCheckListener, errorHandler, apiRequest)
    }

    /**
     * Executes the API task to make a network request for Product Check
     * @param productValidCheckListener VirtusizeButton that is being set up
     * @param errorHandler VirtusizeProduct that is being set to this button
     * @param apiRequest [ApiRequest]
    */
    internal fun productDataCheck(productValidCheckListener: ValidProductCheckHandler,
                                  errorHandler: ErrorResponseHandler,
                                  apiRequest: ApiRequest
    ) {
        VirtusizeApiTask()
            .setBrowserID(browserIdentifier.getBrowserId())
            .setSuccessHandler(productValidCheckListener)
            .setJsonParser(ProductCheckJsonParser())
            .setErrorHandler(errorHandler)
            .setHttpURLConnection(httpURLConnection)
            .setCoroutineDispatcher(coroutineDispatcher)
            .execute(apiRequest)
    }

    /**
     * Sends an image URL of VirtusizeProduct to the Virtusize server
     * @param product VirtusizeProduct
     * @param errorHandler
     * @see VirtusizeProduct
     */
    internal fun sendProductImageToBackend(
        product: VirtusizeProduct,
        successHandler: SuccessResponseHandler? = null,
        errorHandler: ErrorResponseHandler) {
        val apiRequest = VirtusizeApi.sendProductImageToBackend(product = product)
        VirtusizeApiTask()
            .setBrowserID(browserIdentifier.getBrowserId())
            .setJsonParser(ProductMetaDataHintsJsonParser())
            .setSuccessHandler(successHandler)
            .setErrorHandler(errorHandler)
            .setHttpURLConnection(httpURLConnection)
            .setCoroutineDispatcher(coroutineDispatcher)
            .execute(apiRequest)
    }

    /**
     * Sends an event to the Virtusize server
     * @param event VirtusizeEvent
     * @param withDataProduct ProductCheckResponse corresponding to VirtusizeProduct
     * @param errorHandler the error callback to get the [VirtusizeErrorType] in the API task
     */
    internal fun sendEventToApi(
        event: VirtusizeEvent,
        withDataProduct: ProductCheck? = null,
        successHandler: SuccessResponseHandler? = null,
        errorHandler: ErrorResponseHandler) {
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
            .setSuccessHandler(successHandler)
            .setErrorHandler(errorHandler)
            .setHttpURLConnection(httpURLConnection)
            .setCoroutineDispatcher(coroutineDispatcher)
            .execute(apiRequest)
    }

    /**
     * Retrieves the specific store info
     * @param onSuccess the success callback to get the [Store] in the API task
     * @param errorHandler the error callback to get the [VirtusizeErrorType] in the API task
     */
    internal fun retrieveStoreInfo(
        onSuccess: SuccessResponseHandler? = null,
        onError: ErrorResponseHandler? = null) {
        val apiRequest = VirtusizeApi.retrieveStoreInfo()
        VirtusizeApiTask()
            .setJsonParser(StoreJsonParser())
            .setSuccessHandler(onSuccess)
            .setErrorHandler(onError)
            .setHttpURLConnection(httpURLConnection)
            .setCoroutineDispatcher(coroutineDispatcher)
            .execute(apiRequest)
    }

    /**
     * Sends an order to the Virtusize server for Kotlin apps
     * @param order
     * @param onSuccess the optional success callback to notify [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeErrorType] in the API task
     */
    fun sendOrder(order: VirtusizeOrder,
                  onSuccess: (() -> Unit)? = null,
                  onError: ((VirtusizeError) -> Unit)? = null) {
        retrieveStoreInfo(object : SuccessResponseHandler{
            override fun onSuccess(data: Any?) {
                /**
                 * Throws the error if the user id is not set up or empty during the initialization of the [Virtusize] class
                 */
                if(params.externalUserId.isNullOrEmpty()) {
                    throwError(VirtusizeErrorType.UserIdNullOrEmpty)
                }
                // Sets the region from the store info
                order.setRegion((data as? Store)?.region)
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
                    .setHttpURLConnection(httpURLConnection)
                    .setCoroutineDispatcher(coroutineDispatcher)
                    .execute(apiRequest)
            }
        }, object : ErrorResponseHandler{
            override fun onError(error: VirtusizeError) {
                onError?.invoke(error)
            }

        })
    }

    /**
     * Sends an order to the Virtusize server for Java apps
     * @param order
     * @param onSuccess the optional success callback to pass the [Store] from the response when [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeErrorType] in the API task
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
                if(params.externalUserId.isNullOrEmpty()) {
                    throwError(VirtusizeErrorType.UserIdNullOrEmpty)
                }
                // Sets the region from the store info
                order.setRegion((data as? Store)?.region)
                val apiRequest = VirtusizeApi.sendOrder(order)
                VirtusizeApiTask()
                    .setBrowserID(browserIdentifier.getBrowserId())
                    .setSuccessHandler(onSuccess)
                    .setErrorHandler(onError)
                    .setHttpURLConnection(httpURLConnection)
                    .setCoroutineDispatcher(coroutineDispatcher)
                    .execute(apiRequest)
            }
        }, object : ErrorResponseHandler{
            override fun onError(error: VirtusizeError) {
                onError(error)
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

    /**
     * Sets the HTTP URL connection
     * @param urlConnection an instance of [HttpURLConnection]
     */
    internal fun setHTTPURLConnection(urlConnection: HttpURLConnection?) {
        this.httpURLConnection = urlConnection
    }

    /**
     * Sets the Coroutine dispatcher
     * @param dispatcher an instance of [CoroutineDispatcher]
     */
    internal fun setCoroutineDispatcher(dispatcher: CoroutineDispatcher) {
        this.coroutineDispatcher = dispatcher
    }
}

/**
 * Throws a VirtusizeError.
 * It logs the error information and exits the normal app flow by throwing an error
 * @param errorType VirtusizeError
 * @throws IllegalArgumentException
 * @see VirtusizeErrorType
 */
internal fun throwError(errorType: VirtusizeErrorType) {
    Log.e(Constants.LOG_TAG, errorType.message())
    errorType.throwError()
}

/**
 * This class utilizes the builder pattern to build and return a Virtusize object
 * @param userId the user id that is the unique user id from the client system
 * @param apiKey the API key that is unique to every Virtusize Client
 * @param env the Virtusize environment
 * @param context Android Application Context
 * @param region the [VirtusizeRegion] that is used to set the region of the config url domains within the Virtusize web app
 * @param language the [VirtusizeLanguage] that sets the initial language the Virtusize web app will load in
 * @param allowedLanguages the languages that the user can switch to using the Language Selector
 * @param showSGI the Boolean value to determine whether the Virtusize web app will fetch SGI and use SGI flow for users to add user generated items to their wardrobe
 * @param detailsPanelCards the info categories that will display in the Product Details tab
 */
class VirtusizeBuilder {
    private var userId: String? = null
    private var apiKey: String? = null
    private var browserID: String? = null
    private var env = VirtusizeEnvironment.GLOBAL
    private var context: Context? = null
    private var region: VirtusizeRegion = VirtusizeRegion.JP
    private var language: VirtusizeLanguage? = region.defaultLanguage()
    private var allowedLanguages: MutableList<VirtusizeLanguage> = VirtusizeLanguage.values().asList().toMutableList()
    private var showSGI: Boolean = false
    private var detailsPanelCards: MutableList<VirtusizeInfoCategory> = VirtusizeInfoCategory.values().asList().toMutableList()

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
        this.region = environment.virtusizeRegion()
        return this
    }

    /**
     * Sets up the initial display language for the Virtusize web app
     * By default, the language value is based on the region value
     * @param language [VirtusizeLanguage]
     * @return VirtusizeBuilder
     */
    fun setLanguage(language: VirtusizeLanguage) : VirtusizeBuilder {
        this.language = language
        return this
    }

    /**
     * Sets up the languages for users to select for the Virtusize web app
     * By default, the Virtusize web app allows all the possible languages
     * @param allowedLanguages the list of [VirtusizeLanguage]
     * @return VirtusizeBuilder
     */
    fun setAllowedLanguages(allowedLanguages: MutableList<VirtusizeLanguage>) : VirtusizeBuilder {
        this.allowedLanguages = allowedLanguages
        return this
    }

    /**
     * Sets up whether the Virtusize web app will fetch SGI and use SGI flow for users to add user generated items to their wardrobe
     * By default, showSGI is false
     * @param showSGI the Boolean value
     * @return VirtusizeBuilder
     */
    fun setShowSGI(showSGI: Boolean) : VirtusizeBuilder {
        this.showSGI = showSGI
        return this
    }

    /**
     * Sets up the info categories that will be displayed in the Product Details tab of the Virtusize web app
     * By default, the Virtusize web app display all the possible info categories
     * @param detailsPanelCards the list of [VirtusizeInfoCategory]
     * @return VirtusizeBuilder
     */
    fun setDetailsPanelCards(detailsPanelCards: MutableList<VirtusizeInfoCategory>) : VirtusizeBuilder {
        this.detailsPanelCards = detailsPanelCards
        return this
    }

    /**
     * Builds the Virtusize object from the passed data and returns the Virtusize object
     * @return Virtusize
     * @see Virtusize
     */
    fun build(): Virtusize {
        if (apiKey.isNullOrEmpty()) {
            throwError(VirtusizeErrorType.ApiKeyNullOrInvalid)
        }
        if (context == null) {
            throwError(VirtusizeErrorType.NullContext)
        }
        val params = VirtusizeParams(
            apiKey = apiKey,
            bid = browserID,
            environment = env,
            region = region,
            language = language,
            allowedLanguages = allowedLanguages,
            virtusizeProduct = null,
            externalUserId = userId,
            showSGI = showSGI,
            detailsPanelCards = detailsPanelCards
        )
        return Virtusize(context = context!!, params = params)
    }
}

/**
 * Constants used in the Virtusize SDK
 */
object Constants {
    const val FRAG_TAG = "FRAG_TAG"
    const val URL_KEY = "URL_KEY"
    const val VIRTUSIZE_PARAMS_SCRIPT_KEY = "VIRTUSIZE_PARAMS_SCRIPT_KEY"
    const val LOG_TAG = "VIRTUSIZE"
    const val SHARED_PREFS_NAME = "VIRTUSIZE_SHARED_PREFS"
    const val BID_KEY = "BID_KEY_VIRTUSIZE"
    const val JSBridgeName = "VirtusizeAndroid"
}
