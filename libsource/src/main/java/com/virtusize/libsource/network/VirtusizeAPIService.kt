package com.virtusize.libsource.network

import android.content.Context
import android.content.res.Configuration
import android.view.WindowManager
import com.virtusize.libsource.*
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.local.virtusizeError
import com.virtusize.libsource.data.parsers.*
import com.virtusize.libsource.data.parsers.I18nLocalizationJsonParser
import com.virtusize.libsource.data.parsers.UserSessionInfoJsonParser
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.data.remote.UserSessionInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.util.*
import javax.net.ssl.HttpsURLConnection

class VirtusizeAPIService(private var context: Context) {

    companion object {
        private var INSTANCE: VirtusizeAPIService? = null

        fun getInstance(context: Context): VirtusizeAPIService {
            if (INSTANCE == null) {
                synchronized(VirtusizeAPIService::javaClass) {
                    INSTANCE = VirtusizeAPIService(context)
                }
            }
            return INSTANCE!!
        }
    }

    // The helper to store data locally using Shared Preferences
    private var sharedPreferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)

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
     * Executes the API task to make a network request for Product Check
     * @param productValidCheckListener VirtusizeButton that is being set up
     * @param errorHandler VirtusizeProduct that is being set to this button
     * @param apiRequest [ApiRequest]
     */
    internal fun productDataCheck(
        productValidCheckListener: ValidProductCheckHandler,
        errorHandler: ErrorResponseHandler,
        apiRequest: ApiRequest
    ) {
        VirtusizeApiTask(httpURLConnection)
            .setSuccessHandler(productValidCheckListener)
            .setJsonParser(ProductCheckJsonParser())
            .setErrorHandler(errorHandler)
            .setSharedPreferencesHelper(sharedPreferencesHelper)
            .executeAsync<ProductCheck>(apiRequest, coroutineDispatcher)
    }

    /**
     * Sends an image URL of VirtusizeProduct to the Virtusize server
     * @param product VirtusizeProduct
     * @param successHandler the success callback to get the API response data
     * @param errorHandler
     * @see VirtusizeProduct
     */
    internal fun sendProductImageToBackend(
        product: VirtusizeProduct,
        successHandler: SuccessResponseHandler? = null,
        errorHandler: ErrorResponseHandler
    ) {
        val apiRequest = VirtusizeApi.sendProductImageToBackend(product = product)
        VirtusizeApiTask(httpURLConnection)
            .setJsonParser(ProductMetaDataHintsJsonParser())
            .setSuccessHandler(successHandler)
            .setErrorHandler(errorHandler)
            .setSharedPreferencesHelper(sharedPreferencesHelper)
            .executeAsync<Any>(apiRequest, coroutineDispatcher)
    }

    /**
     * Sends an event to the Virtusize server
     * @param event VirtusizeEvent
     * @param withDataProduct ProductCheckResponse corresponding to VirtusizeProduct
     * @param successHandler the success callback to get the API response data
     * @param errorHandler the error callback to get the [VirtusizeError] in the API task
     */
    internal fun sendEventToApi(
        event: VirtusizeEvent,
        withDataProduct: ProductCheck? = null,
        successHandler: SuccessResponseHandler? = null,
        errorHandler: ErrorResponseHandler
    ) {
        val defaultDisplay =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        resolution = "${defaultDisplay.height}x${defaultDisplay.width}"

        val apiRequest = VirtusizeApi.sendEventToAPI(
            virtusizeEvent = event,
            productCheck = withDataProduct,
            deviceOrientation = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) context.getString(
                R.string.landscape
            )
            else context.getString(R.string.portrait),
            screenResolution = resolution,
            versionCode = context.packageManager
                .getPackageInfo(context.packageName, 0).versionCode
        )
        VirtusizeApiTask(httpURLConnection)
            .setSuccessHandler(successHandler)
            .setErrorHandler(errorHandler)
            .setSharedPreferencesHelper(sharedPreferencesHelper)
            .executeAsync<Any>(apiRequest, coroutineDispatcher)
    }

    /**
     * Retrieves the specific store info
     * @param onSuccess the success callback to get the [Store] in the API task
     * @param onError the error callback to get the [VirtusizeError] in the API task
     */
    internal fun getStoreInfo(
        onSuccess: SuccessResponseHandler? = null,
        onError: ErrorResponseHandler? = null) {
        val apiRequest = VirtusizeApi.getStoreInfo()
        VirtusizeApiTask(httpURLConnection)
            .setJsonParser(StoreJsonParser())
            .setSuccessHandler(onSuccess)
            .setErrorHandler(onError)
            .executeAsync<Any>(apiRequest, coroutineDispatcher)
    }


    /**
     * Gets the API response for retrieving the store product info
     * @param productId the ID of the store product
     * @return the [VirtusizeApiResponse] with the data class [StoreProduct]
     */
    internal suspend fun getStoreProductResponse(productId: Int): VirtusizeApiResponse<Product?> = withContext(
        Dispatchers.IO
    ) {
        if(productId == 0) {
            return@withContext VirtusizeApiResponse.Error(com.virtusize.libsource.data.local.VirtusizeErrorType.NullProduct.virtusizeError())
        }
        val apiRequest = VirtusizeApi.getStoreProductInfo(productId.toString())
        return@withContext VirtusizeApiTask(httpURLConnection)
            .setJsonParser(StoreProductJsonParser())
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving the list of the product types
     * @return the [VirtusizeApiResponse] with a list of [ProductType]
     */
    internal suspend fun getProductTypesResponse(): VirtusizeApiResponse<List<ProductType>?> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getProductTypes()
        return@withContext VirtusizeApiTask(httpURLConnection)
            .setJsonParser(ProductTypeJsonParser())
            .execute(apiRequest)
    }

    /**
     * Gets the API response for getting the user session data
     * @return the [VirtusizeApiResponse] with [UserSessionInfo]
     */
    internal suspend fun getUserSessionInfoResponse(): VirtusizeApiResponse<UserSessionInfo?> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getSessions()
        return@withContext VirtusizeApiTask(httpURLConnection)
            .setJsonParser(UserSessionInfoJsonParser())
            .setSharedPreferencesHelper(sharedPreferencesHelper)
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving a list of user products
     * @return the [VirtusizeApiResponse] with the list of [Product]
     */
    internal suspend fun getUserProductsResponse(): VirtusizeApiResponse<List<Product>?> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getUserProducts()
        return@withContext VirtusizeApiTask(httpURLConnection)
            .setJsonParser(UserProductJsonParser())
            .setSharedPreferencesHelper(sharedPreferencesHelper)
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving the current user body profile such as age, height, weight and body measurements
     * @return the [VirtusizeApiResponse] with the data class [UserBodyProfile]
     */
    internal suspend fun getUserBodyProfileResponse(): VirtusizeApiResponse<UserBodyProfile?> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getUserBodyProfile()
        VirtusizeApiTask(httpURLConnection)
            .setJsonParser(UserBodyProfileJsonParser())
            .setSharedPreferencesHelper(sharedPreferencesHelper)
            .execute(apiRequest)
    }

    /**
     * Gets the API response for retrieving the recommended size based on the user body profile
     * @param productTypes a list of product types
     * @param storeProduct the store product
     * @param userBodyProfile the user body profile
     * @return the [VirtusizeApiResponse] with the data class [UserBodyProfile]
     */
    internal suspend fun getBodyProfileRecommendedSizeResponse(productTypes: List<ProductType>, storeProduct: Product, userBodyProfile: UserBodyProfile): VirtusizeApiResponse<BodyProfileRecommendedSize?> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getSize(productTypes, storeProduct, userBodyProfile)
        VirtusizeApiTask(httpURLConnection)
            .setJsonParser(BodyProfileRecommendedSizeJsonParser())
            .setSharedPreferencesHelper(sharedPreferencesHelper)
            .execute(apiRequest)
    }

    /**
     * Gets the API response for fetching the i18n localization texts
     * @return the [VirtusizeApiResponse] with the data class [I18nLocalization]
     */
    internal suspend fun getI18nResponse(params: VirtusizeParams): VirtusizeApiResponse<I18nLocalization?> = withContext(
        Dispatchers.IO
    ) {
        val apiRequest = VirtusizeApi.getI18n(params.language ?: (VirtusizeLanguage.values().find { it.value == Locale.getDefault().language } ?: VirtusizeLanguage.EN))
        VirtusizeApiTask(httpURLConnection)
            .setJsonParser(I18nLocalizationJsonParser(context, params.language))
            .execute(apiRequest)
    }
}