package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductSize
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [Product] object
 */
internal open class ProductJsonParser : VirtusizeJsonParser<Product> {

    override fun parse(json: JSONObject): Product? {
        val id = json.optInt(FIELD_ID)
        var sizes = emptyList<ProductSize>()
        json.optJSONArray(FIELD_SIZES)?.let { jsonArray ->
            sizes = (0 until jsonArray.length())
                .map { idx -> jsonArray.getJSONObject(idx) }
                .mapNotNull { ProductSizeJsonParser().parse(it) }
        }
        val productType = json.optInt(FIELD_PRODUCT_TYPE)
        val name = json.optString(FIELD_NAME)
        val cloudinaryPublicId = JsonUtils.optString(json, FIELD_CLOUDINARY_PUBLIC_ID)
        val storeId = json.optInt(FIELD_STORE_ID)
        if (id == 0 || productType == 0) {
            return null
        }
        return Product(
            id = id,
            sizes = sizes,
            productType = productType,
            name = name,
            cloudinaryPublicId = cloudinaryPublicId,
            storeId = storeId
        )
    }

    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_SIZES = "sizes"
        private const val FIELD_PRODUCT_TYPE = "productType"
        private const val FIELD_NAME = "name"
        private const val FIELD_CLOUDINARY_PUBLIC_ID = "cloudinaryPublicId"
        private const val FIELD_STORE_ID = "store"
    }
}
