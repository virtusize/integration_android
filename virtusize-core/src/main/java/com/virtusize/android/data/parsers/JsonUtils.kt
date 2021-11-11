package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.Measurement
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * JSON parsing utility functions
 */
object JsonUtils {

    /**
     * Returns the String value mapped by name. If it isn't present, return an empty string
     *
     * @param jsonObject the input JSON object
     * @param name the optional field name
     * @return the value stored in the field. If it isn't present, it returns an empty string
     */
    fun optString(jsonObject: JSONObject, name: String?): String {
        val stringValue = optNullableString(jsonObject, name)
        return stringValue ?: ""
    }

    /**
     * Returns the String value mapped by name. If it isn't present, return null
     *
     * @param jsonObject the input JSON object
     * @param name the optional field name
     * @return the value stored in the field. If it isn't present, it returns null
     */
    fun optNullableString(jsonObject: JSONObject, name: String?): String? {
        val stringValue = jsonObject.optString(name)
        return if (stringValue == "null") null else stringValue
    }

    /**
     * Converts a JSONObject to a Map.
     *
     * @param jsonObject a JSONObject to be converted
     * @return a Map representing the input
     */
    fun jsonObjectToMap(jsonObject: JSONObject): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        val keys: Iterator<String> = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            jsonObject.opt(key)?.let { value ->
                map[key] = when (value) {
                    is JSONObject -> jsonObjectToMap(
                        value
                    )
                    is JSONArray -> jsonArrayToList(
                        value
                    )
                    else -> {
                        if (value is BigDecimal) {
                            value.toDouble()
                        } else {
                            value
                        }
                    }
                }
            }
        }
        return map
    }

    /**
     * Converts a JSONObject to a set of [Measurement]
     *
     * @param jsonObject a JSONObject to be converted
     * @return a Set representing the input
     */
    fun jsonObjectToMeasurements(jsonObject: JSONObject): Set<Measurement> {
        return jsonObjectToMap(jsonObject)
            .filter {
                it.value as? Int != null
            }.map {
                Measurement(it.key, it.value as Int)
            }.toSet()
    }

    /**
     * Converts a JSONArray to a List
     *
     * @param array a JSONArray to be converted
     * @return a List representing the input
     */
    fun jsonArrayToList(array: JSONArray?): List<Any> {
        val list: MutableList<Any> = ArrayList()
        if (array == null) {
            return list
        }
        for (i in 0 until array.length()) {
            var value = array[i]
            if (value is JSONArray) {
                value =
                    jsonArrayToList(
                        value
                    )
            } else if (value is JSONObject) {
                value =
                    jsonObjectToMap(
                        value
                    )
            }
            if (value is BigDecimal) {
                value = value.toDouble()
            }
            list.add(value)
        }
        return list
    }
}
