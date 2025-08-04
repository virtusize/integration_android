package com.virtusize.android.network

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.WindowManager
import com.virtusize.android.BuildConfig
import com.virtusize.android.R
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.data.local.StoreId
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.virtusizeError
import com.virtusize.android.data.parsers.BodyProfileRecommendedSizeJsonParser
import com.virtusize.android.data.parsers.ProductCheckDataJsonParser
import com.virtusize.android.data.parsers.ProductMetaDataHintsJsonParser
import com.virtusize.android.data.parsers.ProductTypeJsonParser
import com.virtusize.android.data.parsers.StoreJsonParser
import com.virtusize.android.data.parsers.StoreProductJsonParser
import com.virtusize.android.data.parsers.UserBodyProfileJsonParser
import com.virtusize.android.data.parsers.UserProductJsonParser
import com.virtusize.android.data.parsers.UserSessionInfoJsonParser
import com.virtusize.android.data.remote.BodyProfileRecommendedSize
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductCheckData
import com.virtusize.android.data.remote.ProductMetaDataHints
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.Store
import com.virtusize.android.data.remote.UserBodyProfile
import com.virtusize.android.data.remote.UserSessionInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

/**
 * A class that handles API requests to the Virtusize server
 * @param context the application context
 * @param messageHandler pass VirtusizeMessageHandler to listen to any Virtusize-related messages
 */
