package com.virtusize.android.data.local

import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.data.remote.Measurement
import com.virtusize.android.data.remote.UserBodyProfile
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * The kid's body inputs entered in the web widget, in the units expected by
 * the `user-body-measurements-predict` API (height in mm, weight in kg, age in years).
 *
 * The widget keeps these inputs on the client only, so the SDK caches them in
 * [SharedPreferencesHelper] from the `user-selected-gender` and `user-updated-body-measurements`
 * events whose `source` is `kids`.
 *
 * @param gender the kid's gender ("girl" or "boy")
 * @param height the kid's height in millimeters
 * @param weight the kid's weight in kilograms
 * @param age the kid's age in years
 */
data class KidBodyData(
    val gender: String,
    val height: Int,
    val weight: Int,
    val age: Int,
) {
    /**
     * Returns the map that represents the `user-body-measurements-predict` API request body
     */
    fun paramsToMap(): Map<String, Any> =
        linkedMapOf(
            PARAM_GENDER to gender,
            PARAM_HEIGHT to height,
            PARAM_WEIGHT to weight,
            PARAM_AGE to age,
        )

    /**
     * Builds the user body profile used for the kids size recommendation
     * from the measurements returned by the `user-body-measurements-predict` API
     * @param predictedMeasurements the predicted body measurements, keyed by camelCase measurement name
     */
    fun toUserBodyProfile(predictedMeasurements: Map<String, Any?>): UserBodyProfile {
        val bodyData =
            predictedMeasurements.mapNotNull { (name, value) ->
                intValue(value)?.let { Measurement(name, it) }
            }.toSet()
        return UserBodyProfile(
            gender = gender,
            age = age,
            height = height,
            weight = weight.toString(),
            bodyData = bodyData,
            footwearData = emptyMap(),
        )
    }

    companion object {
        /** The `source` value of the widget events that carry kids inputs */
        const val EVENT_SOURCE = "kids"

        /** The gender used when the widget has not reported one */
        const val DEFAULT_GENDER = "girl"

        private const val PARAM_GENDER = "gender"
        private const val PARAM_HEIGHT = "height"
        private const val PARAM_WEIGHT = "weight"
        private const val PARAM_AGE = "age"
        private const val FIELD_SOURCE = "source"

        /**
         * Checks if the widget event comes from the kids flow, whose body inputs the SDK caches
         */
        fun isKidsEvent(eventData: JSONObject?): Boolean = eventData?.optString(FIELD_SOURCE) == EVENT_SOURCE

        /**
         * The cached kid's body data, or null unless age, height and weight have all been received
         */
        fun cached(sharedPreferencesHelper: SharedPreferencesHelper): KidBodyData? {
            val age = sharedPreferencesHelper.getKidAge() ?: return null
            val heightInCm = sharedPreferencesHelper.getKidHeight() ?: return null
            val weight = sharedPreferencesHelper.getKidWeight() ?: return null
            return KidBodyData(
                gender = sharedPreferencesHelper.getKidGender() ?: DEFAULT_GENDER,
                height = heightInCm * 10,
                weight = weight,
                age = age,
            )
        }

        /**
         * Overrides the cached kid's body data with the values present in a widget event
         * (`gender` in "girl"/"boy", `age` in years, `height` in cm, `weight` in kg).
         * Values absent from the event keep their cached value.
         */
        fun cache(
            sharedPreferencesHelper: SharedPreferencesHelper,
            eventData: JSONObject,
        ) {
            eventData.optString(PARAM_GENDER).takeIf { it.isNotEmpty() }?.let {
                sharedPreferencesHelper.storeKidGender(it)
            }
            intValue(eventData.opt(PARAM_AGE))?.let { sharedPreferencesHelper.storeKidAge(it) }
            intValue(eventData.opt(PARAM_HEIGHT))?.let { sharedPreferencesHelper.storeKidHeight(it) }
            intValue(eventData.opt(PARAM_WEIGHT))?.let { sharedPreferencesHelper.storeKidWeight(it) }
        }

        /**
         * Converts a widget event value (a number or a numeric string such as "107") to an integer
         */
        fun intValue(value: Any?): Int? =
            when (value) {
                is Int -> value
                is Number -> value.toDouble().roundToInt()
                is String -> value.trim().toDoubleOrNull()?.roundToInt()
                else -> null
            }
    }
}
