package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.ProductSize
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [ProductSize] object
 */
class ProductSizeJsonParser : VirtusizeJsonParser<ProductSize> {
    override fun parse(json: JSONObject): ProductSize? {
        val name = JsonUtils.optString(json, FIELD_NAME)
        var measurements = setOf<Measurement>()
        json.optJSONObject(FIELD_MEASUREMENTS)?.let { measurementsJsonObject ->
            measurements = JsonUtils.jsonObjectToMap(measurementsJsonObject)
                .filter {
                    it.value as? Int != null
                }.map {
                    Measurement(it.key, it.value as Int)
                }.toSet()
        }
        if(name.isEmpty() && measurements.isEmpty()) {
            return null
        }
        return ProductSize(name, measurements)
    }

    companion object {
        private const val FIELD_NAME = "name"
        private const val FIELD_MEASUREMENTS = "measurements"
    }
}