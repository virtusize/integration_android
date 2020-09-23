package com.virtusize.libsource.data.parsers

import android.util.Log
import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.UserBodyProfile
import com.virtusize.libsource.network.VirtusizeEndpoint
import com.virtusize.libsource.network.getPath
import org.json.JSONObject

class UserBodyProfileJsonParser: VirtusizeJsonParser<UserBodyProfile> {
    override fun parse(json: JSONObject): UserBodyProfile? {
        val gender = JsonUtils.optString(json, FIELD_GENDER)
        val age = json.optInt(FIELD_AGE)
        val height = json.optInt(FIELD_HEIGHT)
        val weight = json.optString(FIELD_WEIGHT)
        var bodyData = setOf<Measurement>()
        json.optJSONObject(FIELD_BODY_DATA)?.let { bodyDataJsonObject ->
            bodyData = JsonUtils.jsonObjectToMeasurements(bodyDataJsonObject)
        }
        if(age == 0 || height == 0 || weight.isBlank() || bodyData.isEmpty()) {
            return null
        }
        return UserBodyProfile(gender, age, height, weight, bodyData)
    }

    companion object {
        private const val FIELD_GENDER = "gender"
        private const val FIELD_AGE = "age"
        private const val FIELD_HEIGHT = "height"
        private const val FIELD_WEIGHT = "weight"
        private const val FIELD_BODY_DATA = "bodyData"
    }
}
