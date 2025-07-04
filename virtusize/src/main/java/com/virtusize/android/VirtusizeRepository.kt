package com.virtusize.android

import android.content.Context
import android.graphics.Bitmap
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
import com.virtusize.android.data.local.virtusizeError
import com.virtusize.android.data.parsers.I18nLocalizationJsonParser
import com.virtusize.android.data.parsers.UserAuthDataJsonParser
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductCheckData
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.Store
import com.virtusize.android.network.VirtusizeAPIService
import com.virtusize.android.network.VirtusizeApiResponse
import com.virtusize.android.util.VirtusizeUtils
import com.virtusize.android.util.deepMerge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

// This class is used to handle the logic required to access remote and local data sources
class VirtusizeRepository internal constructor(
    private val context: Context,
    private var messageHandler: VirtusizeMessageHandler,
    private var virtusizeAPIService: VirtusizeAPIService,
    private var presenter: VirtusizePresenter? = null,
) {
    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper =
        SharedPreferencesHelper.getInstance(context)

    private var userProducts: List<Product>? = null
    private var userProductRecommendedSize: SizeComparisonRecommendedSize? = null
    private var userBodyRecommendedSize: String? = null

    // This variable holds the list of product types from the Virtusize API
    private var productTypes: List<ProductType>? = null

    // A map to cache the product data check data of all the visited products
    private val virtusizeProductCheckResponseMap: MutableMap<ExternalProductId, VirtusizeApiResponse<ProductCheckData>> =
        mutableMapOf()

    // A set to cache the store product information of all the visited products
    private val storeProductSet = mutableSetOf<Product>()

    // This variable holds the i18n localization texts
    internal var i18nLocalization: I18nLocalization? = null

    // / The last visited store product on the Virtusize web view
    private var lastProductOnVirtusizeWebView: Product? = null

    // A cached flag from user-session, to see if there is a need to fetch user-measurements
    private var hasSessionBodyMeasurement: Boolean = false

    // A store info to be able to load store specific i18n texts
    // The Store is persistent for the app and never changes
    private var storeInfo: Store? = null

    // A flag indicating if the store specific i18n texts should be reloaded
    // `true` for the first time and if the i18n texts are ever being successfully loaded
    // `false` if the i18n texts do not exist for the store (API returns 403 status code)
    private var shouldReloadStoreSpecificI18n: Boolean = true

    /**
     * Sets the last visited store product on the Virtusize web view
     * @param externalProductId the external product ID set by a client
     */
    internal fun setLastProductOnVirtusizeWebView(externalProductId: ExternalProductId) {
        lastProductOnVirtusizeWebView = getProductBy(externalProductId)
    }

    /**
     * Get the [Product] data by an external product ID
     * @param externalProductId the external product ID set by a client
     */
    internal fun getProductBy(externalProductId: ExternalProductId): Product? {
        return storeProductSet.firstOrNull { product ->
            product.externalId == externalProductId
        }
    }

    /**
     * Checks if the product is valid
     * @param virtusizeProduct the product info set by a client
     * @return true if the product is valid, false otherwise
     */
    internal suspend fun productCheck(virtusizeProduct: VirtusizeProduct): Boolean =
        coroutineScope {
            val productCheckResponse =
                virtusizeProductCheckResponseMap.getOrPut(virtusizeProduct.externalId) {
                    virtusizeAPIService.productCheck(virtusizeProduct)
                }
            if (!productCheckResponse.isSuccessful) {
                productCheckResponse.failureData?.let { error -> messageHandler.onError(error) }
                return@coroutineScope false
            }

            val productCheck = productCheckResponse.successData!!
            virtusizeProduct.productCheckData = productCheck

            // Send API Event UserSawProduct as non-blocking
            launch {
                sendEvent(
                    virtusizeProduct,
                    VirtusizeEvent.UserSawProduct(),
                )
            }

            val productCheckData = productCheck.data ?: return@coroutineScope false

            if (productCheckData.validProduct) {
                if (productCheckData.fetchMetaData) {
                    if (virtusizeProduct.imageUrl != null) {
                        // If image URL is valid, send image URL to server
                        val sendProductImageResponse =
                            virtusizeAPIService.sendProductImageToBackend(
                                product = virtusizeProduct,
                            )
                        if (!sendProductImageResponse.isSuccessful) {
                            sendProductImageResponse.failureData?.let {
                                messageHandler.onError(
                                    it,
                                )
                            }
                        }
                    } else {
                        VirtusizeErrorType.ImageUrlNotValid.throwError()
                    }
                }

                // Send API Event UserSawWidgetButton as non-blocking
                launch {
                    sendEvent(
                        virtusizeProduct,
                        VirtusizeEvent.UserSawWidgetButton(),
                    )
                }

                withContext(Dispatchers.Main) {
                    presenter?.onValidProductCheck(virtusizeProduct)
                }
                return@coroutineScope true
            } else {
                withContext(Dispatchers.Main) {
                    presenter?.hasInPageError(
                        externalProductId = virtusizeProduct.externalId,
                        error =
                            VirtusizeErrorType.InvalidProduct.virtusizeError(
                                extraMessage = virtusizeProduct.externalId,
                            ),
                    )
                }
                return@coroutineScope false
            }
        }

    /**
     * Sends a Virtusize event with the product data check data to the Virtusize API
     * @param product the [VirtusizeProduct] data wit the product check data
     * @param vsEvent the [VirtusizeEvent]
     */
    private suspend fun sendEvent(
        product: VirtusizeProduct,
        vsEvent: VirtusizeEvent,
    ) {
        val sendEventResponse =
            virtusizeAPIService.sendEvent(
                event = vsEvent,
                withDataProduct = product.productCheckData,
            )
        if (sendEventResponse.isSuccessful) {
            messageHandler.onEvent(product, vsEvent)
        }
    }

    /**
     * Fetches the initial data such as store product info, product type lists and i18 localization
     * @param language the display language set by a client
     * @param product the [VirtusizeProduct] data set by a client
     */
    internal suspend fun fetchInitialData(
        language: VirtusizeLanguage?,
        product: VirtusizeProduct,
    ) = coroutineScope {
        val productId = product.productCheckData!!.data!!.productDataId
        val externalProductId = product.externalId

        // Those API requests are independent and can be run in parallel
        val storeDeferred = async { virtusizeAPIService.getStoreProduct(productId) }
        val productTypesDeferred = async { virtusizeAPIService.getProductTypes() }
        val languageDeferred = async { fetchLanguage(language) }

        val storeProductResponse = storeDeferred.await()
        val productTypesResponse = productTypesDeferred.await()
        val i18n = languageDeferred.await()

        if (storeProductResponse.successData == null) {
            withContext(Dispatchers.Main) {
                presenter?.hasInPageError(externalProductId, storeProductResponse.failureData)
            }
            return@coroutineScope
        }

        val storeProduct = storeProductResponse.successData!!
        storeProduct.clientProductImageURL = product.imageUrl
        storeProductSet.add(storeProduct)

        if (productTypesResponse.successData == null) {
            withContext(Dispatchers.Main) {
                presenter?.hasInPageError(externalProductId, productTypesResponse.failureData)
            }
            return@coroutineScope
        }

        if (i18n == null) {
            withContext(Dispatchers.Main) {
                presenter?.hasInPageError(externalProductId, null)
            }
            return@coroutineScope
        }

        productTypes = productTypesResponse.successData!!
        i18nLocalization = i18n
    }

    /**
     * Updates the user session by calling the session API
     * @param externalProductId the external product ID set by a client
     */
    internal suspend fun updateUserSession(externalProductId: ExternalProductId? = lastProductOnVirtusizeWebView?.externalId) {
        val userSessionInfoResponse = virtusizeAPIService.getUserSessionInfo()
        if (userSessionInfoResponse.isSuccessful) {
            userSessionInfoResponse.successData?.apply {
                sharedPreferencesHelper.storeSessionData(userSessionResponse)
                sharedPreferencesHelper.storeAccessToken(accessToken)
                if (accessToken.isNotBlank()) {
                    sharedPreferencesHelper.storeAuthToken(authToken)
                }
                hasSessionBodyMeasurement = hasBodyMeasurement
            }
        } else {
            withContext(Dispatchers.Main) {
                presenter?.hasInPageError(externalProductId, userSessionInfoResponse.failureData)
            }
        }
    }

    /**
     * Fetches data for InPage recommendation
     * @param externalProductId the external product ID set by a client
     * @param selectedUserProductId the selected product Id from the web view to decide a specific user product to compare with the store product
     * @param shouldUpdateUserProducts determines whether to update user products from the Virtusize API
     * @param shouldUpdateBodyProfile determines whether to update a user's body profile from the Virtusize API
     */
    internal suspend fun fetchDataForInPageRecommendation(
        externalProductId: ExternalProductId? = null,
        selectedUserProductId: Int? = null,
        shouldUpdateUserProducts: Boolean = true,
        shouldUpdateBodyProfile: Boolean = true,
    ) = coroutineScope {
        var storeProduct = lastProductOnVirtusizeWebView
        externalProductId?.let {
            getProductBy(it)?.let { product ->
                storeProduct = product
            }
        }

        val userProductsDeferred =
            if (shouldUpdateUserProducts) {
                async { virtusizeAPIService.getUserProducts() }
            } else {
                null
            }

        val recommendedSizeDeferred =
            if (shouldUpdateBodyProfile && hasSessionBodyMeasurement) {
                async { getUserBodyRecommendedSize(storeProduct, productTypes) }
            } else {
                null
            }

        val userProductsResponse = userProductsDeferred?.await()
        // set `userBodyRecommendedSize` only when update is requested
        if (shouldUpdateBodyProfile) {
            // reset userBodyRecommendedSize if update is requested but hasSessionBodyMeasurement is false
            userBodyRecommendedSize = recommendedSizeDeferred?.await()
        }

        if (userProductsResponse != null) {
            if (userProductsResponse.isSuccessful) {
                userProducts = userProductsResponse.successData
            } else if (userProductsResponse.failureData?.code != HttpURLConnection.HTTP_NOT_FOUND) {
                withContext(Dispatchers.Main) {
                    presenter?.hasInPageError(
                        storeProduct?.externalId,
                        userProductsResponse.failureData,
                    )
                }
                return@coroutineScope
            }
        }

        userProductRecommendedSize =
            VirtusizeUtils.findBestFitProductSize(
                userProducts =
                    if (selectedUserProductId != null) {
                        userProducts?.filter { it.id == selectedUserProductId }
                    } else {
                        userProducts
                    },
                storeProduct = storeProduct,
                productTypes = productTypes,
            )
    }

    /**
     * Updates the user body recommended size
     * @param recommendedSize the recommended size got from the web view
     */
    internal fun updateUserBodyRecommendedSize(recommendedSize: String?) {
        userBodyRecommendedSize = recommendedSize
    }

    /**
     * Removes the deleted user product by the product ID from the user product list
     * @param userProductID the user product ID
     */
    internal fun deleteUserProduct(userProductID: Int) {
        userProducts = userProducts?.filter { userProduct -> userProduct.id != userProductID }
    }

    /**
     * Updates the recommendation for InPage based on the recommendation type
     * @param externalProductId the external product ID set by a client
     * @param type the selected recommendation compare view type
     */
    internal suspend fun updateInPageRecommendation(
        externalProductId: ExternalProductId? = null,
        type: SizeRecommendationType? = null,
    ) {
        (externalProductId ?: lastProductOnVirtusizeWebView?.externalId)?.let { productId ->
            withContext(Dispatchers.Main) {
                when (type) {
                    SizeRecommendationType.CompareProduct -> {
                        presenter?.gotSizeRecommendations(
                            externalProductId = productId,
                            userProductRecommendedSize = userProductRecommendedSize,
                            userBodyRecommendedSize = null,
                        )
                    }

                    SizeRecommendationType.Body -> {
                        presenter?.gotSizeRecommendations(
                            externalProductId = productId,
                            userProductRecommendedSize = null,
                            userBodyRecommendedSize = userBodyRecommendedSize,
                        )
                    }

                    else -> {
                        presenter?.gotSizeRecommendations(
                            externalProductId = productId,
                            userProductRecommendedSize = userProductRecommendedSize,
                            userBodyRecommendedSize = userBodyRecommendedSize,
                        )
                    }
                }
            }
        }
    }

    /**
     * Clear user session and the data related to size recommendations
     */
    internal suspend fun clearUserData() {
        virtusizeAPIService.deleteUser()
        sharedPreferencesHelper.storeAuthToken("")

        userProducts = null
        userProductRecommendedSize = null
        userBodyRecommendedSize = null
    }

    /**
     * Gets size recommendation for a store product that would best fit a user's body.
     * @param storeProduct the store product
     * @param productTypes a list of product types
     * @return recommended size name. If it's not available, return null
     */
    private suspend fun getUserBodyRecommendedSize(
        storeProduct: Product?,
        productTypes: List<ProductType>?,
    ): String? {
        if (storeProduct == null || productTypes == null || storeProduct.isAccessory()) {
            return null
        }
        val userBodyProfileResponse = virtusizeAPIService.getUserBodyProfile()
        if (userBodyProfileResponse.successData != null) {
            val bodyProfileRecommendedSizeResponse =
                virtusizeAPIService.getBodyProfileRecommendedSize(
                    productTypes,
                    storeProduct,
                    userBodyProfileResponse.successData!!,
                )
            return bodyProfileRecommendedSizeResponse.successData?.get(0)?.sizeName
        } else if (userBodyProfileResponse.failureData?.code != HttpURLConnection.HTTP_NOT_FOUND) {
            userBodyProfileResponse.failureData?.let {
                messageHandler.onError(it)
            }
        }
        return null
    }

    /**
     * Sends an order to the Virtusize server
     * @param params [VirtusizeParams]
     * @param order [VirtusizeOrder]
     * @param onSuccess the optional success callback to notify sending an order is successful
     * @param onError the optional error callback to get the [VirtusizeError]
     */
    internal suspend fun sendOrder(
        params: VirtusizeParams,
        order: VirtusizeOrder,
        onSuccess: ((Any?) -> Unit)?,
        onError: ((VirtusizeError) -> Unit)?,
    ) {
        // Throws the error if the user id is not set up or empty
        if (params.externalUserId.isNullOrEmpty()) {
            VirtusizeErrorType.UserIdNullOrEmpty.throwError()
        }
        val storeInfoResponse = virtusizeAPIService.getStoreInfo()
        if (storeInfoResponse.isSuccessful) {
            val sendOrderResponse =
                virtusizeAPIService.sendOrder(storeInfoResponse.successData?.region, order)
            if (sendOrderResponse.isSuccessful) {
                onSuccess?.invoke(sendOrderResponse.successData)
            } else {
                sendOrderResponse.failureData?.let { onError?.invoke(it) }
            }
        } else {
            storeInfoResponse.failureData?.let { onError?.invoke(it) }
        }
    }

    /**
     * Loads an image URL and returns the bitmap of the image
     * @param urlString the image URL string
     * @return the bitmap of the image
     */
    internal suspend fun loadImage(urlString: String?): Bitmap? = if (urlString == null) null else virtusizeAPIService.loadImage(urlString)

    /**
     * Updates the browser ID and the auth token from the data of the event user-auth-data
     * @param eventJsonObject the event data in JSONObject
     */
    internal fun updateUserAuthData(eventJsonObject: JSONObject) {
        try {
            val userAutoData = UserAuthDataJsonParser().parse(eventJsonObject)
            sharedPreferencesHelper.storeBrowserId(userAutoData?.bid)
            sharedPreferencesHelper.storeAuthToken(userAutoData?.auth)
        } catch (e: JSONException) {
            messageHandler.onError(
                VirtusizeErrorType.JsonParsingError.virtusizeError(
                    extraMessage = e.localizedMessage,
                ),
            )
        }
    }

    internal suspend fun fetchLanguage(language: VirtusizeLanguage?): I18nLocalization? =
        coroutineScope {
            // load common and specific localizations concurrently
            val languageDeferred = async { virtusizeAPIService.getI18n(language) }
            val storeLangDeferred =
                async {
                    // fetch Store to be able to load specific i18n by store name
                    if (storeInfo == null) {
                        val storeResponse = virtusizeAPIService.getStoreInfo()
                        storeInfo = storeResponse.successData
                    }

                    // skip loading if specific i18n does not exist for current store
                    if (!shouldReloadStoreSpecificI18n) {
                        return@async null
                    }

                    // load specific i18n
                    storeInfo?.let {
                        virtusizeAPIService.getStoreSpecificI18n(it.shortName)
                    }
                }

            // await for both loadings
            val i18nResponse = languageDeferred.await()
            val storeI18nResponse = storeLangDeferred.await()

            // prepare common i18n and i18n JSON parser
            val i18nJson = i18nResponse.successData ?: return@coroutineScope null
            val i18nParser = I18nLocalizationJsonParser(context, language)

            // if the i18n texts does not exist for specific store - never load it again
            if (storeI18nResponse?.failureData?.code == 403) {
                shouldReloadStoreSpecificI18n = false
                // i18n for the store does not exists, move on with default i18n
                return@coroutineScope i18nParser.parse(i18nJson)
            }

            // merge `storeI18n` over `i18n`
            val storeI18n = storeI18nResponse?.successData?.getJSONObject("mobile")
            storeI18n?.let {
                val lang = language?.value ?: "en"
                if (storeI18n.has(lang)) {
                    i18nJson.getJSONObject("keys").deepMerge(it.getJSONObject(lang))
                }
            }
            return@coroutineScope i18nParser.parse(i18nJson)
        }
}

private typealias ExternalProductId = String
