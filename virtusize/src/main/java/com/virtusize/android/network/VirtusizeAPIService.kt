package com.virtusize.android.network

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.WindowManager
import com.virtusize.android.BuildConfig
import com.virtusize.android.R
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.virtusizeError
import com.virtusize.android.data.parsers.BodyProfileRecommendedSizeJsonParser
import com.virtusize.android.data.parsers.I18nLocalizationJsonParser
import com.virtusize.android.data.parsers.ProductCheckJsonParser
import com.virtusize.android.data.parsers.ProductMetaDataHintsJsonParser
import com.virtusize.android.data.parsers.ProductTypeJsonParser
import com.virtusize.android.data.parsers.StoreJsonParser
import com.virtusize.android.data.parsers.StoreProductJsonParser
import com.virtusize.android.data.parsers.UserBodyProfileJsonParser
import com.virtusize.android.data.parsers.UserProductJsonParser
import com.virtusize.android.data.parsers.UserSessionInfoJsonParser
import com.virtusize.android.data.remote.BodyProfileRecommendedSizeNew
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductCheck
import com.virtusize.android.data.remote.ProductMetaDataHints
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.Store
import com.virtusize.android.data.remote.UserBodyProfile
import com.virtusize.android.data.remote.UserSessionInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

/**
 * A class that handles API requests to the Virtusize server
 * @param context the application context
 * @param messageHandler pass VirtusizeMessageHandler to listen to any Virtusize-related messages
 */
