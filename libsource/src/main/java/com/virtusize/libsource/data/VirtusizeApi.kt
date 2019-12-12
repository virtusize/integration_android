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
 */
object VirtusizeApi {
    private var environment = VirtusizeEnvironment.GLOBAL // Virtusize environment to be used in network requests
    private lateinit var apiKey: String // API Key that is unique for every Virtusize client
    private lateinit var browserID: String // BrowserID for this application
    private lateinit var language: String // Language set in user's device
    private lateinit var userId: String // UserId userId corresponding to the app user

    /**
     * This method is used to initialize VirtusizeApi
     * @param env Virtusize environment to be used in network requests
     * @param key API Key that is unique for every Virtusize client
     * @param browserID BrowserID for this application
     * @param userId userId corresponding to the app user
     * @param language Language set in device
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
     * This method is used get api request for product check
     * @param product VirtusizeProduct for which check needs to be performed
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
     * This method is used to get Fit Illustrator URL for a VirtusizeProduct
     * @param product VirtusizeProduct for which Fit Illustrator URL is needed
     * @return Fit Illustrator URL as String
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
     * This method is used to get api request for sending image of VirtusizeProduct to server
     * @param product VirtusizeProduct whose image needs to be sent to Virtusize server
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
     * This method is used to get api request for sending event to server
     * @param virtusizeEvent Event to be sent to Virtusize server
     * @param productCheckResponse ProductCheckResponse as additional payload to be sent to server with event
     * @param deviceOrientation Device's screen orientation
     * @param screenResolution Device's screen resolution
     * @param versionCode Version Code
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
     * This method is used to build additional payload to be sent when sending event to server
     * @param virtusizeEvent Event to be sent to Virtusize server
     * @param productCheckResponse ProductCheckResponse as additional payload to be sent to server with event
     * @param orientation Device's screen orientation
     * @param resolution Device's screen resolution
     * @param versionCode Version Code
     * @return MutableMap of params for payload
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
 * This class represents Api request that can be used to perform network request
 * @param url URL for network request
 * @param method Request method type
 * @param params MutableMap of parameters to be sent as request body
 */
data class ApiRequest(val url: String, val method: Int, val params: MutableMap<String, Any> = mutableMapOf())