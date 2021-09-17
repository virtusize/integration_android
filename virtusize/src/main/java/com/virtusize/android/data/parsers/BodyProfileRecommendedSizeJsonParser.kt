package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.BodyProfileRecommendedSize
import com.virtusize.android.data.remote.Product
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [BodyProfileRecommendedSize] object
 * @param product the store product that is associated with this recommendation
 */
internal class BodyProfileRecommendedSizeJsonParser(private val product: Product) :
    VirtusizeJsonParser<BodyProfileRecommendedSize> {

    override fun parse(json: JSONObject): BodyProfileRecommendedSize? {
        val sizeName = json.getString(FIELD_NAME)
        return BodyProfileRecommendedSize(product, sizeName)
    }

    private companion object {
        const val FIELD_NAME = "sizeName"
    }
}
