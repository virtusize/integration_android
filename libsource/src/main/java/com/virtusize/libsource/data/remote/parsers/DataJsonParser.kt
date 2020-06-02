package com.virtusize.libsource.data.remote.parsers

import android.util.Log
import com.virtusize.libsource.Constants
import com.virtusize.libsource.data.remote.Data
import org.json.JSONException
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [Data] object
 */
internal class DataJsonParser: VirtusizeJsonParser {
    override fun parse(json: JSONObject): Data? {
        try {
            val validProduct = json.getBoolean(FIELD_VALID_PRODUCT)
            val fetchMetaData = json.getBoolean(FIELD_FETCH_META_DATA)
            val userData = UserDataJsonParser().parse(json.getJSONObject(FIELD_USER_DATA))
            val productDataId = json.getInt(FIELD_PRODUCT_DATA_ID)
            val productTypeName = json.getString(FIELD_PRODUCT_TYPE_NAME)
            val storeName = json.getString(FIELD_STORE_NAME)
            val storeId = json.getInt(FIELD_STORE_ID)
            val productTypeId = json.getInt(FIELD_PRODUCT_TYPE_ID)
            return Data(validProduct, fetchMetaData, userData, productDataId, productTypeName, storeName, storeId, productTypeId)
        } catch(e: JSONException) {
            Log.e(Constants.LOG_TAG, e.localizedMessage)
        }
        return null
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