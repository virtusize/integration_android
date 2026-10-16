package com.virtusize.android.data.remote

import com.virtusize.android.SharedPreferencesHelper
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Kids widget inputs and the predicted measurements built from them.
 * Stored separately from [UserBodyProfile] (the adult/shoe API cache).
 *
 * @param gender the kid's gender
 * @param age the kid's age
 * @param height the kid's height in millimeters
 * @param weight the kid's weight
 * @param bodyData predicted body measurements
 */
data class KidBodyProfile(
    val gender: String,
    val age: Int,
    val height: Int,
    val weight: String,
    val bodyData: Set<Measurement> = emptySet(),
) {
    /** Age, height and weight are present so kids size can be predicted */
    val hasKidPredictInputs: Boolean =
        age > 0 && height > 0 && weight.isNotEmpty() &&
            (weight.toIntOrNull() ?: weight.toDoubleOrNull()?.roundToInt()) != null

    /** Payload for `user-body-measurements-predict` */
    fun predictParams(): Map<String, Any>? {
        if (!hasKidPredictInputs) return null
        val weightKg = weight.toIntOrNull() ?: weight.toDoubleOrNull()?.roundToInt() ?: return null
        return linkedMapOf(
            "gender" to gender.ifEmpty { DEFAULT_GENDER },
            "height" to height,
            "weight" to weightKg,
            "age" to age,
        )
    }

    /**
     * Builds the profile used for the kids size recommendation from predicted measurements.
     * This does not write the adult/shoe [UserBodyProfile] cache.
     */
    fun applyingPredictedMeasurements(predictedMeasurements: Map<String, Any?>): UserBodyProfile {
        val predicted =
            predictedMeasurements.mapNotNull { (name, value) ->
                intValue(value)?.let { Measurement(name, it) }
            }.toSet()
        return UserBodyProfile(
            gender = gender.ifEmpty { DEFAULT_GENDER },
            age = age,
            height = height,
            weight = weight,
            bodyData = predicted,
            footwearData = emptyMap(),
        )
    }

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
        return json
    }

    companion object {
        const val EVENT_SOURCE = "kids"
        const val DEFAULT_GENDER = "girl"

        fun isKidsEvent(eventData: JSONObject?): Boolean = eventData?.optString("source") == EVENT_SOURCE

        fun cached(sharedPreferencesHelper: SharedPreferencesHelper): KidBodyProfile? {
            val json = sharedPreferencesHelper.getKidBodyProfileJson() ?: return null
            val obj = JSONObject(json)
            val bodyData =
                obj.optJSONObject("bodyData")?.let { body ->
                    body.keys().asSequence().mapNotNull { name ->
                        intValue(body.opt(name))?.let { Measurement(name, it) }
                    }.toSet()
                }.orEmpty()
            return KidBodyProfile(
                gender = obj.optString("gender"),
                age = obj.optInt("age"),
                height = obj.optInt("height"),
                weight = obj.optString("weight"),
                bodyData = bodyData,
            )
        }

        fun cache(
            sharedPreferencesHelper: SharedPreferencesHelper,
            profile: KidBodyProfile,
        ) {
            sharedPreferencesHelper.storeKidBodyProfileJson(profile.toJson().toString())
        }

        /**
         * Overrides gender/age/height/weight from a kids widget event. Height is in centimeters
         * in the event and stored in millimeters. Absent values keep their cached value.
         */
        fun cacheKidsEvent(
            sharedPreferencesHelper: SharedPreferencesHelper,
            eventData: JSONObject,
        ) {
            val current = cached(sharedPreferencesHelper)
            val gender =
                eventData.optString("gender").takeIf { it.isNotEmpty() }
                    ?: current?.gender?.takeIf { it.isNotEmpty() }
                    ?: DEFAULT_GENDER
            val age = intValue(eventData.opt("age")) ?: current?.age ?: 0
            val height = intValue(eventData.opt("height"))?.let { it * 10 } ?: current?.height ?: 0
            val weight = intValue(eventData.opt("weight"))?.toString() ?: current?.weight.orEmpty()
            cache(
                sharedPreferencesHelper,
                KidBodyProfile(
                    gender = gender,
                    age = age,
                    height = height,
                    weight = weight,
                ),
            )
        }

        fun clearCache(sharedPreferencesHelper: SharedPreferencesHelper) {
            sharedPreferencesHelper.deleteKidBodyProfile()
        }

        fun intValue(value: Any?): Int? =
            when (value) {
                is Int -> value
                is Number -> value.toDouble().roundToInt()
                is String -> value.trim().toDoubleOrNull()?.roundToInt()
                is JSONObject -> intValue(value.opt("_value"))
                else -> null
            }
    }
}
