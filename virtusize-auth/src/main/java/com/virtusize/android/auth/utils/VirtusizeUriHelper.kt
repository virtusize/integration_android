package com.virtusize.android.auth.utils

import android.net.Uri
import androidx.annotation.Keep
import com.virtusize.android.data.parsers.JsonUtils
import org.json.JSONObject

@Keep
object VirtusizeUriHelper {
    private const val QUERY_STATE_KEY = "state"

    /**
     * Checks if the URL is a Virtusize external link to be opened with a browser app
     */
    fun updateStateWithValue(uri: Uri, key: String, value: String): Uri {
        var stateQueryString = uri.getQueryParameter(QUERY_STATE_KEY) ?: "{}"
        if (stateQueryString.isNotEmpty() && !stateQueryString.startsWith("{")) {
            stateQueryString = "{\"state\":\"${stateQueryString}\"}"
        }

        val stateMap =  JsonUtils.jsonObjectToMap(JSONObject(stateQueryString))

        stateMap[key] = value
        val updatedStateQueryString = JSONObject(stateMap.toMap()).toString()

        val newUri = updateUriParameter(
            uri,
            QUERY_STATE_KEY,
            updatedStateQueryString
        )

        return newUri
    }

    fun getStateMap(uri: Uri): Map<String, Any> = uri.getQueryParameter(QUERY_STATE_KEY)
        ?.let { JsonUtils.jsonObjectToMap(JSONObject(it)) }
        ?: emptyMap()

    fun getRedirectUrl(region: String?, env: String?): String {
        return "https://static.api.virtusize.${region ?: "com"}/a/sns-proxy/${env ?: "production"}/sns-auth.html"
    }

    /**
     * Update or add the query parameter with the given key and value.
     *
     * @return updated URI
     */
    private fun updateUriParameter(uri: Uri, key: String, newValue: String): Uri {
        val params = uri.queryParameterNames
        return uri.buildUpon().clearQuery().apply {
            params.forEach { param ->
                appendQueryParameter(
                    param,
                    if (param == key) newValue else uri.getQueryParameter(param)
                )
            }
            if (!params.contains(key)) {
                appendQueryParameter(key, newValue)
            }
        }.build()
    }
}