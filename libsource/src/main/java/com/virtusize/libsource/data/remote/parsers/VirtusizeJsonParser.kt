package com.virtusize.libsource.data.remote.parsers

import org.json.JSONObject

/**
 * The interface for the different data type of the JSON parser
 */
internal interface VirtusizeJsonParser {
    fun parse(json: JSONObject): Any?
}