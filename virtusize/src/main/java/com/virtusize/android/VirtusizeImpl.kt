package com.virtusize.android

import android.content.Context
import androidx.lifecycle.AtomicReference
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.SizeRecommendationType
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeParams
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.throwError
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.network.VirtusizeAPIService
import com.virtusize.android.network.VirtusizeApi
import com.virtusize.android.ui.VirtusizeInPageStandard
import com.virtusize.android.ui.VirtusizeInPageView
import com.virtusize.android.ui.VirtusizeView
import com.virtusize.android.util.trimI18nText
import com.virtusize.android.util.valueOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
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
                when (event) {
                    is VirtusizeEvent.UserAddedProduct -> {
                        scope.launch {
                            virtusizeRepository.fetchDataForInPageRecommendation(
                                shouldUpdateUserProducts = true,
                                shouldUpdateBodyProfile = false,
                            )
                            virtusizeRepository.updateInPageRecommendation(
                                type = SizeRecommendationType.CompareProduct,
                            )
                        }
                    }

                    is VirtusizeEvent.UserAuthData -> {
                        event.data?.let { data ->
                            virtusizeRepository.updateUserAuthData(data)
                        }
                    }

                    is VirtusizeEvent.UserChangedRecommendationType -> {
                        // Switches the view for InPage based on user selected size recommendation type
                        var recommendationType: SizeRecommendationType? = null
                        event.data?.optString("recommendationType")?.let {
                            recommendationType = valueOf<SizeRecommendationType>(it)
                        }
                        scope.launch {
                            virtusizeRepository.updateInPageRecommendation(
                                type = recommendationType,
                            )
                        }
                    }

                    is VirtusizeEvent.UserLoggedOut, is VirtusizeEvent.UserDeletedData -> {
                        // Clears user related data and updates the session,
                        // and then re-fetches user products and body profile from the server
                        scope.launch {
                            virtusizeRepository.clearUserData()
                            virtusizeRepository.updateUserSession()
                            virtusizeRepository.fetchDataForInPageRecommendation()
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    is VirtusizeEvent.UserDeletedProduct -> {
                        event.data?.optInt("userProductId")?.let { userProductId ->
                            virtusizeRepository.deleteUserProduct(userProductId)
                        }
                        scope.launch {
                            virtusizeRepository.fetchDataForInPageRecommendation(
                                shouldUpdateUserProducts = false,
                                shouldUpdateBodyProfile = false,
                            )
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    is VirtusizeEvent.UserLoggedIn -> {
                        // Updates the user session and fetches updated user products and body profile from the server
                        scope.launch {
                            virtusizeRepository.updateUserSession()
                            virtusizeRepository.fetchDataForInPageRecommendation()
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    is VirtusizeEvent.UserOpenedWidget -> {
                        virtusizeRepository.setLastProductOnVirtusizeWebView(product.externalId)
                        scope.launch {
                            virtusizeRepository.fetchDataForInPageRecommendation(
                                shouldUpdateUserProducts = false,
                                shouldUpdateBodyProfile = false,
                            )
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    is VirtusizeEvent.UserSelectedProduct -> {
                        val userProductId = event.data?.optInt("userProductId")
                        scope.launch {
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

                    is VirtusizeEvent.UserUpdatedBodyMeasurements -> {
                        invalidateCurrentProduct()
                        // Updates the body recommendation size and switches the view to the body comparison
                        val sizeRecName = event.data?.optString("sizeRecName")
                        scope.launch {
                            virtusizeRepository.updateUserBodyRecommendedSize(sizeRecName)
                            virtusizeRepository.updateInPageRecommendation(
                                type = SizeRecommendationType.Body,
                            )
                        }
                    }

                    is VirtusizeEvent.UserClosedWidget ->
                        scope.launch {
                            virtusizeRepository.updateUserSession()
                        }

                    is VirtusizeEvent.UserCreatedSilhouette,
                    is VirtusizeEvent.UserSawProduct,
                    is VirtusizeEvent.UserSawWidgetButton,
                    is VirtusizeEvent.UssrClickedStart,
                    is VirtusizeEvent.Undefined,
                    -> Unit
                }
            }

            override fun onError(error: VirtusizeError) {
                messageHandlers.forEach { messageHandler ->
                    messageHandler.onError(error)
                }
            }
        }

    /**
     * The current product external ID
     */
    private var currentProductExternalId: AtomicReference<String?> = AtomicReference()

    private fun invalidateCurrentProduct() {
        currentProductExternalId.set(null)
    }

    /**
     * The VirtusizePresenter handles the data passed from the actions of VirtusizeRepository
     */
    private val virtusizePresenter =
        object : VirtusizePresenter {
            override fun onValidProductCheck(productWithPCDData: VirtusizeProduct) {
                // Update VirtusizeViews with product data
                virtusizeViews.forEach { virtusizeView ->
                    virtusizeView.setProductWithProductCheckData(productWithPCDData)
                }

                // Check if product ID has changed
                val newExternalProductId = productWithPCDData.externalId
                val shouldReloadProduct = currentProductExternalId.getAndSet(newExternalProductId) != newExternalProductId
                if (virtusizeViewsContainInPage()) {
                    scope.launch {
                        if (shouldReloadProduct) {
                            // Those two data fetches are independent and can be run in parallel
                            awaitAll(
                                async { virtusizeRepository.fetchInitialData(params.language, productWithPCDData) },
                                async { virtusizeRepository.updateUserSession(newExternalProductId) },
                            )

                            virtusizeRepository.fetchDataForInPageRecommendation(newExternalProductId)
                        }
                        // update recommendations anyway
                        virtusizeRepository.updateInPageRecommendation(newExternalProductId)
                    }
                }
            }

            override fun hasInPageError(
                externalProductId: String?,
                error: VirtusizeError?,
            ) {
                invalidateCurrentProduct()
                error?.let { messageHandler.onError(it) }
                virtusizeViews
                    .filterIsInstance<VirtusizeInPageView>()
                    .forEach { virtusizeView ->
                        virtusizeView.showInPageError(externalProductId)
                    }
            }

            override fun gotSizeRecommendations(
                externalProductId: String,
                userProductRecommendedSize: SizeComparisonRecommendedSize?,
                userBodyRecommendedSize: String?,
            ) {
                val storeProduct = virtusizeRepository.getProductBy(externalProductId)
                virtusizeViews
                    .filterIsInstance<VirtusizeInPageView>()
                    .forEach { virtusizeView ->
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
                                        context = context,
                                        i18nLocalization = i18nLocalization,
                                        sizeComparisonRecommendedSize = userProductRecommendedSize,
                                        bodyProfileRecommendedSizeName = userBodyRecommendedSize,
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

    override val virtusizeRepository: VirtusizeRepository =
        VirtusizeRepository(
            context,
            messageHandler,
            VirtusizeAPIService.getInstance(context, messageHandler),
            virtusizePresenter,
        )

    // TODO: Remove the array and find a way to have callbacks inside the VirtusizeView
    // This variable holds the Virtusize view that clients use on their application
    private val virtusizeViews = mutableSetOf<VirtusizeView>()

    init {
        // Virtusize API for building API requests
        VirtusizeApi.init(
            env = params.environment,
            key = params.apiKey!!,
            userId = params.externalUserId ?: "",
            branch = params.branch,
        )
    }

    /**
     * @see Virtusize.setUserId
     */
    override fun setUserId(userId: String) {
        VirtusizeApi.setUserId(userId)
        virtusizeViews.forEach { virtusizeView ->
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
        scope.launch {
            productCheck(virtusizeProduct)
        }
    }

    /**
     * @see Virtusize.productCheck
     */
    override suspend fun productCheck(virtusizeProduct: VirtusizeProduct): Boolean = virtusizeRepository.productCheck(virtusizeProduct)

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

    override fun cleanupVirtusizeView(virtusizeView: VirtusizeView) {
        virtusizeViews.remove(virtusizeView)
    }

    /**
     * @see Virtusize.sendOrder
     */
    override fun sendOrder(
        order: VirtusizeOrder,
        onSuccess: (() -> Unit)?,
        onError: ((VirtusizeError) -> Unit)?,
    ) {
        scope.launch {
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
        scope.launch {
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
