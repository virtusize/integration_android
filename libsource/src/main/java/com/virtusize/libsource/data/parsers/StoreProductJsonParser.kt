package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.Product
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [Product] object for a store product
 */
class StoreProductJsonParser : ProductJsonParser() {
    override fun parse(json: JSONObject): Product? {
        val product = super.parse(json)
        product?.externalId = json.optString(FIELD_EXTERNAL_ID)
        json.optJSONObject(FIELD_STORE_PRODUCT_META)?.let {
            product?.storeProductMeta = StoreProductMetaJsonParser().parse(it)
        }
        if(product?.externalId == "") {
            return null
        }
        return product
    }

    companion object {
        private const val FIELD_EXTERNAL_ID = "externalId"
        private const val FIELD_STORE_PRODUCT_META = "storeProductMeta"
    }
}