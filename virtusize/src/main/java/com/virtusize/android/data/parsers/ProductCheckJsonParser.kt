package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.ProductCheck
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [ProductCheck] object
 */
internal class ProductCheckJsonParser : VirtusizeJsonParser<ProductCheck> {
    override fun parse(json: JSONObject): ProductCheck {
        val data = json.optJSONObject(FIELD_DATA)?.let {
            DataJsonParser().parse(it)
        }
        val productId = JsonUtils.optString(json, FIELD_PRODUCT_ID)
        val name = JsonUtils.optString(json, FIELD_NAME)
        return ProductCheck(data, productId, name, json.toString())
    }

    companion object {
        private const val FIELD_DATA = "data"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_NAME = "name"
    }
}
