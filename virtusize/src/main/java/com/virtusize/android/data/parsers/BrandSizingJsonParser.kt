package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.BrandSizing
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [BrandSizing] object
 */
internal class BrandSizingJsonParser : VirtusizeJsonParser<BrandSizing> {
    override fun parse(json: JSONObject): BrandSizing? {
        val compare = json.optString(FIELD_COMPARE)
        val itemBrand = json.optBoolean(FIELD_ITEM_BRAND)
        if (compare.isBlank()) {
            return null
        }
        return BrandSizing(compare, itemBrand)
    }

    companion object {
        private const val FIELD_COMPARE = "compare"
        private const val FIELD_ITEM_BRAND = "itemBrand"
    }
}
