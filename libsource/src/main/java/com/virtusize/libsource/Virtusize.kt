package com.virtusize.libsource

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.util.Log
import android.view.WindowManager
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.parsers.*
import com.virtusize.libsource.data.parsers.I18nLocalizationJsonParser.TrimType
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.network.*
import com.virtusize.libsource.network.ApiRequest
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.network.VirtusizeApiTask
import com.virtusize.libsource.ui.VirtusizeInPageStandard
import com.virtusize.libsource.ui.VirtusizeInPageView
import com.virtusize.libsource.ui.VirtusizeView
import com.virtusize.libsource.util.Constants
import com.virtusize.libsource.util.VirtusizeUtils
import com.virtusize.libsource.util.trimI18nText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        override fun onEvent(event: VirtusizeEvent) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onEvent(event)
            }
        }

        override fun onError(error: VirtusizeError) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onError(error)
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

    // This variable holds the product information from the client and the product data check API
    private var virtusizeProduct: VirtusizeProduct? = null

    // TODO: add comment
    private var virtusizeViews = mutableListOf<VirtusizeView>()

    init {
        // Virtusize API for building API requests
        VirtusizeApi.init(
            env = params.environment,
            key = params.apiKey!!,
            userId = params.externalUserId ?: ""
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
        messageHandler.onError(VirtusizeErrorType.NullProduct.virtusizeError())
        throwError(errorType = VirtusizeErrorType.NullProduct)
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

        params.bid = browserIdentifier.getBrowserId()
        params.virtusizeProduct = virtusizeProduct

        // API Request to perform Product check on Virtusize server
        val apiRequest = VirtusizeApi.productCheck(product = virtusizeProduct)

        val errorHandler: ErrorResponseHandler = object : ErrorResponseHandler {
            override fun onError(error: VirtusizeError) {
                messageHandler.onError(error)
                virtusizeViews.clear()
            }
        }

        val productValidCheckListener = object : ValidProductCheckHandler {
            /**
             * This method returns ProductCheckResponse from Virtusize
             * when Product check Request is performed on server on Virtusize server
             */
            override fun onValidProductCheckCompleted(productCheck: ProductCheck) {
                var containInPage = false
                for (i in 0 until virtusizeViews.size) {
                    if(virtusizeViews[i] is VirtusizeInPageView) {
                        containInPage = true
                    }
                    virtusizeViews[i].setup(params = params, messageHandler = messageHandler)
                    virtusizeViews[i].setupProductCheckResponseData(productCheck)
                }

                // Send API Event UserSawProduct
                sendEventToApi(
                    event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
                    withDataProduct = productCheck,
                    errorHandler = errorHandler
                )
                messageHandler.onEvent(VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()))

                productCheck.data?.apply {
                    if (validProduct) {
                        if (fetchMetaData) {
                            if (params.virtusizeProduct?.imageUrl != null) {
                                // If image URL is valid, send image URL to server
                                sendProductImageToBackend(
                                    product = virtusizeProduct,
                                    errorHandler = errorHandler
                                )
                            } else {
                                messageHandler.onError(VirtusizeErrorType.ImageUrlNotValid.virtusizeError())
                                throwError(VirtusizeErrorType.ImageUrlNotValid)
                            }
                        }
                        // Send API Event UserSawWidgetButton
                        sendEventToApi(
                            event = VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()),
                            withDataProduct = productCheck,
                            errorHandler = errorHandler
                        )
                        messageHandler.onEvent(VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()))
                    }
                }

                if(containInPage) {
                    productCheck.data?.productDataId?.let { productId ->
                        CoroutineScope(Main).launch {
                            val storeProduct: Product?
                            val storeProductResponse = getStoreProductResponse(productId)
                            if (storeProductResponse.isSuccessful) {
                                storeProduct = storeProductResponse.successData
                            } else {
                                showErrorForInPage(storeProductResponse.failureData)
                                return@launch
                            }
                            val userProducts: List<Product>?
                            val userProductsResponse = getUserProductsResponse()
                            if (userProductsResponse.isSuccessful) {
                                userProducts = userProductsResponse.successData
                            } else {
                                showErrorForInPage(userProductsResponse.failureData)
                                return@launch
                            }

                            val productTypes: List<ProductType>?
                            val productTypesResponse = getProductTypesResponse()
                            if (productTypesResponse.isSuccessful) {
                                productTypes = productTypesResponse.successData
                            } else {
                                showErrorForInPage(productTypesResponse.failureData)
                                return@launch
                            }

                            val userProductRecommendedSize = VirtusizeUtils.findBestMatchedProductSize(userProducts = userProducts!!, storeProduct!!, productTypes = productTypes!!)
                            Log.d("productRecommendedSize", userProductRecommendedSize.toString())
                            val userBodyRecommendedSize = getUserBodyRecommendedSize(storeProduct, productTypes)
                            Log.d("bodyRecommendedSize", userBodyRecommendedSize.toString())

                            val i18nLocalization: I18nLocalization?
                            val i18nResponse = getI18nResponse()
                            if (i18nResponse.isSuccessful) {
                                i18nLocalization = i18nResponse.successData
                            } else {
                                showErrorForInPage(i18nResponse.failureData)
                                return@launch
                            }

                            for(virtusizeView in virtusizeViews) {
                                if(virtusizeView is VirtusizeInPageView) {
                                    val trimType =
                                        if (virtusizeView is VirtusizeInPageStandard) TrimType.MULTIPLELINES else TrimType.ONELINE
                                    virtusizeView.setupRecommendationText(
                                        storeProduct.getRecommendationText(i18nLocalization!!)
                                            .trimI18nText(trimType)
                                    )
                                    if (virtusizeView is VirtusizeInPageStandard) {
                                        virtusizeView.setupProductImage(
                                            params.virtusizeProduct?.imageUrl,
                                            storeProduct.cloudinaryPublicId,
                                            storeProduct.productType,
                                            storeProduct.storeProductMeta?.additionalInfo?.style
                                        )
                                    }
                                }
                            }
                            virtusizeViews.clear()
                        }
                    } ?: run {
                        showErrorForInPage(VirtusizeErrorType.InvalidProduct.virtusizeError())
                    }
                } else {
                    virtusizeViews.clear()
                }
            }
        }

        productDataCheck(productValidCheckListener, errorHandler, apiRequest)
    }

    // TODO: add comment
    private fun showErrorForInPage(error: VirtusizeError?) {
        error?.let { messageHandler.onError(it) }
        for(virtusizeView in virtusizeViews) {
            if (virtusizeView is VirtusizeInPageView) {
                virtusizeView.showErrorScreen()
            }
        }
        virtusizeViews.clear()
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
            messageHandler.onError(VirtusizeErrorType.NullVirtusizeButtonError.virtusizeError())
            throwError(errorType = VirtusizeErrorType.NullVirtusizeButtonError)
            return
        }

        virtusizeViews.add(virtusizeView)
    }

    /**
     * Gets size recommendation for a store product that would best fit a user's body.
     * @return recommended size name. If it's not available, return null
     */
    private suspend fun getUserBodyRecommendedSize(storeProduct: Product, productTypes: List<ProductType>): String? {
        if(storeProduct.isAccessory()) {
            return null
        }
        val userBodyProfileResponse = getUserBodyProfileResponse()
        if (userBodyProfileResponse.isSuccessful) {
            val bodyProfileRecommendedSizeResponse = getBodyProfileRecommendedSizeResponse(
                productTypes,
                storeProduct,
                userBodyProfileResponse.successData!!
            )
            return bodyProfileRecommendedSizeResponse.successData?.sizeName
        } else {
            userBodyProfileResponse.failureData?.let {
                messageHandler.onError(it)
            }
        }
        return null
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
            .executeAsync(apiRequest)
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
            .executeAsync(apiRequest)
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
            .executeAsync(apiRequest)
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
            .executeAsync(apiRequest)
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
                    .executeAsync(apiRequest)
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
                    .executeAsync(apiRequest)
            }
        }, object : ErrorResponseHandler{
            override fun onError(error: VirtusizeError) {
                onError(error)
            }
        })
    }

    /**
     * Gets the API response for retrieving the store product info
     * @param productId the ID of the store product
     * @return the [VirtusizeApiResponse] with the data class [StoreProduct]
     */
    internal suspend fun getStoreProductResponse(productId: Int): VirtusizeApiResponse<Product?> = withContext(IO) {
        if(productId == 0) {
            return@withContext VirtusizeApiResponse.Error(VirtusizeErrorType.NullProduct.virtusizeError())
        }
        val apiRequest = VirtusizeApi.getStoreProductInfo(productId.toString())
        return@withContext VirtusizeApiTask()
            .setJsonParser(StoreProductJsonParser())
            .setHttpURLConnection(httpURLConnection)
            .execute(apiRequest) as VirtusizeApiResponse<Product?>
    }

    /**
     * Gets the API response for retrieving the list of the product types
     */
    internal suspend fun getProductTypesResponse(): VirtusizeApiResponse<List<ProductType>?> = withContext(IO) {
        val apiRequest = VirtusizeApi.getProductTypes()
        return@withContext VirtusizeApiTask()
            .setJsonParser(ProductTypeJsonParser())
            .setHttpURLConnection(httpURLConnection)
            .execute(apiRequest) as VirtusizeApiResponse<List<ProductType>?>
    }

    /**
     * Gets the API response for fetching the i18n localization texts
     * @return the [VirtusizeApiResponse] with the data class [I18nLocalization]
     */
    internal suspend fun getI18nResponse(): VirtusizeApiResponse<I18nLocalization?> = withContext(IO) {
        val apiRequest = VirtusizeApi.getI18n(params.language ?: (VirtusizeLanguage.values().find { it.value == Locale.getDefault().language } ?: VirtusizeLanguage.EN))
        VirtusizeApiTask()
            .setJsonParser(I18nLocalizationJsonParser(context, params.language))
            .setHttpURLConnection(httpURLConnection)
            .execute(apiRequest) as VirtusizeApiResponse<I18nLocalization?>
    }

    /**
     * Gets the API response for retrieving a list of user products
     * @return the [VirtusizeApiResponse] with the list of [Product]
     */
    internal suspend fun getUserProductsResponse(): VirtusizeApiResponse<List<Product>?> = withContext(IO) {
        val apiRequest = VirtusizeApi.getUserProducts()
        return@withContext VirtusizeApiTask()
            .setJsonParser(UserProductJsonParser())
            .setHttpURLConnection(httpURLConnection)
            .execute(apiRequest) as VirtusizeApiResponse<List<Product>?>
    }

    /**
     * Gets the API response for retrieving the current user body profile such as age, height, weight and body measurements
     * @return the [VirtusizeApiResponse] with the data class [UserBodyProfile]
     */
    internal suspend fun getUserBodyProfileResponse(): VirtusizeApiResponse<UserBodyProfile?> = withContext(IO) {
        val apiRequest = VirtusizeApi.getUserBodyProfile()
        VirtusizeApiTask()
            .setJsonParser(UserBodyProfileJsonParser())
            .setHttpURLConnection(httpURLConnection)
            .execute(apiRequest) as VirtusizeApiResponse<UserBodyProfile?>
    }

    internal suspend fun getBodyProfileRecommendedSizeResponse(productTypes: List<ProductType>, storeProduct: Product, userBodyProfile: UserBodyProfile): VirtusizeApiResponse<BodyProfileRecommendedSize?> = withContext(IO) {
        val apiRequest = VirtusizeApi.getSize(productTypes, storeProduct, userBodyProfile)
        VirtusizeApiTask()
            .setJsonParser(BodyProfileRecommendedSizeJsonParser())
            .setHttpURLConnection(httpURLConnection)
            .execute(apiRequest) as VirtusizeApiResponse<BodyProfileRecommendedSize?>
    }
}