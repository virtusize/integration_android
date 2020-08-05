package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.ProductType
import com.virtusize.libsource.data.remote.Weight
import org.json.JSONObject

class ProductTypeJsonParser : VirtusizeJsonParser {
    override fun parse(json: JSONObject): ProductType? {
        val id = json.optInt("id")
        var weights = setOf<Weight>()
        json.optJSONObject("weights")?.let { weightsJsonObject ->
            weights = JsonUtils.jsonObjectToMap(weightsJsonObject).map {
                Weight(it.key, it.value.toString().toFloatOrNull() ?: 0f)
            }.toSet()
        }
        if(id == 0 && weights.isEmpty()) {
            return null
        }
        return ProductType(id, weights)
    }
}