package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.ProductSize
import com.virtusize.libsource.data.remote.StoreProduct
import com.virtusize.libsource.data.remote.StoreProductMeta
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [StoreProduct] object
 */
internal class StoreProductJsonParser: VirtusizeJsonParser {

    override fun parse(json: JSONObject): StoreProduct? {
        val id = json.optInt(FIELD_ID)
        var sizes = emptyList<ProductSize>()
        json.optJSONArray(FIELD_SIZES)?.let { jsonArray ->
            sizes = (0 until jsonArray.length())
                .map { idx -> jsonArray.getJSONObject(idx) }
                .mapNotNull { ProductSizeJsonParser().parse(it) }
        }
        val externalId = json.optString(FIELD_EXTERNAL_ID)
        val productType = json.optInt(FIELD_PRODUCT_TYPE)
        val name = json.optString(FIELD_NAME)
        var cloudinaryPublicId = json.optString(FIELD_CLOUDINARY_PUBLIC_ID)
        val storeId = json.optInt(FIELD_STORE_ID)
        var storeProductMeta: StoreProductMeta? = null
        json.optJSONObject(FIELD_STORE_PRODUCT_META)?.let {
            storeProductMeta = StoreProductMetaJsonParser().parse(it)
        }
        if (id == 0 || externalId == "" || productType == 0 || storeId == 0) {
            return null
        }
        return StoreProduct(id, sizes, externalId, productType, name, cloudinaryPublicId, storeId, storeProductMeta)
    }

    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_SIZES = "sizes"
        private const val FIELD_EXTERNAL_ID = "externalId"
        private const val FIELD_PRODUCT_TYPE = "productType"
        private const val FIELD_NAME = "name"
        private const val FIELD_CLOUDINARY_PUBLIC_ID = "cloudinaryPublicId"
        private const val FIELD_STORE_ID = "store"
        private const val FIELD_STORE_PRODUCT_META = "storeProductMeta"
    }
}