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
import com.virtusize.libsource.util.trimI18nText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
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
                    virtusizeRepository.updateInPageRecommendation(storeProduct, productTypes, userProductId)
                }
            } else if (event.name == VirtusizeEvents.UserAddedProduct.getEventName()) {
                CoroutineScope(Main).launch {
                    virtusizeRepository.updateInPageRecommendation(storeProduct, productTypes)
                }
            } else if (event.name == VirtusizeEvents.UserAuthData.getEventName()) {
                event.data?.let { updateUserAuthData(it) }
            } else if (event.name == VirtusizeEvents.UserLoggedIn.getEventName()) {
                CoroutineScope(Main).launch {
                    virtusizeRepository.updateUserSession()
                    virtusizeRepository.updateInPageRecommendation(storeProduct, productTypes)
                }
            } else if (event.name == VirtusizeEvents.UserLoggedOut.getEventName()) {
                sharedPreferencesHelper.storeAuthToken("")
                CoroutineScope(Main).launch {
                    virtusizeRepository.updateUserSession()
                    virtusizeRepository.updateInPageRecommendation(storeProduct, productTypes, null, true)
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

    private val virtusizePresenter = object: VirtusizePresenter {
        override fun onProductCheck(productCheck: ProductCheck) {
            for(virtusizeView in virtusizeViews) {
                virtusizeView.setup(params = params, messageHandler = messageHandler)
                virtusizeView.setupProductCheckResponseData(productCheck)
            }
        }

        override fun onProductId(productId: Int) {
            if(virtusizeViewsContainInPage()) {
                CoroutineScope(Main).launch {
                    virtusizeRepository.fetchInitialData(params, productId)
                    virtusizeRepository.updateUserSession()
                    virtusizeRepository.updateInPageRecommendation(storeProduct, productTypes)
                }
            }
        }

        override fun showErrorForInPage(error: VirtusizeError?) {
            error?.let { messageHandler.onError(it) }
            for(virtusizeView in virtusizeViews) {
                if (virtusizeView is VirtusizeInPageView) {
                    virtusizeView.showErrorScreen()
                }
            }
        }

        override fun onStoreProduct(storeProduct: Product?) {
            this@Virtusize.storeProduct = storeProduct
        }

        override fun onProductTypes(productTypes: List<ProductType>?) {
            this@Virtusize.productTypes = productTypes
        }

        override fun onI18nLocalization(i18nLocalization: I18nLocalization?) {
            this@Virtusize.i18nLocalization = i18nLocalization
        }

        override fun updateInPageRecommendation(
            userProductRecommendedSize: SizeComparisonRecommendedSize?,
            userBodyRecommendedSize: String?
        ) {
            for(virtusizeView in virtusizeViews) {
                if(virtusizeView is VirtusizeInPageView) {
                    val trimType = if (virtusizeView is VirtusizeInPageStandard) TrimType.MULTIPLELINES else TrimType.ONELINE
                    storeProduct?.apply {
                        i18nLocalization?.let { i18nLocalization ->
                            getRecommendationText(
                                i18nLocalization,
                                userProductRecommendedSize,
                                userBodyRecommendedSize
                            ).trimI18nText(trimType).let {
                                virtusizeView.setupRecommendationText(it)

                            }
                        }
                        if (virtusizeView is VirtusizeInPageStandard) {
                            clientProductImageURL = params.virtusizeProduct?.imageUrl
                            virtusizeView.setProductImages(this, userProductRecommendedSize?.bestUserProduct)
                        }
                    }
                }
            }
        }
    }

    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)

    private var virtusizeRepository: VirtusizeRepository = VirtusizeRepository(context, messageHandler, virtusizePresenter)

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
            virtusizeRepository.productDataCheck(params)
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
            virtusizeRepository.sendOrder(params, order, { _ ->
                onSuccess?.invoke()
            }, { error ->
                onError?.invoke(error)
            })
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
            virtusizeRepository.sendOrder(params, order, { data ->
                onSuccess?.onSuccess(data)
            }, { error ->
                onError?.onError(error)
            })
        }
    }

    private fun virtusizeViewsContainInPage(): Boolean {
        for(virtusizeView in virtusizeViews) {
            if(virtusizeView is VirtusizeInPageView) {
                return true
            }
        }
        return false
    }
}