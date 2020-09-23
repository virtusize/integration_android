package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.BrandSizing
import com.virtusize.libsource.data.remote.StoreProductAdditionalInfo
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [StoreProductAdditionalInfo] object
 */
class StoreProductAdditionalInfoJsonParser : VirtusizeJsonParser<StoreProductAdditionalInfo> {
    override fun parse(json: JSONObject): StoreProductAdditionalInfo? {
        val fit = JsonUtils.optString(json, FIELD_FIT)
        val style = JsonUtils.optString(json, FIELD_STYLE)
        var brandSizing: BrandSizing? = null
        json.optJSONObject(FIELD_BRAND_SIZING)?.let {
            brandSizing = BrandSizingJsonParser().parse(it)
        }
        if(fit.isBlank() && brandSizing == null) {
            return null
        }
        return StoreProductAdditionalInfo(fit, style, brandSizing)
    }

    companion object {
        private const val FIELD_FIT = "fit"
        private const val FIELD_STYLE = "style"
        private const val FIELD_BRAND_SIZING = "brandSizing"
    }
}