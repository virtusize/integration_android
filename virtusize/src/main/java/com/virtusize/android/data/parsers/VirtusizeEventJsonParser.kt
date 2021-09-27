package com.virtusize.android.data.parsers

import com.virtusize.android.data.local.VirtusizeEvent
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
