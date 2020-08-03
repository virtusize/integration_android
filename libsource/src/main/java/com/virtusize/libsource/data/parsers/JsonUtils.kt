package com.virtusize.libsource.data.parsers

import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * JSON parsing utility functions
 */
internal object JsonUtils {

    /**
     * Returns the String value mapped by name. If it isn't present, return `null`
     *
     * @param jsonObject the input JSON object
     * @param name the optional field name
     * @return the value stored in the field. If it isn't present, it returns `null`
     */
    internal fun optString(jsonObject: JSONObject, name: String?): String {
        val stringValue = jsonObject.optString(name)
        return if(stringValue == "null") "" else stringValue
    }

    /**
     * Converts a JSONObject to a Map.
     *
     * @param jsonObject a JSONObject to be converted
     * @return a Map representing the input
     */
    internal fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any> {
        // Use Hashtable to keep the insertion order
        val table: MutableMap<String, Any> = Hashtable()
        val keys: Iterator<String> = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            jsonObject.opt(key)?.let { value ->
                table[key] = when (value) {
                    is JSONObject -> jsonObjectToMap(
                        value
                    )
                    is JSONArray -> jsonArrayToList(
                        value
                    )
                    else -> value
                }
            }
        }
        return table
    }

    /**
     * Converts a JSONArray to a List
     *
     * @param array a JSONArray to be converted
     * @return a List representing the input
     */
    internal fun jsonArrayToList(array: JSONArray?): List<Any> {
        val list: MutableList<Any> = ArrayList()
        if(array == null) {
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
            list.add(value)
        }
        return list
    }
}