package com.virtusize.libsource

import android.content.Context
import android.view.View
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.parsers.I18nLocalizationJsonParser.TrimType
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.network.VirtusizeApiTask
import com.virtusize.libsource.ui.VirtusizeInPageStandard
import com.virtusize.libsource.ui.VirtusizeInPageView
import com.virtusize.libsource.ui.VirtusizeView
import com.virtusize.libsource.util.trimI18nText
import com.virtusize.libsource.util.valueOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

/**
 * This is the main class that can be used by Virtusize Clients to perform all available operations related to fit check
 *
 * @param context Android Application Context
 * @param params [VirtusizeParams] that contains userId, apiKey, env and other parameters to be passed to the Virtusize web app
 */
class Virtusize(
    private val context: Context,
    internal val params: VirtusizeParams
) {

    // The getter of the display language
    val displayLanguage: VirtusizeLanguage
        get() = params.language

    // Registered message handlers
    private val messageHandlers = mutableListOf<VirtusizeMessageHandler>()

    // The Virtusize message handler passes received errors and events to registered message handlers
    private val messageHandler = object : VirtusizeMessageHandler {
        override fun onEvent(event: VirtusizeEvent) {
            messageHandlers.forEach { messageHandler ->
                messageHandler.onEvent(event)
            }
            // Handle different user events from the web view
            when (event.name) {
                VirtusizeEvents.UserOpenedWidget.getEventName() -> {
                    CoroutineScope(Main).launch {
                        virtusizeRepository.fetchDataForInPageRecommendation(
                            shouldUpdateUserProducts = false
                        )
                    }
                }
                VirtusizeEvents.UserAuthData.getEventName() -> {
                    event.data?.let { data ->
                        virtusizeRepository.updateUserAuthData(data)
                    }
                }
                VirtusizeEvents.UserSelectedProduct.getEventName() -> {
                    // Filters userProducts by the selected product ID to get userProductRecommendedSize
                    val userProductId = event.data?.optInt("userProductId")
                    CoroutineScope(Main).launch {
                        virtusizeRepository.fetchDataForInPageRecommendation(
                            shouldUpdateUserProducts = false,
                            selectedUserProductId = userProductId
                        )
                        virtusizeRepository.switchInPageRecommendation(SizeRecommendationType.compareProduct)
                    }
                }
                VirtusizeEvents.UserAddedProduct.getEventName() -> {
                    // Gets updated user products from the server,
                    // and then filters userProducts by the selected product ID to get userProductRecommendedSize
                    val userProductId = event.data?.optInt("userProductId")
                    CoroutineScope(Main).launch {
                        virtusizeRepository.fetchDataForInPageRecommendation(
                            shouldUpdateUserProducts = true,
                            selectedUserProductId = userProductId
                        )
                        virtusizeRepository.switchInPageRecommendation(SizeRecommendationType.compareProduct)
                    }
                }
                VirtusizeEvents.UserChangedRecommendationType.getEventName() -> {
                    // Switches the view for InPage based on user selected size recommendation type
                    var recommendationType: SizeRecommendationType? = null
                    event.data?.optString("recommendationType")?.let {
                        recommendationType = valueOf<SizeRecommendationType>(it)
                    }
                    CoroutineScope(Main).launch {
                        virtusizeRepository.switchInPageRecommendation(recommendationType)
                    }
                }
                VirtusizeEvents.UserUpdatedBodyMeasurements.getEventName() -> {
                    // Updates the body recommendation size and switches the view to the body comparison
                    val sizeRecName = event.data?.optString("sizeRecName")
                    CoroutineScope(Main).launch {
                        virtusizeRepository.updateUserBodyRecommendedSize(sizeRecName)
                        virtusizeRepository.switchInPageRecommendation(SizeRecommendationType.body)
                    }
                }
                VirtusizeEvents.UserLoggedIn.getEventName() -> {
                    // Updates the user session and fetches updated user prodcuts and body profile from the server
                    CoroutineScope(Main).launch {
                        virtusizeRepository.updateUserSession()
                        virtusizeRepository.fetchDataForInPageRecommendation()
                        virtusizeRepository.switchInPageRecommendation()
                    }
                }
                VirtusizeEvents.UserLoggedOut.getEventName(), VirtusizeEvents.UserDeletedData.getEventName() -> {
                    // Clears user related data and updates the session,
                    // and then re-fetches user products and body profile from the server
                    CoroutineScope(Main).launch {
                        virtusizeRepository.clearUserData()
                        virtusizeRepository.updateUserSession()
                        virtusizeRepository.fetchDataForInPageRecommendation(
                            shouldUpdateUserProducts = false
                        )
                        virtusizeRepository.switchInPageRecommendation()
                    }
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
                    virtusizeRepository.fetchDataForInPageRecommendation()
                    virtusizeRepository.switchInPageRecommendation()
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

        override fun gotSizeRecommendations(
            userProductRecommendedSize: SizeComparisonRecommendedSize?,
            userBodyRecommendedSize: String?
        ) {
            for(virtusizeView in virtusizeViews) {
                if(virtusizeView is VirtusizeInPageView) {
                    virtusizeRepository.storeProduct?.apply {
                        virtusizeRepository.i18nLocalization?.let { i18nLocalization ->
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

    // This variable holds the Virtusize view that clients use on their application
    private var virtusizeViews = mutableSetOf<VirtusizeView>()

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

        params.virtusizeProduct = virtusizeProduct
        virtusizeRepository.virtusizeProduct = virtusizeProduct

        CoroutineScope(Main).launch {
            virtusizeRepository.productDataCheck(virtusizeProduct)
        }
    }

    /**
     * Sets up the Virtusize view by passing the VirtusizeView
     * @param virtusizeView VirtusizeView that is being set up
     * @throws IllegalArgumentException throws an error if VirtusizeButton is null or the image URL of VirtusizeProduct is invalid
     */
    fun setupVirtusizeView(virtusizeView: VirtusizeView?) {
        // Throws NullProduct error if the product is not set yet
        if (virtusizeRepository.virtusizeProduct == null) {
            VirtusizeErrorType.NullProduct.throwError()
            return
        }

        // Throws VirtusizeError.NullVirtusizeButtonError error if button is null
        if (virtusizeView == null) {
            VirtusizeErrorType.NullVirtusizeViewError.throwError()
            return
        }

        (virtusizeView as? View)?.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener{
            override fun onViewAttachedToWindow(v: View?) {}

            override fun onViewDetachedFromWindow(v: View?) {
                val detachedVirtusizeView = v as? VirtusizeView
                if (virtusizeViews.contains(detachedVirtusizeView))
                    virtusizeViews.remove(detachedVirtusizeView)
            }
        })

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

    /**
     * Returns a boolean value to tell whether the VirtusizeView array contains at least one VirtusizeInPageView
     */
    private fun virtusizeViewsContainInPage(): Boolean {
        for(virtusizeView in virtusizeViews) {
            if(virtusizeView is VirtusizeInPageView) {
                return true
            }
        }
        return false
    }
}