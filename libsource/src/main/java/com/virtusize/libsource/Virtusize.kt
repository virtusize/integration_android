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
            // TODO: Fix Handling events from the web view
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
                event.data?.let { virtusizeRepository.updateUserAuthData(it) }
            } else if (event.name == VirtusizeEvents.UserLoggedIn.getEventName()) {
                CoroutineScope(Main).launch {
                    virtusizeRepository.updateUserSession()
                    virtusizeRepository.updateInPageRecommendation(storeProduct, productTypes)
                }
            } else if (event.name == VirtusizeEvents.UserLoggedOut.getEventName()) {
                CoroutineScope(Main).launch {
                    virtusizeRepository.updateUserSession(true)
                    virtusizeRepository.updateInPageRecommendation(storeProduct, productTypes, null, true)
                }
            }
        }

        override fun onError(error: VirtusizeError) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onError(error)
            }
        }
    }

    /**
     * The VirtusizePresenter handles the data passed from the actions of VirtusizeRepository
     */
    private val virtusizePresenter = object: VirtusizePresenter {
        override fun finishedProductCheck(productCheck: ProductCheck) {
            for(virtusizeView in virtusizeViews) {
                virtusizeView.setup(params = params, messageHandler = messageHandler)
                virtusizeView.setupProductCheckResponseData(productCheck)
            }
        }

        override fun onValidProductId(productId: Int) {
            if(virtusizeViewsContainInPage()) {
                CoroutineScope(Main).launch {
                    virtusizeRepository.fetchInitialData(params.language, productId)
                    virtusizeRepository.updateUserSession()
                    virtusizeRepository.updateInPageRecommendation(storeProduct, productTypes)
                }
            }
        }

        override fun hasInPageError(error: VirtusizeError?) {
            error?.let { messageHandler.onError(it) }
            for(virtusizeView in virtusizeViews) {
                if (virtusizeView is VirtusizeInPageView) {
                    virtusizeView.showErrorScreen()
                }
            }
        }

        override fun gotInitialData(
            storeProduct: Product,
            productTypes: List<ProductType>,
            i18nLocalization: I18nLocalization
        ) {
            this@Virtusize.storeProduct = storeProduct
            this@Virtusize.productTypes = productTypes
            this@Virtusize.i18nLocalization = i18nLocalization
        }

        override fun gotSizeRecommendations(
            userProductRecommendedSize: SizeComparisonRecommendedSize?,
            userBodyRecommendedSize: String?
        ) {
            for(virtusizeView in virtusizeViews) {
                if(virtusizeView is VirtusizeInPageView) {
                    storeProduct?.apply {
                        i18nLocalization?.let { i18nLocalization ->
                            val trimType = if (virtusizeView is VirtusizeInPageStandard) TrimType.MULTIPLELINES else TrimType.ONELINE
                            val recommendationText = getRecommendationText(
                                i18nLocalization,
                                userProductRecommendedSize,
                                userBodyRecommendedSize
                            ).trimI18nText(trimType)
                            virtusizeView.setupRecommendationText(recommendationText)
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