package com.virtusize.android.flutter

import android.content.Context
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.Virtusize
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeEvents
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.getEventName
import com.virtusize.android.data.local.throwError
import com.virtusize.android.data.local.virtusizeError
import com.virtusize.android.data.parsers.UserAuthDataJsonParser
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductCheck
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.UserBodyProfile
import com.virtusize.android.network.VirtusizeAPIService
import com.virtusize.android.network.VirtusizeApiResponse
import java.net.HttpURLConnection
import org.json.JSONException
import org.json.JSONObject

class VirtusizeFlutterRepository(
    context: Context,
    private val messageHandler: VirtusizeMessageHandler
) {
    private val apiService: VirtusizeAPIService =
        VirtusizeAPIService.getInstance(context, messageHandler)
    private val sharedPreferencesHelper: SharedPreferencesHelper =
        SharedPreferencesHelper.getInstance(context)

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
            product.productCheckData = pdcResponse.successData
            sendEvent(
                product,
                VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName())
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
                                sendProductImageResponse.failureData?.let {
                                    messageHandler.onError(
                                        it
                                    )
                                }
                            }
                        } else {
                            VirtusizeErrorType.ImageUrlNotValid.throwError()
                        }
                    }

                    sendEvent(
                        product,
                        VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName())
                    )
                }
            }
        }
    }

    private suspend fun sendEvent(product: VirtusizeProduct, vsEvent: VirtusizeEvent) {
        val sendEventResponse = apiService.sendEvent(
            event = vsEvent,
            withDataProduct = product.productCheckData
        )
        if (sendEventResponse.isSuccessful) {
            messageHandler.onEvent(product, vsEvent)
        }
    }

    suspend fun getStoreProduct(productId: Int) = apiService.getStoreProduct(productId).successData

    suspend fun getProductTypes() = apiService.getProductTypes().successData

    suspend fun getI18nLocalization(language: VirtusizeLanguage?) =
        apiService.getI18n(language).successData

    suspend fun getUserSessionResponse(): String? {
        val userSessionInfo = apiService.getUserSessionInfo().successData
        if (userSessionInfo != null) {
            sharedPreferencesHelper.storeSessionData(userSessionInfo.userSessionResponse)
            sharedPreferencesHelper.storeAccessToken(userSessionInfo.accessToken)
            if (userSessionInfo.authToken.isNotBlank()) {
                sharedPreferencesHelper.storeAuthToken(userSessionInfo.authToken)
            }
        }
        return userSessionInfo?.userSessionResponse
    }

    fun updateUserAuthData(eventJsonObject: JSONObject) {
        try {
            val userAutoData = UserAuthDataJsonParser().parse(eventJsonObject)
            sharedPreferencesHelper.storeBrowserId(userAutoData?.bid)
            sharedPreferencesHelper.storeAuthToken(userAutoData?.auth)
        } catch (e: JSONException) {
            messageHandler.onError(
                VirtusizeErrorType.JsonParsingError.virtusizeError(
                    extraMessage = e.localizedMessage
                )
            )
        }
    }

    suspend fun getUserProducts(): List<Product>? {
        val userProductsResponse = apiService.getUserProducts()
        if (userProductsResponse.isSuccessful) {
            return userProductsResponse.successData
        } else if (userProductsResponse.failureData?.code == HttpURLConnection.HTTP_NOT_FOUND) {
            return mutableListOf()
        }
        return null
    }

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

    suspend fun deleteUser(): Any? {
        val response = apiService.deleteUser()
        if (response.isSuccessful) {
            sharedPreferencesHelper.storeAuthToken("")
        }
        return response.successData
    }

    suspend fun sendOrder(
        virtusize: Virtusize?,
        orderMap: Map<String, Any?>,
        onSuccess: ((Any?) -> Unit)?,
        onError: ((VirtusizeError) -> Unit)?
    ) {
        if (virtusize?.params?.externalUserId.isNullOrEmpty()) {
            VirtusizeErrorType.UserIdNullOrEmpty.throwError()
        }
        val storeInfoResponse = apiService.getStoreInfo()
        if (storeInfoResponse.isSuccessful) {
            val sendOrderResponse =
                apiService.sendOrder(
                    storeInfoResponse.successData?.region,
                    VirtusizeOrder.parseMap(orderMap)
                )
            if (sendOrderResponse.isSuccessful) {
                onSuccess?.invoke(sendOrderResponse.successData)
            } else {
                sendOrderResponse.failureData?.let { onError?.invoke(it) }
            }
        } else {
            storeInfoResponse.failureData?.let { onError?.invoke(it) }
        }
    }
}
