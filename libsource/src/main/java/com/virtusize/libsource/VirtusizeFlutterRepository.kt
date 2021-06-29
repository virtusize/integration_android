package com.virtusize.libsource

import android.content.Context
import com.virtusize.libsource.data.local.VirtusizeLanguage
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductType
import com.virtusize.libsource.data.remote.UserBodyProfile
import com.virtusize.libsource.network.VirtusizeAPIService

class VirtusizeFlutterRepository(context: Context) {
    private val apiService: VirtusizeAPIService = VirtusizeAPIService.getInstance(context, null)
    private val sharedPreferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)

    suspend fun productDataCheck(product: VirtusizeProduct) = apiService.productDataCheck(product).successData

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