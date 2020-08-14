package com.virtusize.libsource.network

import android.net.Uri
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.parsers.JsonUtils
import com.virtusize.libsource.data.remote.ProductCheck


/**
 * This enum contains supported HTTP request methods
 */
internal enum class HttpMethod {
    GET,
    POST
}

/**
 * This class represents the API request that can be used to perform a network request
 * @param url the URL for a network request
 * @param method the HTTP request method type
 * @param params the MutableMap of query parameters to be sent to the server
 */
internal data class ApiRequest(val url: String, val method: HttpMethod, val params: MutableMap<String, Any> = mutableMapOf())

/**
 * This object represents the Virtusize API
 * @param environment the Virtusize environment that is used in network requests
 * @param apiKey the API key that is unique for every Virtusize client
 * @param userId the user ID that is unique from the client system
 */
internal object VirtusizeApi {
    private var environment = VirtusizeEnvironment.GLOBAL
    private lateinit var apiKey: String
    private lateinit var userId: String

    /**
     * Initializes the VirtusizeApi
     * @param env the Virtusize environment that is used in network requests
     * @param key the API key that is unique for every Virtusize client
     * @param userId the user ID that is unique from the client system
     */
    fun init(env: VirtusizeEnvironment,
             key: String, userId: String) {
        environment = env
        apiKey = key
        this.userId = userId
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
        val urlBuilder = Uri.parse(environment.productDataCheckUrl() + VirtusizeEndpoint.ProductCheck.getPath())
            .buildUpon()
            .appendQueryParameter("apiKey", apiKey)
            .appendQueryParameter("externalId", product.externalId)
            .appendQueryParameter("version", "1")
        val url = urlBuilder.build().toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    /**
     * Gets the Virtusize URL for a VirtusizeProduct
     * @return the Virtusize URL as String
     */
    fun virtusizeURL(): String {
        val urlBuilder = Uri.parse(environment.virtusizeUrl() + VirtusizeEndpoint.Virtusize.getPath())
            .buildUpon()
        return urlBuilder.build().toString()
    }

    /**
     * Gets a API request for sending the image of VirtusizeProduct to the server
     * @param product the VirtusizeProduct whose image needs to be sent to the Virtusize server
     * @return ApiRequest
     */
    fun sendProductImageToBackend(product: VirtusizeProduct): ApiRequest {
        val url = Uri.parse(environment.apiUrl() + VirtusizeEndpoint.ProductMetaDataHints.getPath())
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
     * @param productCheck ProductCheckResponse as the additional payload to be sent to the server along with the event
     * @param deviceOrientation the screen orientation of the device
     * @param screenResolution the screen resolution of the device
     * @param versionCode the SDK version code
     * @return ApiRequest
     * @see VirtusizeEvent
     * @see ProductCheck
     * @see ApiRequest
     */
    fun sendEventToAPI(
        virtusizeEvent: VirtusizeEvent,
        productCheck: ProductCheck?,
        deviceOrientation: String,
        screenResolution: String,
        versionCode: Int
    ): ApiRequest {
        val url = Uri.parse(environment.apiUrl() + VirtusizeEndpoint.Events.getPath())
            .buildUpon()
            .build()
            .toString()
        val params =
            buildEventPayload(
                virtusizeEvent,
                productCheck,
                deviceOrientation,
                screenResolution,
                versionCode
            )
        return ApiRequest(url, HttpMethod.POST, params)
    }

    /**
     * Builds the additional payload to be sent when sending an event to the server
     * @param virtusizeEvent the event to be sent to the Virtusize server
     * @param productCheck ProductCheckResponse as the additional payload to be sent to the server along with the event
     * @param orientation the screen orientation of the device
     * @param resolution the screen resolution of the device
     * @param versionCode the SDK version code
     * @return the MutableMap that holds pairs of keys and values for the payload
     * @see VirtusizeEvent
     * @see ProductCheck
     * @see MutableMap
     */
    private fun buildEventPayload(
        virtusizeEvent: VirtusizeEvent,
        productCheck: ProductCheck?,
        orientation: String,
        resolution: String,
        versionCode: Int
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
        params["integrationVersion"] = "$versionCode"
        params["snippetVersion"] = "$versionCode"

        if (productCheck != null) {
            productCheck.data?.storeId?.let {
                params["storeId"] = it.toString()
            }
            productCheck.data?.storeName?.let {
                params["storeName"] = it
            }
            productCheck.data?.productTypeName?.let {
                params["storeProductType"] = it
            }
            productCheck.productId.let {
                params["storeProductExternalId"] = it
            }
        }

        if (!virtusizeEvent.data?.toString().isNullOrEmpty()) {
            virtusizeEvent.data?.optJSONObject("data")?.let {
                JsonUtils.jsonObjectToMap(it)
            }?.let {
                for ((k,v) in it.iterator()) params[k] = v
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
        val url = Uri.parse(environment.apiUrl() + VirtusizeEndpoint.Orders.getPath())
            .buildUpon()
            .build()
            .toString()
        return ApiRequest(url, HttpMethod.POST, order.paramsToMap(apiKey, userId).toMutableMap())
    }

    /**
     * Gets a API request for retrieve the specific store info from the API key that is unique to the client
     * @param order [VirtusizeOrder]
     * @see ApiRequest
     */
    fun retrieveStoreInfo() : ApiRequest {
        val url = Uri.parse(environment.apiUrl() + VirtusizeEndpoint.StoreViewApiKey.getPath() + apiKey)
            .buildUpon()
            .appendQueryParameter("format", "json")
            .build()
            .toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    fun getStoreProductInfo(productId: String) : ApiRequest {
        val url = Uri.parse(environment.apiUrl() + VirtusizeEndpoint.StoreProduct.getPath() + productId)
            .buildUpon()
            .appendQueryParameter("format", "json")
            .build()
            .toString()
        return ApiRequest(url, HttpMethod.GET)
    }

    fun getProductTypes() : ApiRequest {
        val url = Uri.parse(environment.apiUrl() + VirtusizeEndpoint.ProductType.getPath())
            .buildUpon()
            .build()
            .toString()
        return ApiRequest(url, HttpMethod.GET)
    }
}