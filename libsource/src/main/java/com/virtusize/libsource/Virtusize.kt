package com.virtusize.libsource

import android.content.Context
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.parsers.*
import com.virtusize.libsource.data.parsers.I18nLocalizationJsonParser.TrimType
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.network.*
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.network.VirtusizeApiTask
import com.virtusize.libsource.ui.VirtusizeInPageStandard
import com.virtusize.libsource.ui.VirtusizeInPageView
import com.virtusize.libsource.ui.VirtusizeView
import com.virtusize.libsource.util.VirtusizeUtils
import com.virtusize.libsource.util.trimI18nText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
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
            // Handle different user events from the web view
            if (event.name == VirtusizeEvents.UserSelectedProduct.getEventName() || event.name == VirtusizeEvents.UserOpenedPanelCompare.getEventName()) {
                val userProductId = event.data?.optInt("userProductId")
                CoroutineScope(Main).launch {
                    updateInPageRecommendation(userProductId)
                }
            } else if (event.name == VirtusizeEvents.UserAddedProduct.getEventName()) {
                CoroutineScope(Main).launch {
                    updateInPageRecommendation()
                }
            } else if (event.name == VirtusizeEvents.UserAuthData.getEventName()) {
                event.data?.let { updateUserAuthData(it) }
            } else if (event.name == VirtusizeEvents.UserLoggedIn.getEventName()) {
                CoroutineScope(Main).launch {
                    updateUserSession()
                    updateInPageRecommendation()
                }
            } else if (event.name == VirtusizeEvents.UserLoggedOut.getEventName()) {
                sharedPreferencesHelper.storeAuthToken("")
                CoroutineScope(Main).launch {
                    updateUserSession()
                    updateInPageRecommendation(null, true)
                }
            }
        }

        override fun onError(error: VirtusizeError) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onError(error)
            }
        }

        /**
         * Updates the browser ID and the auth token from the data of the event user-auth-data
         * @param eventJsonObject the event data in JSONObject
         */
        private fun updateUserAuthData(eventJsonObject: JSONObject) {
            try {
                val userAutoData = UserAuthDataJsonParser().parse(eventJsonObject)
                sharedPreferencesHelper.storeBrowserId(userAutoData?.bid)
                sharedPreferencesHelper.storeAuthToken(userAutoData?.auth)
            } catch (e: JSONException) {
                messageHandlers.forEach { messageHandler ->
                    messageHandler.onError(VirtusizeErrorType.JsonParsingError.virtusizeError(e.localizedMessage))
                }
            }
        }
    }

    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)

    // This variable is the instance of VirtusizeAPIService to handle Virtusize API requests
    private var virtusizeAPIService = VirtusizeAPIService.getInstance(context, messageHandler)

    // The dispatcher that determines what thread the corresponding coroutine uses for its execution
    private var coroutineDispatcher: CoroutineDispatcher = IO

    // This variable holds the product information from the client and the product data check API
    private var virtusizeProduct: VirtusizeProduct? = null

    // This variable holds the Virtusize view that clients use on their application
    private var virtusizeViews = mutableSetOf<VirtusizeView>()

    // This variable holds the list of product types from the Virtusize API
    private var productTypes: List<ProductType>? = null

    // This variable holds the store product from the Virtusize API
    private var storeProduct: Product? = null

    // This variable holds the i18n localization texts
    private var i18nLocalization: I18nLocalization? = null

    init {
        // Virtusize API for building API requests
        VirtusizeApi.init(
            env = params.environment,
            key = params.apiKey!!,
            userId = params.externalUserId ?: ""
        )
    }

    /**
     * Use this function to set up the user ID in the app when the user is logged in/out
     * @param userId the user ID that is unique from the client system
     */
    fun setUserId(userId: String) {
        VirtusizeApi.updateUserId(userId)
        params.externalUserId = userId
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
     * Sets the Coroutine dispatcher
     * @param dispatcher an instance of [CoroutineDispatcher]
     */
    internal fun setCoroutineDispatcher(dispatcher: CoroutineDispatcher) {
        this.coroutineDispatcher = dispatcher
    }

    /**
     * Sets up the product for the product detail page
     *
     * @param virtusizeProduct VirtusizeProduct that is being set to the VirtusizeView
     */
    fun setupVirtusizeProduct(virtusizeProduct: VirtusizeProduct?) {
        // Throws NullProduct error if the product is null
        if (virtusizeProduct == null) {
            VirtusizeErrorType.NullProduct.throwError()
            return
        }
        this.virtusizeProduct = virtusizeProduct

        params.bid = sharedPreferencesHelper.getBrowserId()
        params.virtusizeProduct = virtusizeProduct
        CoroutineScope(Main).launch {
            val productCheckResponse = virtusizeAPIService.productDataCheck(virtusizeProduct)
            if (productCheckResponse.isSuccessful) {
                val productCheck = productCheckResponse.successData!!
                var virtusizeViewsContainInPage = false
                for(virtusizeView in virtusizeViews) {
                    if(virtusizeView is VirtusizeInPageView) {
                        virtusizeViewsContainInPage = true
                    }
                    virtusizeView.setup(params = params, messageHandler = messageHandler)
                    virtusizeView.setupProductCheckResponseData(productCheck)
                }

                // Send API Event UserSawProduct
                val sendEventResponse = virtusizeAPIService.sendEvent(
                    event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
                    withDataProduct = productCheck
                )
                if(sendEventResponse.isSuccessful) {
                    messageHandler.onEvent(VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()))
                }

                productCheck.data?.apply {
                    if (validProduct) {
                        if (fetchMetaData) {
                            if (params.virtusizeProduct?.imageUrl != null) {
                                // If image URL is valid, send image URL to server
                                val sendProductImageResponse = virtusizeAPIService.sendProductImageToBackend(product = virtusizeProduct)
                                if (!sendProductImageResponse.isSuccessful) {
                                    sendProductImageResponse.failureData?.let { messageHandler.onError(it) }
                                }
                            } else {
                                VirtusizeErrorType.ImageUrlNotValid.throwError()
                            }
                        }
                        // Send API Event UserSawWidgetButton
                        virtusizeAPIService.sendEvent(
                            event = VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()),
                            withDataProduct = productCheck
                        )
                        if(sendEventResponse.isSuccessful) {
                            messageHandler.onEvent(VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()))
                        }
                    }
                }

                if(virtusizeViewsContainInPage) {
                    productCheck.data?.productDataId?.let { productId ->
                        CoroutineScope(Main).launch {
                            val storeProductResponse = virtusizeAPIService.getStoreProduct(productId)
                            if (storeProductResponse.isSuccessful) {
                                storeProduct = storeProductResponse.successData
                            } else {
                                showErrorForInPage(storeProductResponse.failureData)
                                return@launch
                            }

                            val productTypesResponse = virtusizeAPIService.getProductTypes()
                            if (productTypesResponse.isSuccessful) {
                                productTypes = productTypesResponse.successData
                            } else {
                                showErrorForInPage(productTypesResponse.failureData)
                                return@launch
                            }

                            val i18nResponse = virtusizeAPIService.getI18n(params)
                            if (i18nResponse.isSuccessful) {
                                i18nLocalization = i18nResponse.successData
                            } else {
                                showErrorForInPage(i18nResponse.failureData)
                                return@launch
                            }

                            updateUserSession()
                            updateInPageRecommendation()
                        }
                    } ?: run {
                        showErrorForInPage(VirtusizeErrorType.InvalidProduct.virtusizeError())
                    }
                }
            } else {
                productCheckResponse.failureData?.let { messageHandler.onError(it) }
            }
        }
    }

    /**
     * Updates the user session by calling the session API
     */
    private suspend fun updateUserSession() {
        val userSessionInfoResponse = virtusizeAPIService.getUserSessionInfo()
        if (userSessionInfoResponse.isSuccessful) {
            sharedPreferencesHelper.storeSessionData(userSessionInfoResponse.successData!!.userSessionResponse)
            sharedPreferencesHelper.storeAccessToken(userSessionInfoResponse.successData!!.accessToken)
            if(userSessionInfoResponse.successData!!.authToken.isNotBlank()) {
                sharedPreferencesHelper.storeAuthToken(userSessionInfoResponse.successData!!.authToken)
            }
        } else {
            showErrorForInPage(userSessionInfoResponse.failureData)
            return
        }
    }

    /**
     * Updates the recommendation for InPage
     * @param selectedUserProductId the selected product Id from the web view to decide a specific user product to compare with the store product
     * @param ignoreUserData pass the boolean vale to determine whether to ignore the API requests that is related to the user data
     */
    private suspend fun updateInPageRecommendation(selectedUserProductId: Int? = null, ignoreUserData: Boolean = false) {
        var userProducts: List<Product>? = null
        var userProductRecommendedSize: SizeComparisonRecommendedSize? = null
        var userBodyRecommendedSize: String? = null
        if(!ignoreUserData) {
            val userProductsResponse = virtusizeAPIService.getUserProducts()
            if (userProductsResponse.isSuccessful) {
                userProducts = userProductsResponse.successData
            } else if(userProductsResponse.failureData?.code != HttpURLConnection.HTTP_NOT_FOUND) {
                showErrorForInPage(userProductsResponse.failureData)
                return
            }

            userProductRecommendedSize = VirtusizeUtils.findBestFitProductSize(
                userProducts = if(selectedUserProductId != null) userProducts?.filter { it.id == selectedUserProductId } else userProducts,
                storeProduct = storeProduct!!,
                productTypes = productTypes!!
            )
            userBodyRecommendedSize = getUserBodyRecommendedSize(storeProduct!!, productTypes!!)
        }

        for(virtusizeView in virtusizeViews) {
            if(virtusizeView is VirtusizeInPageView) {
                val trimType =
                    if (virtusizeView is VirtusizeInPageStandard) TrimType.MULTIPLELINES else TrimType.ONELINE
                virtusizeView.setupRecommendationText(
                    storeProduct!!.getRecommendationText(
                        i18nLocalization!!,
                        userProductRecommendedSize,
                        userBodyRecommendedSize
                    ).trimI18nText(trimType)
                )
                if (virtusizeView is VirtusizeInPageStandard) {
                    storeProduct!!.clientProductImageURL = params.virtusizeProduct?.imageUrl
                    virtusizeView.setProductImages(storeProduct!!, userProductRecommendedSize?.bestUserProduct)
                }
            }
        }
    }

    /**
     * Shows the error for InPage
     * @param error pass a [VirtusizeError] for the messageHandler
     */
    private fun showErrorForInPage(error: VirtusizeError?) {
        error?.let { messageHandler.onError(it) }
        for(virtusizeView in virtusizeViews) {
            if (virtusizeView is VirtusizeInPageView) {
                virtusizeView.showErrorScreen()
            }
        }
    }

    /**
     * Sets up the Virtusize view by passing the VirtusizeView
     * @param virtusizeView VirtusizeView that is being set up
     * @throws IllegalArgumentException throws an error if VirtusizeButton is null or the image URL of VirtusizeProduct is invalid
     */
    fun setupVirtusizeView(virtusizeView: VirtusizeView?) {
        // Throws NullProduct error if the product is not set yet
        if (virtusizeProduct == null) {
            VirtusizeErrorType.NullProduct.throwError()
            return
        }

        // Throws VirtusizeError.NullVirtusizeButtonError error if button is null
        if (virtusizeView == null) {
            VirtusizeErrorType.NullVirtusizeViewError.throwError()
            return
        }

        virtusizeViews.add(virtusizeView)
    }

    /**
     * Gets size recommendation for a store product that would best fit a user's body.
     * @param storeProduct the store product
     * @param productTypes a list of product types
     * @return recommended size name. If it's not available, return null
     */
    private suspend fun getUserBodyRecommendedSize(storeProduct: Product, productTypes: List<ProductType>): String? {
        if(storeProduct.isAccessory()) {
            return null
        }
        val userBodyProfileResponse = virtusizeAPIService.getUserBodyProfile()
        if (userBodyProfileResponse.isSuccessful) {
            val bodyProfileRecommendedSizeResponse = virtusizeAPIService.getBodyProfileRecommendedSize(
                productTypes,
                storeProduct,
                userBodyProfileResponse.successData!!
            )
            return bodyProfileRecommendedSizeResponse.successData?.sizeName
        } else if(userBodyProfileResponse.failureData?.code != HttpURLConnection.HTTP_NOT_FOUND) {
            userBodyProfileResponse.failureData?.let {
                messageHandler.onError(it)
            }
        }
        return null
    }

    /**
     * Sends an order to the Virtusize server for Kotlin apps
     * @param order [VirtusizeOrder]
     * @param onSuccess the optional success callback to notify [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: (() -> Unit)? = null,
        onError: ((VirtusizeError) -> Unit)? = null
    ) {
        CoroutineScope(Main).launch {
            val storeInfoResponse = virtusizeAPIService.getStoreInfo()
            if (storeInfoResponse.isSuccessful) {
                val sendOrderResponse = virtusizeAPIService.sendOrder(params, storeInfoResponse.successData, order)
                if (sendOrderResponse.isSuccessful) {
                    onSuccess?.invoke()
                } else {
                    sendOrderResponse.failureData?.let { onError?.invoke(it) }
                }
            } else {
                storeInfoResponse.failureData?.let { onError?.invoke(it) }
            }
        }
    }

    /**
     * Sends an order to the Virtusize server for Java apps
     * @param order [VirtusizeOrder]
     * @param onSuccess the optional success callback to pass the [Store] from the response when [VirtusizeApiTask] is successful
     * @param onError the optional error callback to get the [VirtusizeError] in the API task
     */
    fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: SuccessResponseHandler? = null,
        onError: ErrorResponseHandler? = null
    ) {
        CoroutineScope(Main).launch {
            val storeInfoResponse = virtusizeAPIService.getStoreInfo()
            if (storeInfoResponse.isSuccessful) {
                val sendOrderResponse = virtusizeAPIService.sendOrder(params, storeInfoResponse.successData, order)
                if (sendOrderResponse.isSuccessful) {
                    onSuccess?.onSuccess(sendOrderResponse.successData)
                } else {
                    sendOrderResponse.failureData?.let { onError?.onError(it) }
                }
            } else {
                storeInfoResponse.failureData?.let { onError?.onError(it) }
            }
        }
    }
}