package com.virtusize.libsource.data

import android.net.Uri
import com.android.volley.Request
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.virtusize.libsource.data.pojo.ProductCheckResponse
import com.virtusize.libsource.model.VirtusizeEnvironment
import com.virtusize.libsource.model.VirtusizeEvent
import com.virtusize.libsource.model.VirtusizeProduct
import com.virtusize.libsource.model.value
import kotlin.random.Random

/**
 * This object represents the Virtusize API
 * @param environment the Virtusize environment that is used in network requests
 * @param apiKey the API key that is unique for every Virtusize client
 * @param browserID the browser ID that is used to identify the user's browser
 * @param language the language code that is set in the user's device
 * @param userId the user ID that is unique from the client system
 */
object VirtusizeApi {
    private var environment = VirtusizeEnvironment.GLOBAL
    private lateinit var apiKey: String
    private lateinit var browserID: String
    private lateinit var language: String
    private lateinit var userId: String

    /**
     * Initializes the VirtusizeApi
     * @param env the Virtusize environment that is used in network requests
     * @param key the API key that is unique for every Virtusize client
     * @param browserID the browser ID that is used to identify the user's browser
     * @param userId the user ID that is unique from the client system
     * @param language the language code that is set in the user's device
     */
    fun init(env: VirtusizeEnvironment,
             key: String, browserID: String, userId: String, language: String) {
        environment = env
        apiKey = key
        this.browserID = browserID
        this.userId = userId
        this.language = language
    }

    // GSON for JSON to Java serialization and vice versa
    private val gson = GsonBuilder().create()

    /**
     * Gets the API request for product check
     * It checks if the product is supported by Virtusize before loading anything on the frontend
     *
     * @param product the VirtusizeProduct for which check needs to be performed
     * @return ApiRequest
     * @see VirtusizeProduct
     * @see ApiRequest
     */
    fun productCheck(product: VirtusizeProduct): ApiRequest  {
        val urlBuilder = Uri.parse(environment.value() + VirtusizeEndpoint.ProductCheck.getUrl())
            .buildUpon()
            .appendQueryParameter("apiKey", apiKey)
            .appendQueryParameter("externalId", product.externalId)
            .appendQueryParameter("version", "1")
        if (userId.isNotEmpty()) {
            urlBuilder.appendQueryParameter("externalUserId", userId)
        }
        val url = urlBuilder.build()
                            .toString()
        return ApiRequest(url, Request.Method.GET)
    }

    /**
     * Gets the Fit Illustrator URL for a VirtusizeProduct
     * @param product the VirtusizeProduct for which Fit Illustrator URL is needed
     * @return the Fit Illustrator URL as String
     */
    fun fitIllustrator(product: VirtusizeProduct): String {
        val urlBuilder = Uri.parse(environment.value() + VirtusizeEndpoint.FitIllustrator.getUrl())
            .buildUpon()
            .appendQueryParameter("detached", "false")
            .appendQueryParameter("browserID", browserID)
            .appendQueryParameter("addToCartEnabled", "false")
            .appendQueryParameter("storeId", product.productCheckData?.data?.storeId.toString())
            .appendQueryParameter("_", Random.nextInt(1519982555).toString())
            .appendQueryParameter("spid", product.productCheckData?.data?.productDataId.toString())
            .appendQueryParameter("lang", language)
            .appendQueryParameter("android", "true")
            .appendQueryParameter("sdk", "android")
            .appendQueryParameter("userId", userId)

        if (userId.isNotEmpty()) {
            urlBuilder.appendQueryParameter("externalUserId", userId)
        }
        return urlBuilder.build().toString()
    }

    /**
     * Gets a API request for sending the image of VirtusizeProduct to the server
     * @param product the VirtusizeProduct whose image needs to be sent to the Virtusize server
     * @return ApiRequest
     */
    fun sendProductImageToBackend(product: VirtusizeProduct): ApiRequest {
        val url = Uri.parse(environment.value() + VirtusizeEndpoint.ProductMetaDataHints.getUrl())
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
        return ApiRequest(url, Request.Method.POST, params)
    }

    /**
     * Gets a API request for logging and sending a Virtusize event to the server
     * @param virtusizeEvent the event to be sent to Virtusize server
     * @param productCheckResponse ProductCheckResponse as the additional payload to be sent to the server along with the event
     * @param deviceOrientation the screen orientation of the device
     * @param screenResolution the screen resolution of the device
     * @param versionCode the SDK version code
     * @return ApiRequest
     * @see VirtusizeEvent
     * @see ProductCheckResponse
     * @see ApiRequest
     */
    fun sendEventToAPI(
        virtusizeEvent: VirtusizeEvent,
        productCheckResponse: ProductCheckResponse?,
        deviceOrientation: String,
        screenResolution: String,
        versionCode: Int
    ): ApiRequest {
        val url = Uri.parse(environment.value() + VirtusizeEndpoint.Events.getUrl())
            .buildUpon()
            .build()
            .toString()
        val params = buildEventPayload(virtusizeEvent, productCheckResponse, deviceOrientation, screenResolution, versionCode)
        return ApiRequest(url, Request.Method.POST, params)
    }

    /**
     * Builds the additional payload to be sent when sending an event to the server
     * @param virtusizeEvent the event to be sent to the Virtusize server
     * @param productCheckResponse ProductCheckResponse as the additional payload to be sent to the server along with the event
     * @param orientation the screen orientation of the device
     * @param resolution the screen resolution of the device
     * @param versionCode the SDK version code
     * @return the MutableMap that holds pairs of keys and values for the payload
     * @see VirtusizeEvent
     * @see ProductCheckResponse
     * @see MutableMap
     */
    private fun buildEventPayload(
        virtusizeEvent: VirtusizeEvent,
        productCheckResponse: ProductCheckResponse?,
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

        val type = object : TypeToken<Map<String, Any>>() {}.type

        if (productCheckResponse != null) {
            productCheckResponse.data.storeId.let {
                params["storeId"] = it.toString()
            }
            productCheckResponse.data.storeName.let {
                params["storeName"] = it
            }
            productCheckResponse.data.productTypeName.let {
                params["storeProductType"] = it
            }
            productCheckResponse.productId.let {
                params["storeProductExternalId"] = it
            }
            productCheckResponse.data.userData.wardrobeActive.let {
                params["wardrobeActive"] = it
            }
            productCheckResponse.data.userData.wardrobeHasM.let {
                params["wardrobeHasM"] = it
            }
            productCheckResponse.data.userData.wardrobeHasP.let {
                params["wardrobeHasP"] = it
            }
            productCheckResponse.data.userData.wardrobeHasR.let {
                params["wardrobeHasR"] = it
            }
        }

        if (!virtusizeEvent.data?.toString().isNullOrEmpty()) {
            val payloadMap = gson.fromJson<Map<String, String>>(virtusizeEvent.data.toString(), type)
            for ((k,v) in payloadMap) params[k] = v
        }
        return params
    }
}

/**
 * This class represents the API request that can be used to perform a network request
 * @param url the URL for a network request
 * @param method the HTTP request method type
 * @param params the MutableMap of query parameters to be sent to the server
 */
data class ApiRequest(val url: String, val method: Int, val params: MutableMap<String, Any> = mutableMapOf())