package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.local.VirtusizeOrderItem
import com.virtusize.libsource.data.remote.StoreProductAdditionalInfo
import com.virtusize.libsource.data.remote.StoreProductMeta
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [StoreProductMeta] object
 */
internal class StoreProductMetaJsonParser: VirtusizeJsonParser<StoreProductMeta> {
    override fun parse(json: JSONObject): StoreProductMeta? {
        val id = json.optInt(FIELD_ID)
        var additionalInfo: StoreProductAdditionalInfo? = null
        json.optJSONObject(FIELD_ADDITIONAL_INFO)?.let {
            additionalInfo = StoreProductAdditionalInfoJsonParser().parse(it)
        }
        val brand = json.optString(FIELD_BRAND)
        val gender = JsonUtils.optNullableString(json, FIELD_GENDER)
        if (id == 0) {
            return null
        }
        return StoreProductMeta(id, additionalInfo, brand, gender)
    }

    private companion object {
        const val FIELD_ID = "id"
        const val FIELD_ADDITIONAL_INFO = "additionalInfo"
        const val FIELD_BRAND = "brand"
        const val FIELD_GENDER = "gender"
    }
}
