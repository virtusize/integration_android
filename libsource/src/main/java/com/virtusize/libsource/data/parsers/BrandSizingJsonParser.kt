package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.BrandSizing
import org.json.JSONObject

class BrandSizingJsonParser : VirtusizeJsonParser {
    override fun parse(json: JSONObject): BrandSizing? {
        val compare = json.optString("compare")
        val itemBrand = json.optBoolean("itemBrand")
        return BrandSizing(compare, itemBrand)
    }
}