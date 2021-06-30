package com.virtusize.libsource

import android.content.Context
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.remote.ProductType
import com.virtusize.libsource.data.remote.UserBodyProfile
import com.virtusize.libsource.network.VirtusizeAPIService
import com.virtusize.libsource.network.VirtusizeApiResponse

class VirtusizeFlutterRepository(context: Context, private val messageHandler: VirtusizeMessageHandler) {
    private val apiService: VirtusizeAPIService = VirtusizeAPIService.getInstance(context, messageHandler)
    private val sharedPreferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)

    suspend fun productDataCheck(product: VirtusizeProduct): ProductCheck? {
        val productCheckResponse = apiService.productDataCheck(product)
        sendEventsAndProductImage(product, productCheckResponse)
        return productCheckResponse.successData
    }

    private suspend fun sendEventsAndProductImage(
        product: VirtusizeProduct,
        pdcResponse: VirtusizeApiResponse<ProductCheck>
    ) {
        if (pdcResponse.isSuccessful) {
            sendEvent(
                VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()),
                pdcResponse.successData
            )
            val productCheck = pdcResponse.successData
            productCheck?.data?.apply {
                if (validProduct) {
                    if (fetchMetaData) {
                        if (product.imageUrl != null) {
                            // If image URL is valid, send image URL to server
                            val sendProductImageResponse =
                                apiService.sendProductImageToBackend(product = product)
                            if (!sendProductImageResponse.isSuccessful) {
                                sendProductImageResponse.failureData?.let { messageHandler.onError(it) }
                            }
                        } else {
                            VirtusizeErrorType.ImageUrlNotValid.throwError()
                        }
                    }

                    sendEvent(
                        VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()),
                        pdcResponse.successData
                    )
                }
            }
        }
    }

    private suspend fun sendEvent(vsEvent: VirtusizeEvent, productCheck: ProductCheck?) {
        val sendEventResponse = apiService.sendEvent(
            event = vsEvent,
            withDataProduct = productCheck
        )
        if (sendEventResponse.isSuccessful) {
            messageHandler.onEvent(vsEvent)
        }
    }

    suspend fun getStoreProduct(productId: Int) = apiService.getStoreProduct(productId).successData

    suspend fun getProductTypes() = apiService.getProductTypes().successData

    suspend fun getI18nLocalization(language: VirtusizeLanguage?) = apiService.getI18n(language).successData

    suspend fun getUserSessionResponse(): String? {
        val userSessionInfo = apiService.getUserSessionInfo().successData
        if(userSessionInfo != null) {
            sharedPreferencesHelper.storeSessionData(userSessionInfo.userSessionResponse)
            sharedPreferencesHelper.storeAccessToken(userSessionInfo.accessToken)
            if(userSessionInfo.authToken.isNotBlank()) {
                sharedPreferencesHelper.storeAuthToken(userSessionInfo.authToken)
            }
        }
        return userSessionInfo?.userSessionResponse
    }

    suspend fun getUserProducts() = apiService.getUserProducts().successData

    suspend fun getUserBodyProfile() = apiService.getUserBodyProfile().successData

    suspend fun getBodyProfileRecommendedSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile
    ) = apiService.getBodyProfileRecommendedSize(
        productTypes,
        storeProduct,
        userBodyProfile
    ).successData

    suspend fun deleteUser() = apiService.deleteUser().successData
}