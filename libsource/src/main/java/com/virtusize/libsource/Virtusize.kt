package com.virtusize.libsource

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.virtusize.libsource.data.VirtusizeApi
import com.virtusize.libsource.data.pojo.ProductCheckResponse
import com.virtusize.libsource.data.pojo.ProductMetaDataHintsResponse
import com.virtusize.libsource.model.*
import com.virtusize.libsource.ui.FitIllustratorButton
import org.json.JSONObject
import java.util.*

class Virtusize
    (
    userId: Int?,
    apiKey: String,
    env: VirtusizeEnvironment,
    private val context: Context)
{

    private var browserIdentifier: BrowserIdentifier = BrowserIdentifier( sharedPrefs =
        context.getSharedPreferences(
            "VIRTUSIZE_SHARED_PREFS",
            MODE_PRIVATE
        )
    )

    private val language = Locale.getDefault().displayLanguage
    private lateinit var resolution: String

    init {
        VirtusizeApi.init(env = env, key = apiKey, browserID = browserIdentifier.getBrowserId(), userId = userId?.toString()?:"", language = language)
    }

    private val requestQueue = Volley.newRequestQueue(context)
    private val gson = GsonBuilder().create()

    private fun perform(url: String, callback: CallbackHandler?, method: Int, dataType: Class<*>?, params: MutableMap<String, String> = mutableMapOf()) {
        if (method == Request.Method.GET) {
            val stringRequest = StringRequest(
                method, url,
                Response.Listener<String> { response ->
                    if (response != null && dataType != null) {
                        val result = gson.fromJson(response, dataType)
                        callback?.handleEvent(result)
                    }
                },
                Response.ErrorListener { error ->
                    if (error != null)
                        handleVolleyError(error)
                })

            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest)
        }
        else {
            val jsonBody = JSONObject()

            for ((key, value) in  params) {
                jsonBody.put(key, value)
            }
            val jsonObjectRequest = JsonObjectRequest(method, url, jsonBody,
                Response.Listener { response ->
                    Log.d(Constants.LOG_TAG, response.toString())
                },
                Response.ErrorListener {error ->
                if (error != null)
                    handleVolleyError(error)
                })
            requestQueue.add(jsonObjectRequest)
        }

    }

    private fun handleVolleyError(err: VolleyError) {
        Log.e(Constants.LOG_TAG, err.toString() + err.localizedMessage)
        Toast.makeText(context, err.toString(), Toast.LENGTH_LONG).show()
    }

    fun setupFitButton(fitIllustratorButton: FitIllustratorButton?,
                       virtusizeProduct: VirtusizeProduct) {

        if (fitIllustratorButton == null) {
            throwError(error = VirtusizeError.NullFitButtonError)
            return
        }
        fitIllustratorButton.setup(product = virtusizeProduct)
        val apiRequest = VirtusizeApi.productCheck(product = virtusizeProduct)

        val productValidCheckListener = object : ValidProductFetchHandler {

            override fun onValidProductCheckCompleted(productData: ProductCheckResponse) {
                fitIllustratorButton.setupProduct(productData)
                sendEventToApi(event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName()), withDataProduct = productData)
                if (productData.data?.validProduct == true) {
                    if (productData.data.fetchMetaData == true) {
                        if (fitIllustratorButton.virtusizeProduct?.imageUrl != null)
                            sendProductImageToBackend(product = fitIllustratorButton.virtusizeProduct!!)
                        else
                            throwError(VirtusizeError.ImageUrlNotValid)
                    }
                    sendEventToApi(event = VirtusizeEvent(VirtusizeEvents.UserSawWidgetButton.getEventName()), withDataProduct = productData)
                }
            }
        }

        perform(
            url = apiRequest.url,
            callback = productValidCheckListener,
            method = apiRequest.method,
            dataType = ProductCheckResponse::class.java
        )
    }

    fun sendProductImageToBackend(product: VirtusizeProduct) {
        val apiRequest = VirtusizeApi.sendProductImageToBackend(product = product)
        perform(
            url = apiRequest.url,
            callback = null,
            method = apiRequest.method,
            dataType = ProductMetaDataHintsResponse::class.java,
            params = apiRequest.params
        )
    }

    fun sendEventToApi(event: VirtusizeEvent, withDataProduct: ProductCheckResponse? = null) {
        val defaultDisplay = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        resolution = "${defaultDisplay.height}x${defaultDisplay.width}"

        val apiRequest = VirtusizeApi.sendEventToAPI(
            virtusizeEvent = event,
            productCheckResponse = withDataProduct,
            deviceOrientation = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) context.getString(R.string.landscape)
            else context.getString(R.string.portrait),
            screenResolution = resolution,
            versionCode = context.packageManager
                .getPackageInfo(context.packageName, 0).versionCode
        )
        perform(
            url = apiRequest.url,
            callback = null,
            method = apiRequest.method,
            dataType = null,
            params = apiRequest.params
        )
    }
}

fun throwError(error: VirtusizeError) {
    Log.e(Constants.LOG_TAG, error.message())
    error.throwError()
}

class VirtusizeBuilder {
    private var userId: Int? = null
    private var apiKey: String? = null
    private var env = VirtusizeEnvironment.GLOBAL
    private var context: Context? = null

    // Builder
    fun init(ctx: Context): VirtusizeBuilder {
        context = ctx
        return this
    }

    fun setAppId(id: Int): VirtusizeBuilder {
        this.userId = id
        return this
    }
    fun setApiKey(key: String): VirtusizeBuilder {
        this.apiKey = key
        return this
    }
    fun setEnv(environment: VirtusizeEnvironment): VirtusizeBuilder {
        this.env = environment
        return this
    }

    fun build(): Virtusize {
        if (apiKey.isNullOrEmpty()) {
            throwError(VirtusizeError.ApiKeyNullOrEmpty)
        }
        if (context == null) {
            throwError(VirtusizeError.NullContext)
        }
        return Virtusize(userId = userId, apiKey = apiKey!!, env = env, context = context!!)
    }
}

object Constants {
    const val FRAG_TAG = "FIT_FRAG_TAG"
    const val URL_KEY = "URL_KEY"
    const val LOG_TAG = "VIRTUSIZE"
    const val BID_KEY = "BID_KEY_VIRTUSIZE"
    const val JSBridgeName = "VirtusizeAndroid"
}