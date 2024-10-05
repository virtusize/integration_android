package com.virtusize.android.data.parsers

import org.json.JSONObject

object LatestAoyamaVersionJsonParser : VirtusizeJsonParser<String> {
    internal const val FIELD_VERSION = "version"

    override fun parse(json: JSONObject): String = JsonUtils.optString(json, FIELD_VERSION).trimIndent()
}
