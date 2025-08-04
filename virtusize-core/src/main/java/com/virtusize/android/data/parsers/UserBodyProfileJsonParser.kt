package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.Measurement
import com.virtusize.android.data.remote.UserBodyProfile
import org.json.JSONObject

class UserBodyProfileJsonParser : VirtusizeJsonParser<UserBodyProfile> {
    override fun parse(json: JSONObject): UserBodyProfile? {
        val gender = JsonUtils.optString(json, FIELD_GENDER)
        val age = json.optInt(FIELD_AGE)
        val height = json.optInt(FIELD_HEIGHT)
        val weight = json.optString(FIELD_WEIGHT)
        var bodyData = setOf<Measurement>()
        var footwearData = mapOf<String, Any>()
        json.optJSONObject(FIELD_BODY_DATA)?.let { bodyDataJsonObject ->
            bodyData = JsonUtils.jsonObjectToMeasurements(bodyDataJsonObject)
        }
        json.optJSONObject(FIELD_FOOTWEAR_DATA)?.let { footwearDataJsonObject ->
            footwearData = JsonUtils.jsonObjectToMap(footwearDataJsonObject)
        }
        if (age == 0 || height == 0 || weight.isBlank() || bodyData.isEmpty()) {
            return null
        }
        return UserBodyProfile(gender, age, height, weight, bodyData, footwearData)
    }

    companion object {
        private const val FIELD_GENDER = "gender"
        private const val FIELD_AGE = "age"
        private const val FIELD_HEIGHT = "height"
        private const val FIELD_WEIGHT = "weight"
        private const val FIELD_BODY_DATA = "bodyData"
        private const val FIELD_FOOTWEAR_DATA = "footwearData"
    }
}
