package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.ProductCheckData
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [ProductCheckData] object
 */
class ProductCheckDataJsonParser : VirtusizeJsonParser<ProductCheckData> {
    override fun parse(json: JSONObject): ProductCheckData {
        val data =
            json.optJSONObject(FIELD_DATA)?.let {
                DataJsonParser().parse(it)
            }
        val productId = JsonUtils.optString(json, FIELD_PRODUCT_ID)
        val name = JsonUtils.optString(json, FIELD_NAME)
        return ProductCheckData(data, productId, name, json.toString())
    }

    companion object {
        private const val FIELD_DATA = "data"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_NAME = "name"
    }
}
