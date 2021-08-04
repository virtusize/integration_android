package com.virtusize.libsource

import android.content.Context
import android.graphics.Bitmap
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.data.parsers.UserAuthDataJsonParser
import com.virtusize.libsource.data.remote.I18nLocalization
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.remote.ProductType
import com.virtusize.libsource.network.VirtusizeAPIService
import com.virtusize.libsource.util.VirtusizeUtils
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

// This class is used to handle the logic required to access remote and local data sources
internal class VirtusizeRepository(
    private val context: Context,
    private var messageHandler: VirtusizeMessageHandler,
    private var presenter: VirtusizePresenter? = null
) {

    // This variable is the instance of VirtusizeAPIService to handle Virtusize API requests
    private var virtusizeAPIService = VirtusizeAPIService.getInstance(context, messageHandler)

    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)

    private var userProducts: List<Product>? = null
    private var userProductRecommendedSize: SizeComparisonRecommendedSize? = null
    private var userBodyRecommendedSize: String? = null

    // This variable holds the product information from the client and the product data check API
    internal var virtusizeProduct: VirtusizeProduct? = null

    // This variable holds the list of product types from the Virtusize API
    private var productTypes: List<ProductType>? = null

    // This variable holds the store product from the Virtusize API
    internal var storeProduct: Product? = null

    // This variable holds the i18n localization texts
    internal var i18nLocalization: I18nLocalization? = null

    /**
     * Checks if the product is valid
     * @param virtusizeProduct the product info set by a client
     */
    internal suspend fun productDataCheck(virtusizeProduct: VirtusizeProduct) {
        val productCheckResponse = virtusizeAPIService.productDataCheck(virtusizeProduct)
        if (productCheckResponse.isSuccessful) {
            val productCheck = productCheckResponse.successData!!
            presenter?.finishedProductCheck(productCheck)

            // Send API Event UserSawProduct
            sendEvent(
                VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
                productCheck
            )

            productCheck.data?.apply {
                if (validProduct) {
                    if (fetchMetaData) {
                        if (virtusizeProduct.imageUrl != null) {
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
                    sendEvent(
                        VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()),
                        productCheck
                    )

                    presenter?.onValidProductId(productDataId)
                } else {
                    presenter?.hasInPageError(VirtusizeErrorType.InvalidProduct.virtusizeError(extraMessage = virtusizeProduct.externalId))
                }
            }
        } else {
            productCheckResponse.failureData?.let { messageHandler.onError(it) }
        }
    }

    /**
     * Sends a Virtusize event with the product data check data to the Virtusize API
     * @param vsEvent the [VirtusizeEvent]
     * @param productCheck the [ProductCheck] data
     */
    private suspend fun sendEvent(vsEvent: VirtusizeEvent, productCheck: ProductCheck?) {
        val sendEventResponse = virtusizeAPIService.sendEvent(
            event = vsEvent,
            withDataProduct = productCheck
        )
        if (sendEventResponse.isSuccessful) {
            messageHandler.onEvent(vsEvent)
        }
    }

    /**
     * Fetches the initial data such as store product info, product type lists and i18 localization
     * @param language the display language set by the client
     * @param productId the product ID provided by the client
     */
    internal suspend fun fetchInitialData(language: VirtusizeLanguage?, productId: Int) {
        val storeProductResponse = virtusizeAPIService.getStoreProduct(productId)
        if(storeProductResponse.successData == null) {
            presenter?.hasInPageError(storeProductResponse.failureData)
            return
        }

        val productTypesResponse = virtusizeAPIService.getProductTypes()
        if(productTypesResponse.successData == null) {
            presenter?.hasInPageError(productTypesResponse.failureData)
            return
        }

        val i18nResponse = virtusizeAPIService.getI18n(language)
        if(i18nResponse.successData == null) {
            presenter?.hasInPageError(i18nResponse.failureData)
            return
        }

        storeProduct = storeProductResponse.successData!!
        productTypes = productTypesResponse.successData!!
        i18nLocalization = i18nResponse.successData!!
    }

    /**
     * Updates the user session by calling the session API
     */
    internal suspend fun updateUserSession(){
        val userSessionInfoResponse = virtusizeAPIService.getUserSessionInfo()
        if (userSessionInfoResponse.isSuccessful) {
            sharedPreferencesHelper.storeSessionData(userSessionInfoResponse.successData!!.userSessionResponse)
            sharedPreferencesHelper.storeAccessToken(userSessionInfoResponse.successData!!.accessToken)
            if(userSessionInfoResponse.successData!!.authToken.isNotBlank()) {
                sharedPreferencesHelper.storeAuthToken(userSessionInfoResponse.successData!!.authToken)
            }
        } else {
            presenter?.hasInPageError(userSessionInfoResponse.failureData)
        }
    }

    /**
     * Fetches data for InPage recommendation
     * @param selectedUserProductId the selected product Id from the web view to decide a specific user product to compare with the store product
     */
    internal suspend fun fetchDataForInPageRecommendation(
        selectedUserProductId: Int? = null,
        shouldUpdateUserProducts: Boolean = true,
        shouldUpdateBodyProfile: Boolean = true
    ) {
        if(shouldUpdateUserProducts) {
            val userProductsResponse = virtusizeAPIService.getUserProducts()
            if (userProductsResponse.isSuccessful) {
                userProducts = userProductsResponse.successData
            } else if (userProductsResponse.failureData?.code != HttpURLConnection.HTTP_NOT_FOUND) {
                presenter?.hasInPageError(userProductsResponse.failureData)
                return
            }
        }

        if(shouldUpdateBodyProfile) {
            userBodyRecommendedSize = getUserBodyRecommendedSize(storeProduct, productTypes)
        }

        userProductRecommendedSize = VirtusizeUtils.findBestFitProductSize(
            userProducts = if(selectedUserProductId != null) userProducts?.filter { it.id == selectedUserProductId } else userProducts,
            storeProduct = storeProduct,
            productTypes = productTypes
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
     * @param type the selected recommendation compare view type
     */
    internal fun updateInPageRecommendation(
        type: SizeRecommendationType? = null
    ) {
        when (type) {
            SizeRecommendationType.compareProduct -> {
                presenter?.gotSizeRecommendations(userProductRecommendedSize, null)
            }
            SizeRecommendationType.body -> {
                presenter?.gotSizeRecommendations(null, userBodyRecommendedSize)
            }
            else -> {
                presenter?.gotSizeRecommendations(userProductRecommendedSize, userBodyRecommendedSize)
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
    private suspend fun getUserBodyRecommendedSize(storeProduct: Product?, productTypes: List<ProductType>?): String? {
        if(storeProduct == null || productTypes == null || storeProduct.isAccessory()) {
            return null
        }
        val userBodyProfileResponse = virtusizeAPIService.getUserBodyProfile()
        if (userBodyProfileResponse.successData != null) {
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
        onError: ((VirtusizeError) -> Unit)?
    ) {
        // Throws the error if the user id is not set up or empty
        if (params.externalUserId.isNullOrEmpty()) {
            VirtusizeErrorType.UserIdNullOrEmpty.throwError()
        }
        val storeInfoResponse = virtusizeAPIService.getStoreInfo()
        if (storeInfoResponse.isSuccessful) {
            val sendOrderResponse = virtusizeAPIService.sendOrder(storeInfoResponse.successData?.region, order)
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
    internal suspend fun loadImage(urlString: String): Bitmap? = virtusizeAPIService.loadImage(urlString)

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
            messageHandler.onError(VirtusizeErrorType.JsonParsingError.virtusizeError(extraMessage = e.localizedMessage))
        }
    }
}