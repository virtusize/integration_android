package com.virtusize.libsource.data.parsers

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
        if (id == 0) {
            return null
        }
       return StoreProductMeta(id, additionalInfo)
    }

    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_ADDITIONAL_INFO = "additionalInfo"
    }
}
