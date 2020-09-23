package com.virtusize.libsource

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.util.Log
import android.view.WindowManager
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.parsers.I18nLocalizationJsonParser.TrimType
import com.virtusize.libsource.data.parsers.*
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.network.ApiRequest
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.network.VirtusizeApiTask
import com.virtusize.libsource.ui.VirtusizeInPageStandard
import com.virtusize.libsource.ui.VirtusizeInPageView
import com.virtusize.libsource.ui.VirtusizeView
import com.virtusize.libsource.util.Constants
import com.virtusize.libsource.util.trimI18nText
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.net.HttpURLConnection
import java.util.*

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
        override fun virtusizeControllerShouldClose(virtusizeView: VirtusizeView) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.virtusizeControllerShouldClose(virtusizeView)
            }
        }

        override fun onEvent(virtusizeView: VirtusizeView?, event: VirtusizeEvent) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onEvent(virtusizeView, event)
            }
        }

        override fun onError(virtusizeView: VirtusizeView?, error: VirtusizeError) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onError(virtusizeView, error)
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

    private var virtusizeProduct: VirtusizeProduct? = null

    init {
        // Virtusize API for building API requests
        VirtusizeApi.init(
            env = params.environment,
            key = params.apiKey!!,
            userId = params.externalUserId ?: ""
        )
    }

    /**
     * Sets up the product for the product detail page
     *
     * @param virtusizeProduct VirtusizeProduct that is being set to the VirtusizeView
     */
    fun setupVirtusizeProduct(virtusizeProduct: VirtusizeProduct?) {
        // Throws NullProduct error if the product is null
        if (virtusizeProduct == null) {
            handleNullProductError()
            return
        }
        this.virtusizeProduct = virtusizeProduct
    }

    /**
     * Sets up the Virtusize view by passing the VirtusizeView
     * @param virtusizeView VirtusizeView that is being set up
     * @throws IllegalArgumentException throws an error if VirtusizeButton is null or the image URL of VirtusizeProduct is invalid
     */
    fun setupVirtusizeView(virtusizeView: VirtusizeView?) {
        // Throws NullProduct error if the product is not set yet
        if (virtusizeProduct == null) {
            handleNullProductError()
            return
        }

        // Throws VirtusizeError.NullVirtusizeButtonError error if button is null
        if (virtusizeView == null) {
            messageHandler.onError(null, VirtusizeErrorType.NullVirtusizeButtonError.virtusizeError())
            throwError(errorType = VirtusizeErrorType.NullVirtusizeButtonError)
            return
        }

        // to handle network errors
        val errorHandler: ErrorResponseHandler = object: ErrorResponseHandler {
            override fun onError(error: VirtusizeError) {
                messageHandler.onError(virtusizeView, error)
            }
        }

        params.bid = browserIdentifier.getBrowserId()
        params.virtusizeProduct = virtusizeProduct
        // Set virtusizeProduct to VirtusizeButton
        virtusizeView.setup(params = params, messageHandler = messageHandler)
        // API Request to perform Product check on Virtusize server
        val apiRequest = VirtusizeApi.productCheck(product = virtusizeProduct!!)
        // Callback Handler for Product Check request
        val productValidCheckListener = object : ValidProductCheckHandler {
            /**
             * This method returns ProductCheckResponse from Virtusize
             * when Product check Request is performed on server on Virtusize server
             */
            override fun onValidProductCheckCompleted(productCheck: ProductCheck) {
                // Sets up Product check response data to VirtusizeProduct in VirtusizeView
                virtusizeView.setupProductCheckResponseData(productCheck)
                if (virtusizeView is VirtusizeInPageView) {
                    getUserProducts({
                        Log.d("User Products", it.toString())
                    }, {
                        Log.e(Constants.INPAGE_LOG_TAG, it.message)
                    })
                    productCheck.data?.productDataId?.let { productId ->
                        val trimType = if(virtusizeView is VirtusizeInPageStandard) TrimType.MULTIPLELINES else TrimType.ONELINE
                        getI18nText({ i18nLocalization ->
                            getStoreProductInfo(productId, onSuccess = { storeProduct ->
                                virtusizeView.setupRecommendationText(storeProduct.getRecommendationText(i18nLocalization).trimI18nText(trimType))
                                if(virtusizeView is VirtusizeInPageStandard) {
                                    virtusizeView.setupProductImage(
                                        params.virtusizeProduct?.imageUrl,
                                        storeProduct.cloudinaryPublicId,
                                        storeProduct.productType,
                                        storeProduct.storeProductMeta?.additionalInfo?.style
                                    )
                                }
                            }, onError = {
                                Log.e(Constants.INPAGE_LOG_TAG, it.message)
                                virtusizeView.showErrorScreen()
                            })
                        }, {
                            Log.e(Constants.INPAGE_LOG_TAG, it.message)
                            virtusizeView.showErrorScreen()
                        })
                    } ?: run {
                        virtusizeView.showErrorScreen()
                    }
                }
                // Send API Event UserSawProduct
                sendEventToApi(
                    event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
                    withDataProduct = productCheck,
                    errorHandler = errorHandler
                )
                messageHandler.onEvent(virtusizeView, VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()))
                productCheck.data?.apply {
                    if (validProduct) {
                        if (fetchMetaData) {
                            if (virtusizeView.virtusizeParams?.virtusizeProduct?.imageUrl != null) {
                                // If image URL is valid, send image URL to server
                                sendProductImageToBackend(
                                    product = virtusizeProduct!!,
                                    errorHandler = errorHandler
                                )
                            } else {
                                messageHandler.onError(
                                    virtusizeView,
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
                            virtusizeView,
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
     * @param errorHandler the error callback to get the [VirtusizeError] in the API task
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
     * @param errorHandler the error callback to get the [VirtusizeError] in the API task
     */
    internal fun getStoreInfo(
        onSuccess: SuccessResponseHandler? = null,
        onError: ErrorResponseHandler? = null) {
        val apiRequest = VirtusizeApi.getStoreInfo()
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
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    fun sendOrder(order: VirtusizeOrder,
                  onSuccess: (() -> Unit)? = null,
                  onError: ((VirtusizeError) -> Unit)? = null) {
        getStoreInfo(object : SuccessResponseHandler{
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
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: SuccessResponseHandler? = null,
        onError: ErrorResponseHandler? = null) {
        getStoreInfo(object : SuccessResponseHandler{
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
     * Retrieves the store product info
     * @param productId the ID of the store product
     * @param onSuccess the optional success callback to pass the [Product]
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    internal fun getStoreProductInfo(
        productId: Int,
        onSuccess: ((Product) -> Unit)? = null,
        onError: ((VirtusizeError) -> Unit)? = null
    ) {
        if(productId == 0) {
            return
        }
        val apiRequest = VirtusizeApi.getStoreProductInfo(productId.toString())
        VirtusizeApiTask()
            .setJsonParser(StoreProductJsonParser())
            .setSuccessHandler(object : SuccessResponseHandler {
                override fun onSuccess(data: Any?) {
                    (data as? Product)?.let {
                        onSuccess?.invoke(it)
                    }
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

    /**
     * Retrieves the list of the product types
     * @param onSuccess the optional success callback to pass the list of [ProductType]
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    internal fun getProductTypes(
        onSuccess: ((List<ProductType>?) -> Unit)? = null,
        onError: ((VirtusizeError) -> Unit)? = null
    ) {
        val apiRequest = VirtusizeApi.getProductTypes()
        VirtusizeApiTask()
            .setJsonParser(ProductTypeJsonParser())
            .setSuccessHandler(object : SuccessResponseHandler {
                override fun onSuccess(data: Any?) {
                    onSuccess?.invoke(data as? List<ProductType>)
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

    /**
     * Gets the i18n localization texts
     * @param onSuccess the optional success callback to pass the [I18nLocalization] from the response when [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeErrorType] in the API task
     */
    internal fun getI18nText(
        onSuccess: ((I18nLocalization) -> Unit)? = null,
        onError: ((VirtusizeError) -> Unit)? = null
    ) {
        val apiRequest = VirtusizeApi.getI18n(params.language ?: (VirtusizeLanguage.values().find { it.value == Locale.getDefault().language } ?: VirtusizeLanguage.EN))
        VirtusizeApiTask()
            .setJsonParser(I18nLocalizationJsonParser(context, params.language))
            .setSuccessHandler(object : SuccessResponseHandler {
                override fun onSuccess(data: Any?) {
                    (data as? I18nLocalization)?.let {
                        onSuccess?.invoke(it)
                    }
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

    /**
     * Retrieves a list of user products
     * @param onSuccess the optional success callback to pass the list of [Product]
     * @param onError the optional error callback to get the [VirtusizeErrorType] in the API task
     */
    internal fun getUserProducts(
        onSuccess: ((List<Product>?) -> Unit)? = null,
        onError: ((VirtusizeError) -> Unit)? = null
    ) {
        val apiRequest = VirtusizeApi.getUserProducts()
        VirtusizeApiTask()
            .setJsonParser(UserProductJsonParser())
            .setSuccessHandler(object : SuccessResponseHandler {
                override fun onSuccess(data: Any?) {
                    onSuccess?.invoke(data as? List<Product>)
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

    /**
     * Handles the null product error
     * @throws VirtusizeErrorType.NullProduct error
     */
    private fun handleNullProductError() {
        messageHandler.onError(null, VirtusizeErrorType.NullProduct.virtusizeError())
        throwError(errorType = VirtusizeErrorType.NullProduct)
    }
}