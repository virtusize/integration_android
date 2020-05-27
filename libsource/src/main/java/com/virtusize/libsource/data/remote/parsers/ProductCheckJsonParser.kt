package com.virtusize.libsource.data.remote.parsers

import android.util.Log
import com.virtusize.libsource.Constants
import com.virtusize.libsource.data.remote.ProductCheck
import org.json.JSONException
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [ProductCheck] object
 */
internal class ProductCheckJsonParser {
    fun parse(json: JSONObject): ProductCheck? {
        try {
            val data = DataJsonParser().parse(json.getJSONObject(FIELD_DATA))
            val productId = json.getString(FIELD_PRODUCT_ID)
            val name = json.getString(FIELD_NAME)
            return ProductCheck(data, productId, name)
        } catch(e: JSONException) {
            Log.e(Constants.LOG_TAG, e.localizedMessage)
        }
        return null
    }

    private companion object {
        private const val FIELD_DATA = "data"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_NAME = "name"
    }
}