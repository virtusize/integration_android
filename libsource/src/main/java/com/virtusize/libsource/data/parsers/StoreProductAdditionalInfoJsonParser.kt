package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.BrandSizing
import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.ProductSize
import com.virtusize.libsource.data.remote.StoreProductAdditionalInfo
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [StoreProductAdditionalInfo] object
 */
internal class StoreProductAdditionalInfoJsonParser :
    VirtusizeJsonParser<StoreProductAdditionalInfo> {
    override fun parse(json: JSONObject): StoreProductAdditionalInfo? {
        val brand = JsonUtils.optString(json, FIELD_BRAND)
        val gender = JsonUtils.optNullableString(json, FIELD_GENDER)
        var sizes = setOf<ProductSize>()
        json.optJSONObject(FIELD_SIZES)?.let { jsonObject ->
            sizes = JsonUtils.jsonObjectToMap(jsonObject)
                .map { filteredSizeHashMap ->
                    ProductSize(
                        filteredSizeHashMap.key,
                        (filteredSizeHashMap.value as MutableMap<String, Int>)
                            .map {
                                Measurement(it.key, it.value)
                            }.toMutableSet()
                    )
                }.toMutableSet()
        }
        var modelInfo = mutableMapOf<String, Any>()
        json.optJSONObject(FIELD_MODEL_INFO)?.let {
            modelInfo = JsonUtils.jsonObjectToMap(it)
        }
        val fit = JsonUtils.optString(json, FIELD_FIT)
        val style = JsonUtils.optString(json, FIELD_STYLE)
        var brandSizing: BrandSizing? = null
        json.optJSONObject(FIELD_BRAND_SIZING)?.let {
            brandSizing = BrandSizingJsonParser().parse(it)
        }
        if (fit.isBlank() && brandSizing == null) {
            return null
        }
        return StoreProductAdditionalInfo(brand, gender, sizes, modelInfo, fit, style, brandSizing)
    }

    private companion object {
        const val FIELD_BRAND = "brand"
        const val FIELD_GENDER = "gender"
        const val FIELD_SIZES = "sizes"
        const val FIELD_MODEL_INFO = "modelInfo"
        const val FIELD_FIT = "fit"
        const val FIELD_STYLE = "style"
        const val FIELD_BRAND_SIZING = "brandSizing"
    }
}
