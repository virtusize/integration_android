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


object VirtusizeApi {
    private var environment = VirtusizeEnvironment.GLOBAL
    private lateinit var apiKey: String
    private lateinit var bid: String
    private lateinit var lang: String
    private lateinit var userId: String

    fun init(env: VirtusizeEnvironment,
             key: String, _bid: String, _userId: String, _lang: String) {
        environment = env
        apiKey = key
        bid = _bid
        userId = _userId
        lang = _lang
    }

    private val gson = GsonBuilder().create()

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

    fun fitIllustrator(product: VirtusizeProduct): String {
        val urlBuilder = Uri.parse(environment.value() + VirtusizeEndpoint.FitIllustrator.getUrl())
            .buildUpon()
            .appendQueryParameter("detached", "false")
            .appendQueryParameter("bid", bid)
            .appendQueryParameter("addToCartEnabled", "false")
            .appendQueryParameter("storeId", product.data?.data?.storeId.toString())
            .appendQueryParameter("_", Random.nextInt(1519982555).toString())
            .appendQueryParameter("spid", product.data?.data?.productDataId.toString())
            .appendQueryParameter("lang", lang)
            .appendQueryParameter("android", "true")
            .appendQueryParameter("sdk", "1")
            .appendQueryParameter("userId", userId)

        if (userId.isNotEmpty()) {
            urlBuilder.appendQueryParameter("externalUserId", userId)
        }

        return urlBuilder.build().toString()
    }

    fun sendProductImageToBackend(product: VirtusizeProduct): ApiRequest {
        val url = Uri.parse(environment.value() + VirtusizeEndpoint.ProductMetaDataHints.getUrl())
            .buildUpon()
            .build()
            .toString()
        val params = HashMap<String, String>()
        product.data?.data?.storeId?.let {
            params["store_id"] = it.toString()
        }
        params["external_id"] = product.externalId
        params["image_url"] = product.imageUrl!!
        params["api_key"] = apiKey
        return ApiRequest(url, Request.Method.POST, params)
    }

    fun sendEventToAPI(
        virtusizeEvent: VirtusizeEvent,
        data: ProductCheckResponse?,
        orientation: String,
        resolution: String,
        versionCode: Int
    ): ApiRequest {
        val url = Uri.parse(environment.value() + VirtusizeEndpoint.Events.getUrl())
            .buildUpon()
            .build()
            .toString()
        val params = buildEventPayload(virtusizeEvent, data, orientation, resolution, versionCode)
        return ApiRequest(url, Request.Method.POST, params)
    }

    private fun buildEventPayload(
        virtusizeEvent: VirtusizeEvent,
        data: ProductCheckResponse?,
        orientation: String,
        resolution: String,
        versionCode: Int
    ): HashMap<String, String> {
        val params = HashMap<String, String>()
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

        val type = object : TypeToken<Map<String, String>>() {}.type

        if (data != null) {
            data.data?.storeId?.let {
                params["storeId"] = it.toString()
            }
            data.data?.storeName?.let {
                params["storeName"] = it
            }
            data.data?.productTypeName?.let {
                params["storeProductType"] = it
            }
            data.productId?.let {
                params["storeProductExternalId"] = it
            }
            data.data?.userData?.wardrobeActive?.let {
                params["wardrobeActive"] = it.toString()
            }
            data.data?.userData?.wardrobeHasM?.let {
                params["wardrobeHasM"] = it.toString()
            }
            data.data?.userData?.wardrobeHasP?.let {
                params["wardrobeHasP"] = it.toString()
            }
            data.data?.userData?.wardrobeHasR?.let {
                params["wardrobeHasR"] = it.toString()
            }
        }

        if (!virtusizeEvent.data?.toString().isNullOrEmpty()) {
            val payloadMap = gson.fromJson<Map<String, String>>(virtusizeEvent.data.toString(), type)
            for ((k,v) in payloadMap) params[k] = v
        }
        return params
    }
}

data class ApiRequest(val url: String, val method: Int, val params: HashMap<String, String> = hashMapOf())