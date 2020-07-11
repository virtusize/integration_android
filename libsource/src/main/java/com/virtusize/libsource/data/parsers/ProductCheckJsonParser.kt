package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.JsonUtils
import com.virtusize.libsource.data.remote.ProductCheck
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [ProductCheck] object
 */
internal class ProductCheckJsonParser: VirtusizeJsonParser {
    override fun parse(json: JSONObject): ProductCheck? {
        val data = json.optJSONObject(FIELD_DATA)?.let {
            DataJsonParser().parse(it)
        }
        val productId = JsonUtils.optString(json, FIELD_PRODUCT_ID)
        val name = JsonUtils.optString(json, FIELD_NAME)
        return ProductCheck(data, productId, name)
    }

    private companion object {
        private const val FIELD_DATA = "data"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_NAME = "name"
    }
}