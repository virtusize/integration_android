package com.virtusize.android.data.parsers

import org.json.JSONObject

/**
 * The interface for the different data type of the JSON parser
 */
internal interface VirtusizeJsonParser<out T> {
    fun parse(json: JSONObject): T?
}
