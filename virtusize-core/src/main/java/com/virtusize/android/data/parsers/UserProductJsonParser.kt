package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.Product
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [Product] object for a user product
 */
class UserProductJsonParser : ProductJsonParser() {
    override fun parse(json: JSONObject): Product? {
        val product = super.parse(json)
        product?.isFavorite = json.optBoolean(FIELD_IS_FAVORITE)
        return product
    }

    companion object {
        private const val FIELD_IS_FAVORITE = "isFavorite"
    }
}
