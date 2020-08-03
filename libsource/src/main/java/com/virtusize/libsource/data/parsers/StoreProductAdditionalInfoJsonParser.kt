package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.BrandSizing
import com.virtusize.libsource.data.remote.StoreProductAdditionalInfo
import org.json.JSONObject

class StoreProductAdditionalInfoJsonParser : VirtusizeJsonParser {
    override fun parse(json: JSONObject): StoreProductAdditionalInfo? {
        val fit = json.optString(FIELD_FIT)
        var brandSizing: BrandSizing? = null
        json.optJSONObject(FIELD_BRAND_SIZING)?.let {
            brandSizing = BrandSizingJsonParser().parse(it)
        }
        if(fit.isBlank() && brandSizing == null) {
            return null
        }
        return StoreProductAdditionalInfo(fit, brandSizing)
    }

    companion object {
        private const val FIELD_FIT = "fit"
        private const val FIELD_BRAND_SIZING = "brandSizing"
    }
}