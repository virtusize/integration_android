package com.virtusize.android.data.parsers

import com.virtusize.android.data.local.EventName
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.toVirtusizeEvent
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [VirtusizeEvent] object
 */
internal class VirtusizeEventJsonParser : VirtusizeJsonParser<VirtusizeEvent> {
    override fun parse(json: JSONObject): VirtusizeEvent? {
        val name = JsonUtils.optNullableString(json, FIELD_NAME) ?: return null
        val eventName = EventName.valueOf(name)
        return eventName.toVirtusizeEvent(json)
    }

    companion object {
        private const val FIELD_NAME = "eventName"
    }
}
