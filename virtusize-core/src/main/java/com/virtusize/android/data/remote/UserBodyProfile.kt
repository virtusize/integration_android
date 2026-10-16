package com.virtusize.android.data.remote

import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.data.parsers.UserBodyProfileJsonParser
import org.json.JSONArray
import org.json.JSONObject

/**
 * The `user-body-measurements` API response for adult and shoe.
 * The whole object is cached in [SharedPreferencesHelper], separately from [KidBodyProfile].
 *
 * @param gender the user's gender
 * @param age the user's age
 * @param height the user's height in millimeters
 * @param weight the user's weight
 * @param bodyData the user's body measurement data, such as hip, bust, waist and so on.
 * @param footwearData the user's footwear data, such as shoe size and width
 * @param braSize the user's bra size information (country, cup, band)
 * @param origin `api` when filled from `getUserBodyProfile`
 */
data class UserBodyProfile(
    val gender: String,
    val age: Int,
    val height: Int,
    val weight: String,
    val bodyData: Set<Measurement>,
    val footwearData: Map<String, Any>,
    val braSize: Map<String, Any>? = null,
    val origin: String? = null,
) {
    fun toJson(): JSONObject {
        val json =
            JSONObject()
                .put("gender", gender)
                .put("age", age)
                .put("height", height)
                .put("weight", weight)
        val body = JSONObject()
        bodyData.forEach { body.put(it.name, it.millimeter) }
        json.put("bodyData", body)
        json.put("footwearData", mapToJson(footwearData))
        braSize?.let { json.put("braSize", mapToJson(it)) }
        origin?.let { json.put("origin", it) }
        return json
    }

    companion object {
        const val ORIGIN_API = "api"

        fun cached(sharedPreferencesHelper: SharedPreferencesHelper): UserBodyProfile? {
            val json = sharedPreferencesHelper.getUserBodyProfileJson() ?: return null
            return UserBodyProfileJsonParser().parse(JSONObject(json))
        }

        fun cache(
            sharedPreferencesHelper: SharedPreferencesHelper,
            profile: UserBodyProfile,
        ) {
            sharedPreferencesHelper.storeUserBodyProfileJson(profile.toJson().toString())
        }

        fun clearCache(sharedPreferencesHelper: SharedPreferencesHelper) {
            sharedPreferencesHelper.deleteUserBodyProfile()
        }

        fun cachedKidPrediction(sharedPreferencesHelper: SharedPreferencesHelper): UserBodyProfile? {
            val json = sharedPreferencesHelper.getKidPredictedBodyProfileJson() ?: return null
            return UserBodyProfileJsonParser().parse(JSONObject(json))
        }

        fun cacheKidPrediction(
            sharedPreferencesHelper: SharedPreferencesHelper,
            profile: UserBodyProfile,
        ) {
            sharedPreferencesHelper.storeKidPredictedBodyProfileJson(profile.toJson().toString())
        }

        fun clearKidPrediction(sharedPreferencesHelper: SharedPreferencesHelper) {
            sharedPreferencesHelper.deleteKidPredictedBodyProfile()
        }

        private fun mapToJson(map: Map<String, Any>): JSONObject {
            val json = JSONObject()
            map.forEach { (key, value) -> json.put(key, valueToJson(value)) }
            return json
        }

        private fun valueToJson(value: Any?): Any =
            when (value) {
                null -> JSONObject.NULL
                is Map<*, *> ->
                    JSONObject().also { obj ->
                        value.forEach { (k, v) -> obj.put(k.toString(), valueToJson(v)) }
                    }
                is Collection<*> ->
                    JSONArray().also { arr ->
                        value.forEach { arr.put(valueToJson(it)) }
                    }
                else -> value
            }
    }
}
