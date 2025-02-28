package com.virtusize.android.network

import android.content.Context
import android.graphics.Bitmap
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
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
import kotlinx.coroutines.CoroutineDispatcher
import org.json.JSONObject
import javax.net.ssl.HttpsURLConnection

internal interface VirtusizeAPIService {
    companion object {
        private var instance: VirtusizeAPIService? = null

        /**
         * Gets the instance of [VirtusizeAPIService]
         */
        fun getInstance(
            context: Context,
            messageHandler: VirtusizeMessageHandler?,
        ): VirtusizeAPIService {
            if (instance == null) {
                synchronized(VirtusizeAPIServiceImpl::javaClass) {
                    instance = VirtusizeAPIServiceImpl(context, messageHandler)
                }
            }
            return instance!!
        }
    }

    /**
     * Sets the HTTP URL connection
     * @param urlConnection an instance of [HttpsURLConnection]
     */
    fun setHTTPURLConnection(urlConnection: HttpsURLConnection?)

    /**
     * Sets the Coroutine dispatcher
     * @param dispatcher an instance of [CoroutineDispatcher]
     */
    fun setCoroutineDispatcher(dispatcher: CoroutineDispatcher)

    suspend fun fetchLatestAoyamaVersion(): VirtusizeApiResponse<String>

    /**
     * Executes the API task to make a network request for Product Check
     * @param product [VirtusizeProduct]
     * @return the [VirtusizeApiResponse] with the data class [ProductCheckData]
     */
    suspend fun productCheck(product: VirtusizeProduct): VirtusizeApiResponse<ProductCheckData>

    /**
     * Sends an image URL of VirtusizeProduct to the Virtusize server
     * @param product [VirtusizeProduct]
     * @return the [VirtusizeApiResponse] with the data class [ProductMetaDataHints]
     */
    suspend fun sendProductImageToBackend(product: VirtusizeProduct): VirtusizeApiResponse<ProductMetaDataHints>

    /**
     * Sends an event to the Virtusize server
     * @param event VirtusizeEvent
     * @param withDataProduct ProductCheckResponse corresponding to VirtusizeProduct
     * @return the [VirtusizeApiResponse]
     */
    suspend fun sendEvent(
        event: VirtusizeEvent,
        withDataProduct: ProductCheckData? = null,
    ): VirtusizeApiResponse<Any>

    /**
     * Sends an order to the Virtusize server
     * @return the [VirtusizeApiResponse]
     */
    suspend fun sendOrder(
        region: String?,
        order: VirtusizeOrder,
    ): VirtusizeApiResponse<Any>

    /**
     * Gets the API response for retrieving the specific store info
     * @return the [VirtusizeApiResponse] with the data class [Store]
     */
    suspend fun getStoreInfo(): VirtusizeApiResponse<Store>

    /**
     * Gets the API response for retrieving the store product info
     * @param productId the ID of the store product
     * @return the [VirtusizeApiResponse] with the data class [Product]
     */
    suspend fun getStoreProduct(productId: Int): VirtusizeApiResponse<Product>

    /**
     * Gets the API response for retrieving the list of the product types
     * @return the [VirtusizeApiResponse] with a list of [ProductType]
     */
    suspend fun getProductTypes(): VirtusizeApiResponse<List<ProductType>>

    /**
     * Gets the API response for getting the user session data
     * @return the [VirtusizeApiResponse] with [UserSessionInfo]
     */
    suspend fun getUserSessionInfo(): VirtusizeApiResponse<UserSessionInfo>

    /**
     * Gets the API response for deleting a user
     * @return the [VirtusizeApiResponse]
     */
    suspend fun deleteUser(): VirtusizeApiResponse<Any>

    /**
     * Gets the API response for retrieving a list of user products
     * @return the [VirtusizeApiResponse] with the list of [Product]
     */
    suspend fun getUserProducts(): VirtusizeApiResponse<List<Product>>

    /**
     * Gets the API response for retrieving the current user body profile such as age, height, weight and body measurements
     * @return the [VirtusizeApiResponse] with the data class [UserBodyProfile]
     */
    suspend fun getUserBodyProfile(): VirtusizeApiResponse<UserBodyProfile>

    /**
     * Gets the API response for retrieving the recommended size based on the user body profile
     * @param productTypes a list of product types
     * @param storeProduct the store product
     * @param userBodyProfile the user body profile
     * @return the [VirtusizeApiResponse] with the a list of [BodyProfileRecommendedSize]
     */
    suspend fun getBodyProfileRecommendedSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile,
    ): VirtusizeApiResponse<ArrayList<BodyProfileRecommendedSize>?>

    /**
     * Loads an image URL and returns the bitmap of the image
     * @param urlString the image URL string
     * @return the Bitmap of the image
     */
    suspend fun loadImage(urlString: String): Bitmap?

    /**
     * Gets the API response for fetching the i18n localization texts
     * @param language [VirtusizeLanguage] that is set by a client
     * @return the [VirtusizeApiResponse] with the JSON data
     */
    suspend fun getI18n(language: VirtusizeLanguage?): VirtusizeApiResponse<JSONObject>

    /**
     * Gets the API response for fetching the custom i18n texts for specific store
     * @param storeName the name of the store
     * @return the [VirtusizeApiResponse] with the JSON data
     */
    suspend fun getStoreSpecificI18n(storeName: String): VirtusizeApiResponse<JSONObject>
}
