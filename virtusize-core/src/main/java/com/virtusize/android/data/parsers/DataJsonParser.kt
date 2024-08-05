package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.Data
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [Data] object
 */
internal class DataJsonParser : VirtusizeJsonParser<Data> {
    override fun parse(json: JSONObject): Data? {
        val validProduct = json.optBoolean(FIELD_VALID_PRODUCT)
        val fetchMetaData = json.optBoolean(FIELD_FETCH_META_DATA)
        val shouldSeePhTooltip =
            json.optJSONObject(FIELD_USER_DATA)
                ?.optBoolean(FIELD_SHOULD_SEE_PH_TOOLTIP)
                ?: false
        val productDataId = json.optInt(FIELD_PRODUCT_DATA_ID)
        val productTypeName = JsonUtils.optString(json, FIELD_PRODUCT_TYPE_NAME)
        val storeName = JsonUtils.optString(json, FIELD_STORE_NAME)
        val storeId = json.optInt(FIELD_STORE_ID)
        val productTypeId = json.optInt(FIELD_PRODUCT_TYPE_ID)
        return Data(
            validProduct,
            fetchMetaData,
            shouldSeePhTooltip,
            productDataId,
            productTypeName,
            storeName,
            storeId,
            productTypeId,
        )
    }

    companion object {
        private const val FIELD_VALID_PRODUCT = "validProduct"
        private const val FIELD_FETCH_META_DATA = "fetchMetaData"
        private const val FIELD_SHOULD_SEE_PH_TOOLTIP = "should_see_ph_tooltip"
        private const val FIELD_USER_DATA = "userData"
        private const val FIELD_PRODUCT_DATA_ID = "productDataId"
        private const val FIELD_PRODUCT_TYPE_NAME = "productTypeName"
        private const val FIELD_STORE_NAME = "storeName"
        private const val FIELD_STORE_ID = "storeId"
        private const val FIELD_PRODUCT_TYPE_ID = "productTypeId"
    }
}
