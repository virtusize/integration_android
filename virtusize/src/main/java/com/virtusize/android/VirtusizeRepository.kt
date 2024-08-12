package com.virtusize.android

import android.content.Context
import android.graphics.Bitmap
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
import com.virtusize.android.data.local.virtusizeError
import com.virtusize.android.data.parsers.UserAuthDataJsonParser
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.network.VirtusizeAPIService
import com.virtusize.android.util.VirtusizeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

// This class is used to handle the logic required to access remote and local data sources
internal class VirtusizeRepository(
    private val context: Context,
    private var messageHandler: VirtusizeMessageHandler,
    private var presenter: VirtusizePresenter? = null,
) {
    // This variable is the instance of VirtusizeAPIService to handle Virtusize API requests
    private var virtusizeAPIService = VirtusizeAPIService.getInstance(context, messageHandler)

    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper =
        SharedPreferencesHelper.getInstance(context)

    private var userProducts: List<Product>? = null
    private var userProductRecommendedSize: SizeComparisonRecommendedSize? = null
    private var userBodyRecommendedSize: String? = null

    // This variable holds the list of product types from the Virtusize API
    private var productTypes: List<ProductType>? = null

    // A set to cache the product data check data of all the visited products
    private val virtusizeProductSet = mutableSetOf<VirtusizeProduct>()

    // A set to cache the store product information of all the visited products
    private val storeProductSet = mutableSetOf<Product>()

    // This variable holds the i18n localization texts
    internal var i18nLocalization: I18nLocalization? = null

    // / The last visited store product on the Virtusize web view
    private var lastProductOnVirtusizeWebView: Product? = null

    /**
     * Sets the last visited store product on the Virtusize web view
     * @param externalProductId the external product ID set by a client
     */
    internal fun setLastProductOnVirtusizeWebView(externalProductId: String) {
        lastProductOnVirtusizeWebView = getProductBy(externalProductId)
    }

    /**
     * Get the [Product] data by an external product ID
     * @param externalProductId the external product ID set by a client
     */
    internal fun getProductBy(externalProductId: String): Product? {
        return storeProductSet.firstOrNull { product ->
            product.externalId == externalProductId
        }
    }

    /**
     * Checks if the product is valid
     * @param virtusizeProduct the product info set by a client
     * @return true if the product is valid, false otherwise
     */
    internal suspend fun productDataCheck(virtusizeProduct: VirtusizeProduct): Boolean {
        val productCheckResponse = virtusizeAPIService.productDataCheck(virtusizeProduct)
        if (productCheckResponse.isSuccessful) {
            val productCheck = productCheckResponse.successData!!
            virtusizeProduct.productCheckData = productCheck
            virtusizeProductSet.add(virtusizeProduct)

            // Send API Event UserSawProduct
            sendEvent(
                virtusizeProduct,
                VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
            )

            productCheck.data?.let { productCheckData ->
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

                    // Send API Event UserSawWidgetButton
                    sendEvent(
                        virtusizeProduct,
                        VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()),
                    )

                    withContext(Dispatchers.Main) {
                        presenter?.onValidProductDataCheck(virtusizeProduct)
                    }
                    return true
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
                    return false
                }
            } ?: return false
        } else {
            productCheckResponse.failureData?.let { error -> messageHandler.onError(error) }
            return false
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
    ) {
        val productId = product.productCheckData!!.data!!.productDataId
        val externalProductId = product.externalId
        val storeProductResponse = virtusizeAPIService.getStoreProduct(productId)
        if (storeProductResponse.successData == null) {
            withContext(Dispatchers.Main) {
                presenter?.hasInPageError(externalProductId, storeProductResponse.failureData)
            }
            return
        }

        val storeProduct = storeProductResponse.successData!!
        storeProduct.clientProductImageURL = product.imageUrl
        storeProductSet.add(storeProduct)

        val productTypesResponse = virtusizeAPIService.getProductTypes()
        if (productTypesResponse.successData == null) {
            withContext(Dispatchers.Main) {
                presenter?.hasInPageError(externalProductId, productTypesResponse.failureData)
            }
            return
        }

        val i18nResponse = virtusizeAPIService.getI18n(language)
        if (i18nResponse.successData == null) {
            withContext(Dispatchers.Main) {
                presenter?.hasInPageError(externalProductId, i18nResponse.failureData)
            }
            return
        }

        productTypes = productTypesResponse.successData!!
        i18nLocalization = i18nResponse.successData!!
    }

    /**
     * Updates the user session by calling the session API
     * @param externalProductId the external product ID set by a client
     */
    internal suspend fun updateUserSession(externalProductId: String? = lastProductOnVirtusizeWebView?.externalId) {
        val userSessionInfoResponse = virtusizeAPIService.getUserSessionInfo()
        if (userSessionInfoResponse.isSuccessful) {
            sharedPreferencesHelper.storeSessionData(
                userSessionInfoResponse.successData!!.userSessionResponse,
            )
            sharedPreferencesHelper.storeAccessToken(
                userSessionInfoResponse.successData!!.accessToken,
            )
            if (userSessionInfoResponse.successData!!.authToken.isNotBlank()) {
                sharedPreferencesHelper.storeAuthToken(
                    userSessionInfoResponse.successData!!.authToken,
                )
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
        externalProductId: String? = null,
        selectedUserProductId: Int? = null,
        shouldUpdateUserProducts: Boolean = true,
        shouldUpdateBodyProfile: Boolean = true,
    ) {
        var storeProduct = lastProductOnVirtusizeWebView
        externalProductId?.let {
            getProductBy(it)?.let { product ->
                storeProduct = product
            }
        }
        if (shouldUpdateUserProducts) {
            val userProductsResponse = virtusizeAPIService.getUserProducts()
            if (userProductsResponse.isSuccessful) {
                userProducts = userProductsResponse.successData
            } else if (userProductsResponse.failureData?.code != HttpURLConnection.HTTP_NOT_FOUND) {
                withContext(Dispatchers.Main) {
                    presenter?.hasInPageError(
                        storeProduct?.externalId,
                        userProductsResponse.failureData,
                    )
                }
                return
            }
        }

        if (shouldUpdateBodyProfile) {
            userBodyRecommendedSize = getUserBodyRecommendedSize(storeProduct, productTypes)
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
        externalProductId: String? = null,
        type: SizeRecommendationType? = null,
    ) {
        (externalProductId ?: lastProductOnVirtusizeWebView?.externalId)?.let { externalProductId ->
            withContext(Dispatchers.Main) {
                when (type) {
                    SizeRecommendationType.CompareProduct -> {
                        presenter?.gotSizeRecommendations(
                            externalProductId,
                            userProductRecommendedSize,
                            null,
                        )
                    }
                    SizeRecommendationType.Body -> {
                        presenter?.gotSizeRecommendations(
                            externalProductId,
                            null,
                            userBodyRecommendedSize,
                        )
                    }
                    else -> {
                        presenter?.gotSizeRecommendations(
                            externalProductId,
                            userProductRecommendedSize,
                            userBodyRecommendedSize,
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
}
