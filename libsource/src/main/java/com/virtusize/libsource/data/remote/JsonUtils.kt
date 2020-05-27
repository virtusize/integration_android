package com.virtusize.libsource.data.remote

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONObject.NULL

/**
 * JSON parsing utility functions
 */
internal object JsonUtils {
    /**
     * Converts a JSONObject to a Map.
     *
     * @param jsonObject a JSONObject to be converted
     * @return a Map representing the input
     */
    internal fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        val keys: Iterator<String> = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            jsonObject.opt(key)?.let { value ->
                map[key] = when (value) {
                    is JSONObject -> jsonObjectToMap(value)
                    is JSONArray -> jsonArrayToList(value)
                    else -> value
                }
            }
        }
        return map
    }

    /**
     * Converts a JSONArray to a List
     *
     * @param array a JSONArray to be converted
     * @return a List representing the input
     */
    private fun jsonArrayToList(array: JSONArray): List<Any> {
        val list: MutableList<Any> = ArrayList()
        for (i in 0 until array.length()) {
            var value = array[i]
            if (value is JSONArray) {
                value = jsonArrayToList(value)
            } else if (value is JSONObject) {
                value = jsonObjectToMap(value)
            }
            list.add(value)
        }
        return list
    }

}