internal class VirtusizeAPIServiceImpl(
    private val context: Context,
    private val messageHandler: VirtusizeMessageHandler?,
) : VirtusizeAPIService {
    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper =
        SharedPreferencesHelper.getInstance(context)

    // Device screen resolution
    private lateinit var resolution: String

    // The HTTP URL connection that is used to make a single request
    private var httpURLConnection: HttpsURLConnection? = null

    // The dispatcher that determines what thread the corresponding coroutine uses for its execution
    private var coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun setHTTPURLConnection(urlConnection: HttpsURLConnection?) {
        this.httpURLConnection = urlConnection
    }

    override fun setCoroutineDispatcher(dispatcher: CoroutineDispatcher) {
        this.coroutineDispatcher = dispatcher
    }

    override suspend fun fetchLatestAoyamaVersion(): VirtusizeApiResponse<String> =
        withContext(Dispatchers.IO) {
            val apiRequest = VirtusizeApi.fetchLatestAoyamaVersion()
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            ).setResponseFormat(VirtusizeApiResponseFormat.STRING).execute(apiRequest)
        }

    override suspend fun productCheck(product: VirtusizeProduct): VirtusizeApiResponse<ProductCheckData> =
        withContext(Dispatchers.IO) {
            val apiRequest = VirtusizeApi.productCheck(product)
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(ProductCheckDataJsonParser())
                .execute<ProductCheckData>(apiRequest)
                .also { response ->
                    val storeId = response.successData?.data?.storeId
                    if (storeId != null) {
                        VirtusizeApi.setStoreId(StoreId(storeId))
                    }
                }
        }

    override suspend fun sendProductImageToBackend(product: VirtusizeProduct): VirtusizeApiResponse<ProductMetaDataHints> =
        withContext(Dispatchers.IO) {
            val apiRequest = VirtusizeApi.sendProductImageToBackend(product = product)
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(ProductMetaDataHintsJsonParser())
                .execute(apiRequest)
        }

    override suspend fun sendEvent(
        event: VirtusizeEvent,
        withDataProduct: ProductCheckData?,
    ): VirtusizeApiResponse<Any> =
        withContext(Dispatchers.IO) {
            val defaultDisplay =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            resolution = "${defaultDisplay.height}x${defaultDisplay.width}"

            val apiRequest =
                VirtusizeApi.sendEventToAPI(
                    virtusizeEvent = event,
                    productCheckData = withDataProduct,
                    deviceOrientation =
                        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            context.getString(R.string.landscape)
                        } else {
                            context.getString(R.string.portrait)
                        },
                    screenResolution = resolution,
                    versionName = BuildConfig.VERSION_NANE,
                )
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            ).execute(apiRequest)
        }

    override suspend fun sendOrder(
        region: String?,
        order: VirtusizeOrder,
    ): VirtusizeApiResponse<Any> =
        withContext(Dispatchers.IO) {
            // Sets the region from the store info
            order.setRegion(region)
            val apiRequest = VirtusizeApi.sendOrder(order)
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .execute(apiRequest)
        }

    override suspend fun getStoreInfo(): VirtusizeApiResponse<Store> =
        withContext(Dispatchers.IO) {
            val apiRequest = VirtusizeApi.getStoreInfo()
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(StoreJsonParser())
                .execute(apiRequest)
        }

    override suspend fun getStoreProduct(productId: Int): VirtusizeApiResponse<Product> =
        withContext(
            Dispatchers.IO,
        ) {
            if (productId == 0) {
                return@withContext VirtusizeApiResponse.Error(
                    VirtusizeErrorType.NullProduct.virtusizeError(),
                )
            }
            val apiRequest = VirtusizeApi.getStoreProductInfo(productId.toString())
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(StoreProductJsonParser())
                .execute(apiRequest)
        }

    override suspend fun getProductTypes(): VirtusizeApiResponse<List<ProductType>> =
        withContext(
            Dispatchers.IO,
        ) {
            val apiRequest = VirtusizeApi.getProductTypes()
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(ProductTypeJsonParser())
                .execute(apiRequest)
        }

    override suspend fun getUserSessionInfo(): VirtusizeApiResponse<UserSessionInfo> =
        withContext(
            Dispatchers.IO,
        ) {
            val apiRequest = VirtusizeApi.getSessions()
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(UserSessionInfoJsonParser())
                .execute(apiRequest)
        }

    override suspend fun deleteUser(): VirtusizeApiResponse<Any> =
        withContext(
            Dispatchers.IO,
        ) {
            val apiRequest = VirtusizeApi.deleteUser()
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .execute(apiRequest)
        }

    override suspend fun getUserProducts(): VirtusizeApiResponse<List<Product>> =
        withContext(
            Dispatchers.IO,
        ) {
            val apiRequest = VirtusizeApi.getUserProducts()
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(UserProductJsonParser())
                .execute(apiRequest)
        }

    override suspend fun getUserBodyProfile(): VirtusizeApiResponse<UserBodyProfile> =
        withContext(
            Dispatchers.IO,
        ) {
            val apiRequest = VirtusizeApi.getUserBodyProfile()
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(UserBodyProfileJsonParser())
                .execute(apiRequest)
        }

    override suspend fun getBodyProfileRecommendedItemSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile,
    ): VirtusizeApiResponse<ArrayList<BodyProfileRecommendedSize>?> =
        withContext(Dispatchers.IO) {
            val apiRequest = VirtusizeApi.getItemSizeRecommendationRequest(productTypes, storeProduct, userBodyProfile)
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(BodyProfileRecommendedSizeJsonParser(storeProduct))
                .execute(apiRequest)
        }

    override suspend fun getBodyProfileRecommendedShoeSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile,
    ): VirtusizeApiResponse<BodyProfileRecommendedSize?> =
        withContext(Dispatchers.IO) {
            val apiRequest = VirtusizeApi.getShoeSizeRecommendationRequest(productTypes, storeProduct, userBodyProfile)
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .setJsonParser(BodyProfileRecommendedSizeJsonParser(storeProduct))
                .execute(apiRequest)
        }

    override suspend fun getI18n(language: VirtusizeLanguage?): VirtusizeApiResponse<JSONObject> =
        withContext(Dispatchers.IO) {
            val apiRequest =
                VirtusizeApi.getI18n(
                    language ?: (
                        VirtusizeLanguage.entries
                            .find { it.value == Locale.getDefault().language } ?: VirtusizeLanguage.EN
                    ),
                )
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .execute(apiRequest)
        }

    override suspend fun getStoreSpecificI18n(storeName: String): VirtusizeApiResponse<JSONObject> =
        withContext(Dispatchers.IO) {
            val apiRequest =
                VirtusizeApi.getStoreSpecificI18n(storeName)
            VirtusizeApiTask(
                httpURLConnection,
                sharedPreferencesHelper,
                messageHandler,
            )
                .execute(apiRequest)
        }

    override suspend fun loadImage(urlString: String): Bitmap? =
        withContext(
            Dispatchers.IO,
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
