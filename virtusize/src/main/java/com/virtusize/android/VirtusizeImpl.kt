package com.virtusize.android

import android.content.Context
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.SizeRecommendationType
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeEvents
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeParams
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.getEventName
import com.virtusize.android.data.local.throwError
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.network.VirtusizeApi
import com.virtusize.android.ui.VirtusizeInPageStandard
import com.virtusize.android.ui.VirtusizeInPageView
import com.virtusize.android.ui.VirtusizeView
import com.virtusize.android.util.trimI18nText
import com.virtusize.android.util.valueOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

/**
 * This is the main class that can be used by Virtusize Clients to perform all available operations related to fit check
 *
 * @param context Android Application Context
 * @param params [VirtusizeParams] that contains userId, apiKey, env and other parameters to be passed to the Virtusize web app
 */
internal class VirtusizeImpl(
    private val context: Context,
    override val params: VirtusizeParams,
) : Virtusize {
    // The getter of the display language
    override val displayLanguage: VirtusizeLanguage = params.language

    // Registered message handlers
    private val messageHandlers = mutableListOf<VirtusizeMessageHandler>()

    // The Virtusize message handler passes received errors and events to registered message handlers
    val messageHandler =
        object : VirtusizeMessageHandler {
            override fun onEvent(
                product: VirtusizeProduct,
                event: VirtusizeEvent,
            ) {
                messageHandlers.forEach { messageHandler ->
                    messageHandler.onEvent(product, event)
                }
                // Handle different user events from the web view
                when (event.name) {
                    VirtusizeEvents.UserOpenedWidget.getEventName() -> {
                        virtusizeRepository.setLastProductOnVirtusizeWebView(product.externalId)
                        CoroutineScope(Main).launch {
                            virtusizeRepository.fetchDataForInPageRecommendation(
                                shouldUpdateUserProducts = false,
                                shouldUpdateBodyProfile = false,
                            )
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    VirtusizeEvents.UserAuthData.getEventName() -> {
                        event.data?.let { data ->
                            virtusizeRepository.updateUserAuthData(data)
                        }
                    }

                    VirtusizeEvents.UserSelectedProduct.getEventName() -> {
                        val userProductId = event.data?.optInt("userProductId")
                        CoroutineScope(Main).launch {
                            virtusizeRepository.fetchDataForInPageRecommendation(
                                selectedUserProductId = userProductId,
                                shouldUpdateUserProducts = false,
                                shouldUpdateBodyProfile = false,
                            )
                            virtusizeRepository.updateInPageRecommendation(
                                type = SizeRecommendationType.CompareProduct,
                            )
                        }
                    }

                    VirtusizeEvents.UserAddedProduct.getEventName() -> {
                        CoroutineScope(Main).launch {
                            virtusizeRepository.fetchDataForInPageRecommendation(
                                shouldUpdateUserProducts = true,
                                shouldUpdateBodyProfile = false,
                            )
                            virtusizeRepository.updateInPageRecommendation(
                                type = SizeRecommendationType.CompareProduct,
                            )
                        }
                    }

                    VirtusizeEvents.UserDeletedProduct.getEventName() -> {
                        event.data?.optInt("userProductId")?.let { userProductId ->
                            virtusizeRepository.deleteUserProduct(userProductId)
                        }
                        CoroutineScope(Main).launch {
                            virtusizeRepository.fetchDataForInPageRecommendation(
                                shouldUpdateUserProducts = false,
                                shouldUpdateBodyProfile = false,
                            )
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    VirtusizeEvents.UserChangedRecommendationType.getEventName() -> {
                        // Switches the view for InPage based on user selected size recommendation type
                        var recommendationType: SizeRecommendationType? = null
                        event.data?.optString("recommendationType")?.let {
                            recommendationType = valueOf<SizeRecommendationType>(it)
                        }
                        CoroutineScope(Main).launch {
                            virtusizeRepository.updateInPageRecommendation(
                                type = recommendationType,
                            )
                        }
                    }

                    VirtusizeEvents.UserUpdatedBodyMeasurements.getEventName() -> {
                        // Updates the body recommendation size and switches the view to the body comparison
                        val sizeRecName = event.data?.optString("sizeRecName")
                        CoroutineScope(Main).launch {
                            virtusizeRepository.updateUserBodyRecommendedSize(sizeRecName)
                            virtusizeRepository.updateInPageRecommendation(
                                type = SizeRecommendationType.Body,
                            )
                        }
                    }

                    VirtusizeEvents.UserLoggedIn.getEventName() -> {
                        // Updates the user session and fetches updated user products and body profile from the server
                        CoroutineScope(Main).launch {
                            virtusizeRepository.updateUserSession()
                            virtusizeRepository.fetchDataForInPageRecommendation()
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    VirtusizeEvents.UserLoggedOut.getEventName(),
                    VirtusizeEvents.UserDeletedData.getEventName(),
                    -> {
                        // Clears user related data and updates the session,
                        // and then re-fetches user products and body profile from the server
                        CoroutineScope(Main).launch {
                            virtusizeRepository.clearUserData()
                            virtusizeRepository.updateUserSession()
                            virtusizeRepository.fetchDataForInPageRecommendation(
                                shouldUpdateUserProducts = false,
                                shouldUpdateBodyProfile = false,
                            )
                            virtusizeRepository.updateInPageRecommendation()
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
    private val virtusizePresenter =
        object : VirtusizePresenter {
            override fun onValidProductDataCheck(productWithPDC: VirtusizeProduct) {
                for (virtusizeView in virtusizeViews) {
                    virtusizeView.setProductWithProductDataCheck(productWithPDC)
                }
                if (virtusizeViewsContainInPage()) {
                    CoroutineScope(Main).launch {
                        virtusizeRepository.fetchInitialData(params.language, productWithPDC)
                        virtusizeRepository.updateUserSession(productWithPDC.externalId)
                        virtusizeRepository.fetchDataForInPageRecommendation(
                            productWithPDC.externalId,
                        )
                        virtusizeRepository.updateInPageRecommendation(productWithPDC.externalId)
                    }
                }
            }

            override fun hasInPageError(
                externalProductId: String?,
                error: VirtusizeError?,
            ) {
                error?.let { messageHandler.onError(it) }
                for (virtusizeView in virtusizeViews) {
                    if (virtusizeView is VirtusizeInPageView) {
                        virtusizeView.showInPageError(externalProductId)
                    }
                }
            }

            override fun gotSizeRecommendations(
                externalProductId: String,
                userProductRecommendedSize: SizeComparisonRecommendedSize?,
                userBodyRecommendedSize: String?,
            ) {
                val storeProduct = virtusizeRepository.getProductBy(externalProductId)
                for (virtusizeView in virtusizeViews) {
                    if (virtusizeView is VirtusizeInPageView) {
                        storeProduct?.apply {
                            virtusizeRepository.i18nLocalization?.let { i18nLocalization ->
                                val trimType =
                                    if (virtusizeView is VirtusizeInPageStandard) {
                                        I18nLocalization.TrimType.MULTIPLELINES
                                    } else {
                                        I18nLocalization.TrimType.ONELINE
                                    }
                                val recommendationText =
                                    getRecommendationText(
                                        i18nLocalization,
                                        userProductRecommendedSize,
                                        userBodyRecommendedSize,
                                    ).trimI18nText(trimType)
                                virtusizeView.setRecommendationText(
                                    externalProductId,
                                    recommendationText,
                                )
                            }
                            if (virtusizeView is VirtusizeInPageStandard) {
                                virtusizeView.setProductImages(
                                    this,
                                    userProductRecommendedSize?.bestUserProduct,
                                )
                            }
                        }
                    }
                }
            }
        }

    private val virtusizeRepository: VirtusizeRepository =
        VirtusizeRepository(context, messageHandler, virtusizePresenter)

    // TODO: Remove the array and find a way to have callbacks inside the VirtusizeView
    // This variable holds the Virtusize view that clients use on their application
    private val virtusizeViews = mutableSetOf<VirtusizeView>()

    init {
        // Virtusize API for building API requests
        VirtusizeApi.init(
            env = params.environment,
            key = params.apiKey!!,
            userId = params.externalUserId ?: "",
        )
    }

    /**
     * @see Virtusize.setUserId
     */
    override fun setUserId(userId: String) {
        VirtusizeApi.updateUserId(userId)
        for (virtusizeView in virtusizeViews) {
            virtusizeView.virtusizeParams.externalUserId = userId
        }
    }

    /**
     * @see Virtusize.registerMessageHandler
     */
    override fun registerMessageHandler(messageHandler: VirtusizeMessageHandler) {
        messageHandlers.add(messageHandler)
    }

    /**
     * @see Virtusize.unregisterMessageHandler
     */
    override fun unregisterMessageHandler(messageHandler: VirtusizeMessageHandler) {
        messageHandlers.remove(messageHandler)
    }

    /**
     * @see Virtusize.load
     */
    override fun load(virtusizeProduct: VirtusizeProduct) {
        CoroutineScope(Main).launch {
            virtusizeRepository.productDataCheck(virtusizeProduct)
        }
    }

    /**
     * @see Virtusize.setupVirtusizeView
     */
    override fun setupVirtusizeView(
        virtusizeView: VirtusizeView?,
        product: VirtusizeProduct,
    ) {
        // Throws VirtusizeError.NullVirtusizeButtonError error if button is null
        if (virtusizeView == null) {
            VirtusizeErrorType.NullVirtusizeViewError.throwError()
            return
        }

        virtusizeView.initialSetup(
            product = product,
            params = params,
            messageHandler = messageHandler,
        )

        virtusizeViews.add(virtusizeView)
    }

    /**
     * @see Virtusize.sendOrder
     */
    override fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: (() -> Unit)?,
        onError: ((VirtusizeError) -> Unit)?,
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
     * @see Virtusize.sendOrder
     */
    override fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: SuccessResponseHandler?,
        onError: ErrorResponseHandler?,
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
        for (virtusizeView in virtusizeViews) {
            if (virtusizeView is VirtusizeInPageView) {
                return true
            }
        }
        return false
    }
}