package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.ProductType
import com.virtusize.libsource.data.remote.Weight
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [ProductType] object
 */
class ProductTypeJsonParser : VirtusizeJsonParser {
    override fun parse(json: JSONObject): ProductType? {
        val id = json.optInt(FIELD_ID)
        val name = json.optString(FIELD_NAME)
        var weights = setOf<Weight>()
        json.optJSONObject(FIELD_WEIGHTS)?.let { weightsJsonObject ->
            weights = JsonUtils.jsonObjectToMap(weightsJsonObject).map {
                Weight(it.key, it.value.toString().toFloatOrNull() ?: 0f)
            }.toSet()
        }
        if(id == 0 && weights.isEmpty()) {
            return null
        }
        return ProductType(id, name, weights)
    }

    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_NAME = "name"
        private const val FIELD_WEIGHTS = "weights"
    }
}