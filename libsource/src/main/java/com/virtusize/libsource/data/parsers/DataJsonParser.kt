package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.Data
import com.virtusize.libsource.data.remote.JsonUtils
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [Data] object
 */
internal class DataJsonParser: VirtusizeJsonParser {
    override fun parse(json: JSONObject): Data? {
        val validProduct = json.optBoolean(FIELD_VALID_PRODUCT)
        val fetchMetaData = json.optBoolean(FIELD_FETCH_META_DATA)
        val userData = json.optJSONObject(FIELD_USER_DATA)?.let {
            UserDataJsonParser().parse(it)
        }
        val productDataId = json.optInt(FIELD_PRODUCT_DATA_ID)
        val productTypeName = JsonUtils.optString(json, FIELD_PRODUCT_TYPE_NAME)
        val storeName = JsonUtils.optString(json, FIELD_STORE_NAME)
        val storeId = json.optInt(FIELD_STORE_ID)
        val productTypeId = json.optInt(FIELD_PRODUCT_TYPE_ID)
        return Data(validProduct, fetchMetaData, userData, productDataId, productTypeName, storeName, storeId, productTypeId)
    }

    companion object {
        private const val FIELD_VALID_PRODUCT = "validProduct"
        private const val FIELD_FETCH_META_DATA = "fetchMetaData"
        private const val FIELD_USER_DATA = "userData"
        private const val FIELD_PRODUCT_DATA_ID = "productDataId"
        private const val FIELD_PRODUCT_TYPE_NAME = "productTypeName"
        private const val FIELD_STORE_NAME = "storeName"
        private const val FIELD_STORE_ID = "storeId"
        private const val FIELD_PRODUCT_TYPE_ID = "productTypeId"
    }
}