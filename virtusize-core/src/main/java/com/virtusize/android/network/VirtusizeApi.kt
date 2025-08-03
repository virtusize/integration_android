package com.virtusize.android.network

import android.net.Uri
import com.virtusize.android.data.local.BodyProfileRecommendedSizeParams
import com.virtusize.android.data.local.I18N_URL
import com.virtusize.android.data.local.StoreId
import com.virtusize.android.data.local.VirtusizeEnvironment
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.defaultApiUrl
import com.virtusize.android.data.local.eventApiUrl
import com.virtusize.android.data.local.integrationApiUrl
import com.virtusize.android.data.local.servicesApiUrl
import com.virtusize.android.data.local.sizeRecommendationApiBaseUrl
import com.virtusize.android.data.local.virtusizeUrl
import com.virtusize.android.data.parsers.JsonUtils
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductCheckData
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.UserBodyProfile

/**
 * This enum contains supported HTTP request methods
 */
enum class HttpMethod {
    GET,
    POST,
    DELETE,
}

/**
 * This class represents the API request that can be used to perform a network request
 * @param url the URL for a network request
 * @param method the HTTP request method type
 * @param params the MutableMap of query parameters to be sent to the server
 * @param authorization if it's true, it means you need the auth token to make the API request
 */
data class ApiRequest(
    val url: String,
    val method: HttpMethod,
    val params: Map<String, Any> = mutableMapOf(),
    val authorization: Boolean = false,
)

/**
 * This object represents the Virtusize API
 * @param environment the Virtusize environment that is used in network requests
 * @param apiKey the API key that is unique for every Virtusize client
 * @param userId the user ID that is unique from the client system
 */
object VirtusizeApi {
    const val DEFAULT_AOYAMA_VERSION = "3.4.2"

    private var environment = VirtusizeEnvironment.GLOBAL
    private lateinit var apiKey: String
    private var branch: String? = null

    var currentUserId: String? = null
        private set

    var currentStoreId: StoreId? = null
        private set

    /**
     * Initializes the VirtusizeApi
     * @param env the Virtusize environment that is used in network requests
     * @param key the API key that is unique for every Virtusize client
     * @param userId the user ID that is unique from the client system
     * @param branch the testing environment branch name
     */
    fun init(
        env: VirtusizeEnvironment,
        key: String,
        userId: String,
        branch: String?,
    ) {
        environment = env
        apiKey = key
        currentUserId = userId
        this.branch = branch
    }

    fun setUserId(userId: String) {
        currentUserId = userId
    }

    fun setStoreId(storeId: StoreId) {
        currentStoreId = storeId
    }

