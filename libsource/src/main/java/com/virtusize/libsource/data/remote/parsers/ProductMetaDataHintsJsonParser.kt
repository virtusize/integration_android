package com.virtusize.libsource.data.remote.parsers

import android.util.Log
import com.virtusize.libsource.Constants
import com.virtusize.libsource.data.remote.ProductMetaDataHints
import org.json.JSONException
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [ProductMetaDataHints] object
 */
internal class ProductMetaDataHintsJsonParser: VirtusizeJsonParser {
    override fun parse(json: JSONObject): ProductMetaDataHints? {
        try {
            val apiKey = json.getString(FIELD_API_KEY)
            val imageUrl = json.getString(FIELD_IMAGE_URL)
            val cloudinaryPublicId = json.getString(FIELD_CLOUDINARY_PUBLIC_ID)
            val externalProductId = json.getString(FIELD_EXTERNAL_PRODUCT_ID)
            return ProductMetaDataHints(apiKey, imageUrl, cloudinaryPublicId, externalProductId)
        } catch(e: JSONException) {
            Log.e(Constants.LOG_TAG, e.localizedMessage)
        }
        return null
    }

    private companion object {
        private const val FIELD_API_KEY = "apiKey"
        private const val FIELD_IMAGE_URL = "imageUrl"
        private const val FIELD_CLOUDINARY_PUBLIC_ID = "cloudinaryPublicId"
        private const val FIELD_EXTERNAL_PRODUCT_ID = "externalProductId"
    }
}