internal class VirtusizeAPIService(
    private var context: Context,
    private var messageHandler: VirtusizeMessageHandler?
) {

    companion object {
        private var INSTANCE: VirtusizeAPIService? = null

        /**
         * Gets the instance of [VirtusizeAPIService]
         */
        fun getInstance(
            context: Context,
            messageHandler: VirtusizeMessageHandler?
        ): VirtusizeAPIService {
            if (INSTANCE == null) {
                synchronized(VirtusizeAPIService::javaClass) {
                    INSTANCE = VirtusizeAPIService(context, messageHandler)
                }
            }
            return INSTANCE!!
        }
    }

    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper =
        SharedPreferencesHelper.getInstance(context)

    // Device screen resolution
    private lateinit var resolution: String

    // The HTTP URL connection that is used to make a single request
    private var httpURLConnection: HttpsURLConnection? = null

    // The dispatcher that determines what thread the corresponding coroutine uses for its execution
    private var coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    /**
     * Sets the HTTP URL connection
     * @param urlConnection an instance of [HttpsURLConnection]
     */
    internal fun setHTTPURLConnection(urlConnection: HttpsURLConnection?) {
        this.httpURLConnection = urlConnection
    }

    /**
     * Sets the Coroutine dispatcher
     * @param dispatcher an instance of [CoroutineDispatcher]
     */
    internal fun setCoroutineDispatcher(dispatcher: CoroutineDispatcher) {
        this.coroutineDispatcher = dispatcher
    }

    /**
     * Executes the API task to make a network request for Product Check
     * @param product [VirtusizeProduct]
     * @return the [VirtusizeApiResponse] with the data class [ProductCheck]
     */
    internal suspend fun productDataCheck(
        product: VirtusizeProduct
    ): VirtusizeApiResponse<ProductCheck> =
        withContext(Dispatchers.IO) {
            val apiRequest = VirtusizeApi.productCheck(product)
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler
            )
                .setJsonParser(ProductCheckJsonParser())
                .execute(apiRequest)
        }

    /**
     * Sends an image URL of VirtusizeProduct to the Virtusize server
     * @param product [VirtusizeProduct]
     * @return the [VirtusizeApiResponse] with the data class [ProductMetaDataHints]
     */
    internal suspend fun sendProductImageToBackend(
        product: VirtusizeProduct
    ): VirtusizeApiResponse<ProductMetaDataHints> =
        withContext(Dispatchers.IO) {
            val apiRequest = VirtusizeApi.sendProductImageToBackend(product = product)
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler
            )
                .setJsonParser(ProductMetaDataHintsJsonParser())
                .execute(apiRequest)
        }

    /**
     * Sends an event to the Virtusize server
     * @param event VirtusizeEvent
     * @param withDataProduct ProductCheckResponse corresponding to VirtusizeProduct
     * @return the [VirtusizeApiResponse]
     */
    internal suspend fun sendEvent(
        event: VirtusizeEvent,
        withDataProduct: ProductCheck? = null
    ): VirtusizeApiResponse<Any> = withContext(Dispatchers.IO) {
        val defaultDisplay =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        resolution = "${defaultDisplay.height}x${defaultDisplay.width}"

        val apiRequest = VirtusizeApi.sendEventToAPI(
            virtusizeEvent = event,
            productCheck = withDataProduct,
            deviceOrientation =
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                context.getString(R.string.landscape)
            else
                context.getString(R.string.portrait),
            screenResolution = resolution,
            versionName = BuildConfig.VERSION_NAME
        )
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .execute(apiRequest)
    }

    /**
     * Sends an order to the Virtusize server
     * @return the [VirtusizeApiResponse]
     */
    internal suspend fun sendOrder(
        region: String?,
        order: VirtusizeOrder
    ): VirtusizeApiResponse<Any> = withContext(Dispatchers.IO) {
        // Sets the region from the store info
        order.setRegion(region)
        val apiRequest = VirtusizeApi.sendOrder(order)
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving the specific store info
     * @return the [VirtusizeApiResponse] with the data class [Store]
     */
    internal suspend fun getStoreInfo(): VirtusizeApiResponse<Store> = withContext(Dispatchers.IO) {
        val apiRequest = VirtusizeApi.getStoreInfo()
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .setJsonParser(StoreJsonParser())
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving the store product info
     * @param productId the ID of the store product
     * @return the [VirtusizeApiResponse] with the data class [Product]
     */
    internal suspend fun getStoreProduct(productId: Int): VirtusizeApiResponse<Product> =
        withContext(
            Dispatchers.IO
        ) {
            if (productId == 0) {
                return@withContext VirtusizeApiResponse.Error(
                    VirtusizeErrorType.NullProduct.virtusizeError()
                )
            }
            val apiRequest = VirtusizeApi.getStoreProductInfo(productId.toString())
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler
            )
                .setJsonParser(StoreProductJsonParser())
                .execute(apiRequest)
        }

    /**
     * Gets the API response for retrieving the list of the product types
     * @return the [VirtusizeApiResponse] with a list of [ProductType]
     */
    internal suspend fun getProductTypes(): VirtusizeApiResponse<List<ProductType>> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getProductTypes()
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .setJsonParser(ProductTypeJsonParser())
            .execute(apiRequest)
    }

    /**
     * Gets the API response for getting the user session data
     * @return the [VirtusizeApiResponse] with [UserSessionInfo]
     */
    internal suspend fun getUserSessionInfo(): VirtusizeApiResponse<UserSessionInfo> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getSessions()
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .setJsonParser(UserSessionInfoJsonParser())
            .execute(apiRequest)
    }

    /**
     * Gets the API response for deleting a user
     * @return the [VirtusizeApiResponse]
     */
    internal suspend fun deleteUser(): VirtusizeApiResponse<Any> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.deleteUser()
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving a list of user products
     * @return the [VirtusizeApiResponse] with the list of [Product]
     */
    internal suspend fun getUserProducts(): VirtusizeApiResponse<List<Product>> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getUserProducts()
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .setJsonParser(UserProductJsonParser())
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving the current user body profile such as age, height, weight and body measurements
     * @return the [VirtusizeApiResponse] with the data class [UserBodyProfile]
     */
    internal suspend fun getUserBodyProfile(): VirtusizeApiResponse<UserBodyProfile> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getUserBodyProfile()
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .setJsonParser(UserBodyProfileJsonParser())
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving the recommended size based on the user body profile
     * @param productTypes a list of product types
     * @param storeProduct the store product
     * @param userBodyProfile the user body profile
     * @return the [VirtusizeApiResponse] with the data class [UserBodyProfile]
     */
    internal suspend fun getBodyProfileRecommendedSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile
    ): VirtusizeApiResponse<ArrayList<BodyProfileRecommendedSizeNew>?> =
        withContext(Dispatchers.IO) {
        val apiRequest = VirtusizeApi.getSize(productTypes, storeProduct, userBodyProfile)
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .setJsonParser(BodyProfileRecommendedSizeJsonParser(storeProduct))
            .execute(apiRequest)
    }

    /**
     * Gets the API response for fetching the i18n localization texts
     * @param params [VirtusizeParams] to get the language that is set by a client
     * @return the [VirtusizeApiResponse] with the data class [I18nLocalization]
     */
    internal suspend fun getI18n(language: VirtusizeLanguage?):
        VirtusizeApiResponse<I18nLocalization> = withContext(Dispatchers.IO) {
        val apiRequest = VirtusizeApi.getI18n(
            language ?: (
                VirtusizeLanguage.values()
                    .find { it.value == Locale.getDefault().language } ?: VirtusizeLanguage.EN
                )
        )
        VirtusizeApiTask(
            httpURLConnection,
            sharedPreferencesHelper,
            messageHandler
        )
            .setJsonParser(I18nLocalizationJsonParser(context, language))
            .execute(apiRequest)
    }

    /**
     * Loads an image URL and returns the bitmap of the image
     * @param urlString the image URL string
     * @return the Bitmap of the image
     */
    internal suspend fun loadImage(urlString: String): Bitmap? = withContext(
        Dispatchers.IO
    ) {
        try {
            URL(urlString).openStream().use {
                return@withContext BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            return@withContext null
        }
    }
}
