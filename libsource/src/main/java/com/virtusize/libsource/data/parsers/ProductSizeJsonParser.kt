package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.ProductSize
import org.json.JSONObject

class ProductSizeJsonParser : VirtusizeJsonParser {
    override fun parse(json: JSONObject): ProductSize? {
        val name = json.optString(FIELD_NAME)
        var measurements = setOf<Measurement>()
        json.optJSONObject(FIELD_MEASUREMENTS)?.let { measurementsJsonObject ->
            measurements = JsonUtils.jsonObjectToMap(measurementsJsonObject).map {
                Measurement(it.key, it.value as? Int ?: 0)
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