package com.virtusize.android.repository

import android.graphics.Bitmap
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.remote.BodyProfileRecommendedSize
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductCheckData
import com.virtusize.android.data.remote.ProductMetaDataHints
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.Store
import com.virtusize.android.data.remote.UserBodyProfile
import com.virtusize.android.data.remote.UserSessionInfo
import com.virtusize.android.network.VirtusizeAPIService
import com.virtusize.android.network.VirtusizeApiResponse
import kotlinx.coroutines.CoroutineDispatcher
import org.json.JSONObject
import javax.net.ssl.HttpsURLConnection

internal class MockVirtusizeApiService : VirtusizeAPIService {
    internal var mockI18n: ((VirtusizeLanguage?) -> JSONObject)? = null
    internal var mockStoreSpecificI18n: ((String) -> JSONObject?)? = null
    internal var mockStoreInfo: (() -> Store)? = null

    override fun setHTTPURLConnection(urlConnection: HttpsURLConnection?) {
        TODO("Not yet implemented")
    }

    override fun setCoroutineDispatcher(dispatcher: CoroutineDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchLatestAoyamaVersion(): VirtusizeApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun productCheck(product: VirtusizeProduct): VirtusizeApiResponse<ProductCheckData> {
        TODO("Not yet implemented")
    }

    override suspend fun sendProductImageToBackend(product: VirtusizeProduct): VirtusizeApiResponse<ProductMetaDataHints> {
        TODO("Not yet implemented")
    }

    override suspend fun sendEvent(
        event: VirtusizeEvent,
        withDataProduct: ProductCheckData?,
    ): VirtusizeApiResponse<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun sendOrder(
        region: String?,
        order: VirtusizeOrder,
    ): VirtusizeApiResponse<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun getStoreInfo() = mockStoreInfo?.let { VirtusizeApiResponse.Success(it()) } ?: TODO("Not supported")

    override suspend fun getStoreProduct(productId: Int): VirtusizeApiResponse<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun getProductTypes(): VirtusizeApiResponse<List<ProductType>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserSessionInfo(): VirtusizeApiResponse<UserSessionInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(): VirtusizeApiResponse<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserProducts(): VirtusizeApiResponse<List<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserBodyProfile(): VirtusizeApiResponse<UserBodyProfile> {
        TODO("Not yet implemented")
    }

    override suspend fun getBodyProfileRecommendedItemSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile,
    ): VirtusizeApiResponse<ArrayList<BodyProfileRecommendedSize>?> {
        TODO("Not yet implemented")
    }

    override suspend fun getBodyProfileRecommendedShoeSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile,
    ): VirtusizeApiResponse<BodyProfileRecommendedSize?> {
        TODO("Not yet implemented")
    }

    override suspend fun loadImage(urlString: String): Bitmap? {
        TODO("Not yet implemented")
    }

    override suspend fun getI18n(language: VirtusizeLanguage?) =
        mockI18n?.let { VirtusizeApiResponse.Success(it(language)) } ?: TODO("Not supported")

    override suspend fun getStoreSpecificI18n(storeName: String) =
        mockStoreSpecificI18n?.let {
            when (val specificI18n = it(storeName)) {
                null -> VirtusizeApiResponse.Error(VirtusizeError(message = "error"))
                else -> VirtusizeApiResponse.Success(specificI18n)
            }
        } ?: TODO("Not supported")
}
