package com.virtusize.android

import android.content.Context
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.SizeRecommendationType
import com.virtusize.android.data.local.VirtusizeEnvironment
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeParams
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.throwError
import com.virtusize.android.data.local.virtusizeRegion
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.network.VirtusizeAPIService
import com.virtusize.android.network.VirtusizeApi
import com.virtusize.android.ui.VirtusizeButton
import com.virtusize.android.ui.VirtusizeInPageStandard
import com.virtusize.android.ui.VirtusizeInPageView
import com.virtusize.android.ui.VirtusizeView
import com.virtusize.android.util.trimI18nText
import com.virtusize.android.util.valueOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
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

    // Sentry store ID helper
    private val sentryStoreId: String?
        get() = VirtusizeApi.currentStoreId?.value?.toString()

    // Job to track and cancel previous load operation
    private var loadJob: Job? = null

    // Tracks the last product ID loaded to detect product changes for session tracking
    private var lastLoadedProductExternalId: String? = null

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
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
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
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
                        event.data?.let { data ->
                            virtusizeRepository.updateUserAuthData(data)
                        }
                    }

                    is VirtusizeEvent.UserChangedRecommendationType -> {
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
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
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
                        // Clears user related data and updates the session,
                        // and then re-fetches user products and body profile from the server
                        scope.launch {
                            virtusizeRepository.clearUserData()
                            virtusizeRepository.updateUserSession(forceUpdate = true)
                            virtusizeRepository.fetchDataForInPageRecommendation()
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    is VirtusizeEvent.UserDeletedProduct -> {
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
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
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
                        // Updates the user session and fetches updated user products and body profile from the server
                        scope.launch {
                            virtusizeRepository.updateUserSession(forceUpdate = true)
                            virtusizeRepository.fetchDataForInPageRecommendation()
                            virtusizeRepository.updateInPageRecommendation()
                        }
                    }

                    is VirtusizeEvent.UserOpenedWidget -> {
                        VirtusizeSentryTracker.trackWebViewEvent(
                            eventName = event.name,
                            storeId = sentryStoreId,
                        )
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
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
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
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
                        // Updates the body recommendation size and switches the view to the body comparison
                        val sizeRecName = event.data?.optString("sizeRecName")
                        scope.launch {
                            virtusizeRepository.updateUserBodyRecommendedSize(sizeRecName)
                            virtusizeRepository.updateInPageRecommendation(
                                type = SizeRecommendationType.Body,
                            )
                        }
                    }

                    is VirtusizeEvent.UserClosedWidget -> {
                        VirtusizeSentryTracker.trackWebViewEvent(
                            eventName = event.name,
                            storeId = sentryStoreId,
                        )
                        scope.launch {
                            virtusizeRepository.updateUserSession(forceUpdate = true)
                        }
                    }

                    is VirtusizeEvent.UserClickedLanguageSelector -> {
                        VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)
                        event.data?.optString("language")?.let { language ->
                            val virtusizeLanguage = VirtusizeLanguage.entries.firstOrNull { it.value == language }
                            if (virtusizeLanguage != null) {
                                setVsWidgetLanguage(virtusizeLanguage)
                            }
                        }
                    }

                    is VirtusizeEvent.UserSawProduct -> Unit

                    is VirtusizeEvent.UserCreatedSilhouette,
                    is VirtusizeEvent.UserSawWidgetButton,
                    is VirtusizeEvent.UserClickedStart,
                    is VirtusizeEvent.WidgetReady,
                    -> VirtusizeSentryTracker.trackWebViewEvent(event.name, sentryStoreId)

                    is VirtusizeEvent.Undefined -> Unit
                }
            }

            override fun onError(error: VirtusizeError) {
                messageHandlers.forEach { messageHandler ->
                    messageHandler.onError(error)
                }
                VirtusizeSentryTracker.trackError(
                    throwable = Exception(error.message),
                    storeId = sentryStoreId,
                )
            }
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
            }

            override fun hasInPageError(
                externalProductId: String?,
                error: VirtusizeError?,
            ) {
                error?.let { messageHandler.onError(it) }
                virtusizeViews
                    .filterIsInstance<VirtusizeInPageView>()
                    .forEach { virtusizeView ->
                        virtusizeView.showInPageError(externalProductId, error)
                    }
            }

            override fun gotSizeRecommendations(
                externalProductId: String,
                userProductRecommendedSize: SizeComparisonRecommendedSize?,
                userBodyRecommendedSize: String?,
                userBodyWillFit: Boolean?,
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
                                        bodyProfileWillFit = userBodyWillFit,
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
            serviceEnv = params.serviceEnvironment,
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
     * @see Virtusize.changeStore
     */
    override fun changeStore(
        apiKey: String,
        env: VirtusizeEnvironment,
    ) {
        VirtusizeApi.setApiKey(apiKey)
        VirtusizeApi.setEnvironment(env)
        virtusizeViews.forEach { virtusizeView ->
            virtusizeView.virtusizeParams.apiKey = apiKey
            virtusizeView.virtusizeParams.environment = env
            virtusizeView.virtusizeParams.region = env.virtusizeRegion()
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
        // Generate a new Sentry session ID when a different product is loaded
        if (lastLoadedProductExternalId != virtusizeProduct.externalId) {
            lastLoadedProductExternalId = virtusizeProduct.externalId
            VirtusizeSentryTracker.generateSessionId()
        }
        loadJob?.cancel()
        loadJob =
            scope.launch {
                if (!isActive) {
                    VirtusizeSentryTracker.trackLoadCancelled(
                        step = "start",
                        externalProductId = virtusizeProduct.externalId,
                        storeId = sentryStoreId,
                    )
                    return@launch
                }
                val success = productCheck(virtusizeProduct)
                if (!isActive) {
                    VirtusizeSentryTracker.trackLoadCancelled(
                        step = "product-check",
                        externalProductId = virtusizeProduct.externalId,
                        storeId = sentryStoreId,
                    )
                    return@launch
                }
                // productCheckData is set only when the API call itself succeeded (valid or invalid product)
                val apiSucceeded = virtusizeProduct.productCheckData != null
                VirtusizeSentryTracker.trackProductCheck(
                    externalProductId = virtusizeProduct.externalId,
                    isValid = success,
                    storeId = if (apiSucceeded) sentryStoreId else null,
                )
                if (apiSucceeded) {
                    VirtusizeSentryTracker.trackUserSawProduct(
                        externalProductId = virtusizeProduct.externalId,
                        storeId = sentryStoreId,
                    )
                    virtusizeRepository.updateUserSession(false, virtusizeProduct.externalId)
                    if (virtusizeViewsContainInPage()) {
                        virtusizeRepository.fetchInitialData(params.language, virtusizeProduct)
                        virtusizeRepository.fetchDataForInPageRecommendation(virtusizeProduct.externalId)
                        virtusizeRepository.updateInPageRecommendation(virtusizeProduct.externalId)
                    }
                } else {
                    VirtusizeSentryTracker.trackError(
                        throwable = Exception("Product check failed"),
                        storeId = null,
                    )
                }
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
            VirtusizeSentryTracker.trackSendOrder(order, sentryStoreId)
            virtusizeRepository.sendOrder(params, order, { _ ->
                onSuccess?.invoke()
            }, { error ->
                VirtusizeSentryTracker.trackError(
                    throwable = Exception(error.message),
                    storeId = sentryStoreId,
                )
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
            VirtusizeSentryTracker.trackSendOrder(order, sentryStoreId)
            virtusizeRepository.sendOrder(params, order, { data ->
                onSuccess?.onSuccess(data)
            }, { error ->
                VirtusizeSentryTracker.trackError(
                    throwable = Exception(error.message),
                    storeId = sentryStoreId,
                )
                onError?.onError(error)
            })
        }
    }

    /**
     * @see Virtusize.setVsWidgetLanguage
     */
    override fun setVsWidgetLanguage(language: VirtusizeLanguage) {
        virtusizeViews
            .filterIsInstance<VirtusizeButton>()
            .forEach { virtusizeView ->
                virtusizeView.setLanguage(language)
            }

        virtusizeViews
            .filterIsInstance<VirtusizeInPageView>()
            .forEach { virtusizeView ->
                virtusizeView.setLanguage(language)
            }

        scope.launch {
            params.language = language
            virtusizeRepository.setVsWidgetLanguage(language)
            virtusizeRepository.fetchDataForInPageRecommendation(
                shouldUpdateUserProducts = false,
                shouldUpdateBodyProfile = true,
            )
            virtusizeRepository.updateInPageRecommendation()
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
