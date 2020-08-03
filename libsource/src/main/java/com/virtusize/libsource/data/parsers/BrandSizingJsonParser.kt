package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.BrandSizing
import org.json.JSONObject

class BrandSizingJsonParser : VirtusizeJsonParser {
    override fun parse(json: JSONObject): BrandSizing? {
        val compare = json.optString(FIELD_COMPARE) // "true", "large", or "small"
        val itemBrand = json.optBoolean(FIELD_ITEM_BRAND) // true or false
        if(compare.isBlank()) {
            return null
        }
        return BrandSizing(compare, itemBrand)
    }

    companion object {
        private const val FIELD_COMPARE = "compare"
        private const val FIELD_ITEM_BRAND = "itemBrand"
    }
}