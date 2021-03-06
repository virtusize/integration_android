package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.BodyProfileRecommendedSize
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [BodyProfileRecommendedSize] object
 */
class BodyProfileRecommendedSizeJsonParser : VirtusizeJsonParser<BodyProfileRecommendedSize> {

    override fun parse(json: JSONObject): BodyProfileRecommendedSize? {
        val sizeName = json.getString(FIELD_NAME)
        return BodyProfileRecommendedSize(sizeName)
    }

    private companion object {
        const val FIELD_NAME = "sizeName"
    }
}