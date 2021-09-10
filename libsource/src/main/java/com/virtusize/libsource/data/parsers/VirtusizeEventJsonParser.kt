package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.local.VirtusizeEvent
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [VirtusizeEvent] object
 */
internal class VirtusizeEventJsonParser : VirtusizeJsonParser<VirtusizeEvent> {
    override fun parse(json: JSONObject): VirtusizeEvent? {
        val name = JsonUtils.optString(json, FIELD_NAME)
        return VirtusizeEvent(name, json)
    }

    companion object {
        private const val FIELD_NAME = "eventName"
    }
}
