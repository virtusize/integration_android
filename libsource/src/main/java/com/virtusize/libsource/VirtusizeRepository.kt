package com.virtusize.libsource

import android.content.Context
import android.graphics.Bitmap
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductType
import com.virtusize.libsource.network.VirtusizeAPIService
import com.virtusize.libsource.util.VirtusizeUtils
import java.net.HttpURLConnection

internal class VirtusizeRepository(
    private val context: Context,
    private var messageHandler: VirtusizeMessageHandler,
    private var presenter: VirtusizePresenter? = null
) {

    // This variable is the instance of VirtusizeAPIService to handle Virtusize API requests
    private var virtusizeAPIService = VirtusizeAPIService.getInstance(context, messageHandler)

    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)

    internal suspend fun productDataCheck(params: VirtusizeParams) {
        val productCheckResponse = virtusizeAPIService.productDataCheck(params.virtusizeProduct!!)
        if (productCheckResponse.isSuccessful) {
            val productCheck = productCheckResponse.successData!!
            presenter?.onProductCheck(productCheck)

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
                            val sendProductImageResponse = virtusizeAPIService.sendProductImageToBackend(product = params.virtusizeProduct!!)
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
                    if (sendEventResponse.isSuccessful) {
                        messageHandler.onEvent(VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()))
                    }
                    presenter?.onProductId(productDataId)
                } else {
                    presenter?.showErrorForInPage(VirtusizeErrorType.InvalidProduct.virtusizeError(params.virtusizeProduct!!.externalId))
                }
            }
        } else {
            productCheckResponse.failureData?.let { messageHandler.onError(it) }
        }
    }

    internal suspend fun fetchInitialData(params: VirtusizeParams, productId: Int) {
        val storeProductResponse = virtusizeAPIService.getStoreProduct(productId)
        storeProductResponse.successData?.let { product ->
            presenter?.onStoreProduct(product)
        } ?: run {
            presenter?.showErrorForInPage(storeProductResponse.failureData)
            return
        }

        val productTypesResponse = virtusizeAPIService.getProductTypes()
        productTypesResponse.successData?.let { productTypes ->
            presenter?.onProductTypes(productTypes)
        } ?: run {
            presenter?.showErrorForInPage(productTypesResponse.failureData)
            return
        }

        val i18nResponse = virtusizeAPIService.getI18n(params)
        i18nResponse.successData?.let { i18nLocalization ->
            presenter?.onI18nLocalization(i18nLocalization)
        } ?: run {
            presenter?.showErrorForInPage(i18nResponse.failureData)
            return
        }
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
            presenter?.showErrorForInPage(userSessionInfoResponse.failureData)
        }
    }

    /**
     * Updates the recommendation for InPage
     * @param selectedUserProductId the selected product Id from the web view to decide a specific user product to compare with the store product
     * @param ignoreUserData pass the boolean vale to determine whether to ignore the API requests that is related to the user data
     */
    internal suspend fun updateInPageRecommendation(
        storeProduct: Product?,
        productTypes: List<ProductType>?,
        selectedUserProductId: Int? = null,
        ignoreUserData: Boolean = false
    ) {
        if (storeProduct == null || productTypes == null) {
            return
        }
        var userProducts: List<Product>? = null
        var userProductRecommendedSize: SizeComparisonRecommendedSize? = null
        var userBodyRecommendedSize: String? = null
        if(!ignoreUserData) {
            val userProductsResponse = virtusizeAPIService.getUserProducts()
            if (userProductsResponse.isSuccessful) {
                userProducts = userProductsResponse.successData
            } else if(userProductsResponse.failureData?.code != HttpURLConnection.HTTP_NOT_FOUND) {
                presenter?.showErrorForInPage(userProductsResponse.failureData)
                return
            }

            userProductRecommendedSize = VirtusizeUtils.findBestFitProductSize(
                userProducts = if(selectedUserProductId != null) userProducts?.filter { it.id == selectedUserProductId } else userProducts,
                storeProduct = storeProduct,
                productTypes = productTypes
            )
            userBodyRecommendedSize = getUserBodyRecommendedSize(storeProduct, productTypes)
        }

        presenter?.updateInPageRecommendation(userProductRecommendedSize, userBodyRecommendedSize)
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
        val storeInfoResponse = virtusizeAPIService.getStoreInfo()
        if (storeInfoResponse.isSuccessful) {
            val sendOrderResponse = virtusizeAPIService.sendOrder(params, storeInfoResponse.successData, order)
            if (sendOrderResponse.isSuccessful) {
                onSuccess?.invoke(sendOrderResponse.successData)
            } else {
                sendOrderResponse.failureData?.let { onError?.invoke(it) }
            }
        } else {
            storeInfoResponse.failureData?.let { onError?.invoke(it) }
        }
    }

    internal suspend fun loadImage(urlString: String): Bitmap? = virtusizeAPIService.loadImage(urlString)
}