    /**
     * Gets the API request for product check
     * It checks if the product is supported by Virtusize before loading anything on the frontend
     *
     * @param product the VirtusizeProduct for which check needs to be performed
     * @return ApiRequest
     * @see VirtusizeProduct
     * @see ApiRequest
     */
    fun productCheck(product: VirtusizeProduct): ApiRequest {
        val urlBuilder =
            Uri.parse(environment.servicesApiUrl() + VirtusizeEndpoint.ProductCheck.path)
                .buildUpon()
                .appendQueryParameter("apiKey", apiKey)
                .appendQueryParameter("externalId", product.externalId)
                .appendQueryParameter("version", "1")
        val url = urlBuilder.build().toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    fun fetchLatestAoyamaVersion(): ApiRequest {
        val url =
            Uri.parse(environment.virtusizeUrl() + VirtusizeEndpoint.LatestAoyamaVersion.path)
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    /**
     * Gets the Virtusize web view URL for a VirtusizeProduct
     *
     * @param version the version of the Virtusize web view
     * @return the Virtusize web view URL as String
     */
    fun getVirtusizeWebViewURL(version: String = DEFAULT_AOYAMA_VERSION): String {
        val urlBuilder =
            Uri.parse(
                environment.virtusizeUrl() +
                    VirtusizeEndpoint.VirtusizeWebView(version = version, branch = branch).path,
            ).buildUpon()
        return urlBuilder.build().toString()
    }

    /**
     * Gets the Virtusize web view URL for a VirtusizeProduct for specific clients
     */
    fun getVirtusizeWebViewURLForSpecificClients(): String {
        val urlBuilder =
            Uri.parse(
                environment.virtusizeUrl() +
                    VirtusizeEndpoint.VirtusizeWebViewForSpecificClients(branch = branch).path,
            ).buildUpon()
        return urlBuilder.build().toString()
    }

    /**
     * Gets a API request for sending the image of VirtusizeProduct to the server
     * @param product the VirtusizeProduct whose image needs to be sent to the Virtusize server
     * @return ApiRequest
     */
    fun sendProductImageToBackend(product: VirtusizeProduct): ApiRequest {
        val url =
            Uri.parse(
                environment.defaultApiUrl() +
                    VirtusizeEndpoint.ProductMetaDataHints.path,
            )
                .buildUpon()
                .build()
                .toString()
        val params = mutableMapOf<String, Any>()
        product.productCheckData?.data?.storeId?.let {
            params["store_id"] = it.toString()
        }
        params["external_id"] = product.externalId
        params["image_url"] = product.imageUrl!!
        params["api_key"] = apiKey
        return ApiRequest(url, HttpMethod.POST, params)
    }

    /**
     * Gets a API request for logging and sending a Virtusize event to the server
     * @param virtusizeEvent the event to be sent to Virtusize server
     * @param productCheckData ProductCheckResponse as the additional payload to be sent to the server along with the event
     * @param deviceOrientation the screen orientation of the device
     * @param screenResolution the screen resolution of the device
     * @param versionName the Virtusize SDK version
     * @return ApiRequest
     * @see VirtusizeEvent
     * @see ProductCheckData
     * @see ApiRequest
     */
    fun sendEventToAPI(
        virtusizeEvent: VirtusizeEvent,
        productCheckData: ProductCheckData?,
        deviceOrientation: String,
        screenResolution: String,
        versionName: String,
    ): ApiRequest {
        val url =
            Uri.parse(environment.eventApiUrl())
                .buildUpon()
                .build()
                .toString()
        val params =
            buildEventPayload(
                virtusizeEvent,
                productCheckData,
                deviceOrientation,
                screenResolution,
                versionName,
            )
        return ApiRequest(url, HttpMethod.POST, params)
    }

    /**
     * Builds the additional payload to be sent when sending an event to the server
     * @param virtusizeEvent the event to be sent to the Virtusize server
     * @param productCheckData ProductCheckResponse as the additional payload to be sent to the server along with the event
     * @param orientation the screen orientation of the device
     * @param resolution the screen resolution of the device
     * @param versionName the Virtusize SDK version
     * @return the MutableMap that holds pairs of keys and values for the payload
     * @see VirtusizeEvent
     * @see ProductCheckData
     * @see MutableMap
     */
    private fun buildEventPayload(
        virtusizeEvent: VirtusizeEvent,
        productCheckData: ProductCheckData?,
        orientation: String,
        resolution: String,
        versionName: String,
    ): MutableMap<String, Any> {
        val params = mutableMapOf<String, Any>()
        params["name"] = virtusizeEvent.name
        params["apiKey"] = apiKey
        params["type"] = "user"
        params["source"] = "integration-android"
        params["userCohort"] = "direct"
        params["widgetType"] = "mobile"
        params["browserOrientation"] = orientation
        params["browserResolution"] = resolution
        params["integrationVersion"] = "$versionName"
        params["snippetVersion"] = "$versionName"

        if (productCheckData != null) {
            productCheckData.data?.storeId?.let {
                params["storeId"] = it.toString()
            }
            productCheckData.data?.storeName?.let {
                params["storeName"] = it
            }
            productCheckData.data?.productTypeName?.let {
                params["storeProductType"] = it
            }
            productCheckData.productId.let {
                params["storeProductExternalId"] = it
            }
        }

        if (!virtusizeEvent.data?.toString().isNullOrEmpty()) {
            virtusizeEvent.data?.optJSONObject("data")?.let {
                JsonUtils.jsonObjectToMap(it)
            }?.let {
                for ((k, v) in it.iterator()) params[k] = v
            }
        }
        return params
    }

    /**
     * Gets a API request for sending the order to the server
     * @param order [VirtusizeOrder]
     * @see ApiRequest
     */
    fun sendOrder(order: VirtusizeOrder): ApiRequest {
        val url =
            Uri.parse(environment.defaultApiUrl() + VirtusizeEndpoint.Orders.path)
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.POST, order.paramsToMap(apiKey, currentUserId).toMutableMap())
    }

    /**
     * Gets a API request for retrieving the specific store info from the API key that is unique to the client
     * @param order [VirtusizeOrder]
     * @see ApiRequest
     */
    fun getStoreInfo(): ApiRequest {
        val url =
            Uri.parse(
                environment.defaultApiUrl() +
                    VirtusizeEndpoint.StoreViewApiKey.path +
                    apiKey,
            )
                .buildUpon()
                .appendQueryParameter("format", "json")
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    /**
     * Gets a API request for retrieving the store product info
     * @param productId the ID of a product
     * @see ApiRequest
     */
    fun getStoreProductInfo(productId: String): ApiRequest {
        val url =
            Uri.parse(
                environment.defaultApiUrl() +
                    VirtusizeEndpoint.StoreProducts.path +
                    productId,
            )
                .buildUpon()
                .appendQueryParameter("format", "json")
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    /**
     * Gets a API request for retrieving the info of all the product types
     * @see ApiRequest
     */
    fun getProductTypes(): ApiRequest {
        val url =
            Uri.parse(environment.servicesApiUrl() + VirtusizeEndpoint.ProductType.path)
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    /**
     * Gets a API request for retrieving i18n localization texts
     * @see ApiRequest
     */
    fun getI18n(language: VirtusizeLanguage): ApiRequest {
        val url =
            Uri.parse(I18N_URL + VirtusizeEndpoint.I18N.path + language.value)
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    /**
     * Gets a API request for retrieving custom i18n texts for specific store
     * @see ApiRequest
     */
    fun getStoreSpecificI18n(storeName: String): ApiRequest {
        val url =
            Uri.parse(
                environment.integrationApiUrl() +
                    VirtusizeEndpoint.StoreI18N(storeName).path,
            )
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    fun getSessions(): ApiRequest {
        val url =
            Uri.parse(environment.defaultApiUrl() + VirtusizeEndpoint.Sessions.path)
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.POST)
    }

    /**
     * Deletes a user
     * @see ApiRequest
     */
    fun deleteUser(): ApiRequest {
        val url =
            Uri.parse(environment.defaultApiUrl() + VirtusizeEndpoint.User.path)
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.DELETE)
    }

    /**
     * Gets a API request for retrieving a list of user products for the current signed-in or anonymous user
     * @see ApiRequest
     */
    fun getUserProducts(): ApiRequest {
        val url =
            Uri.parse(
                environment.defaultApiUrl() + VirtusizeEndpoint.UserProducts.path,
            )
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.GET, authorization = true)
    }

    /**
     * Gets a API request for retrieving the user body profile for the current signed-in or anonymous user
     * @see ApiRequest
     */
    fun getUserBodyProfile(): ApiRequest {
        val url =
            Uri.parse(
                environment.defaultApiUrl() + VirtusizeEndpoint.UserBodyMeasurements.path,
            )
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.GET, authorization = true)
    }

    /**
     * Gets a API request for getting the recommended size based on the user's body profile
     * @param productTypes the list of available [ProductType]
     * @param storeProduct [Product]
     * @param userBodyProfile [UserBodyProfile]
     * @see ApiRequest
     */
    fun getSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile,
    ): ApiRequest {
        val bodyProfileRecommendedSizeParams =
            BodyProfileRecommendedSizeParams(productTypes, storeProduct, userBodyProfile)
        val url =
            Uri.parse("${environment.sizeRecommendationApiBaseUrl()}${VirtusizeEndpoint.GetSize.path}")
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.POST, bodyProfileRecommendedSizeParams.paramsToMap())
    }

    /**
     * Gets a API request for getting the recommended shoe size based on the user's body profile
     * @param productTypes the list of available [ProductType]
     * @param storeProduct [Product]
     * @param userBodyProfile [UserBodyProfile]
     * @see ApiRequest
     */
    fun getShoeSize(
        productTypes: List<ProductType>,
        storeProduct: Product,
        userBodyProfile: UserBodyProfile,
    ): ApiRequest {
        val bodyProfileRecommendedSizeParams =
            BodyProfileRecommendedSizeParams(productTypes, storeProduct, userBodyProfile)
        val url =
            Uri.parse("${environment.sizeRecommendationApiBaseUrl()}${VirtusizeEndpoint.GetShoeSize.path}")
                .buildUpon()
                .build()
                .toString()
        return ApiRequest(url, HttpMethod.POST, bodyProfileRecommendedSizeParams.paramsToMapShoe())
    }
}
