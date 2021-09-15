package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.ProductMetaDataHints
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [ProductMetaDataHints] object
 */
internal class ProductMetaDataHintsJsonParser : VirtusizeJsonParser<ProductMetaDataHints> {
    override fun parse(json: JSONObject): ProductMetaDataHints? {
        val apiKey = JsonUtils.optString(json, FIELD_API_KEY)
        val imageUrl = JsonUtils.optString(json, FIELD_IMAGE_URL)
        val cloudinaryPublicId = JsonUtils.optString(json, FIELD_CLOUDINARY_PUBLIC_ID)
        val externalProductId = JsonUtils.optString(json, FIELD_EXTERNAL_PRODUCT_ID)
        return ProductMetaDataHints(apiKey, imageUrl, cloudinaryPublicId, externalProductId)
    }

    companion object {
        private const val FIELD_API_KEY = "apiKey"
        private const val FIELD_IMAGE_URL = "imageUrl"
        private const val FIELD_CLOUDINARY_PUBLIC_ID = "cloudinaryPublicId"
        private const val FIELD_EXTERNAL_PRODUCT_ID = "externalProductId"
    }
}
