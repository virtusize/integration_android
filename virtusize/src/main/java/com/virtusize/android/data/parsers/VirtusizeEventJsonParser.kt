package com.virtusize.android.data.parsers

import com.virtusize.android.data.local.EventName
import com.virtusize.android.data.local.VirtusizeEvent
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [VirtusizeEvent]
 */
internal class VirtusizeEventJsonParser : VirtusizeJsonParser<VirtusizeEvent> {
    override fun parse(json: JSONObject): VirtusizeEvent? {
        val name = JsonUtils.optNullableString(json, FIELD_NAME) ?: return null
        return getVirtusizeEvent(name = name, json = json)
    }

    companion object {
        private const val FIELD_NAME = "eventName"
    }
}

private fun getVirtusizeEvent(
    name: String,
    json: JSONObject,
): VirtusizeEvent =
    when (EventName.get(name)) {
        EventName.UserSawProduct -> VirtusizeEvent.UserSawProduct(data = json)
        EventName.UserSawWidgetButton -> VirtusizeEvent.UserSawWidgetButton(data = json)
        EventName.UserOpenedWidget -> VirtusizeEvent.UserOpenedWidget(data = json)
        EventName.UserSelectedProduct -> VirtusizeEvent.UserSelectedProduct(data = json)
        EventName.UserAddedProduct -> VirtusizeEvent.UserAddedProduct(data = json)
        EventName.UserDeletedProduct -> VirtusizeEvent.UserDeletedProduct(data = json)
        EventName.UserChangedRecommendationType -> VirtusizeEvent.UserChangedRecommendationType(data = json)
        EventName.UserCreatedSilhouette -> VirtusizeEvent.UserCreatedSilhouette(data = json)
        EventName.UserUpdatedBodyMeasurements -> VirtusizeEvent.UserUpdatedBodyMeasurements(data = json)
        EventName.UserAuthData -> VirtusizeEvent.UserAuthData(data = json)
        EventName.UserLoggedIn -> VirtusizeEvent.UserLoggedIn(data = json)
        EventName.UserLoggedOut -> VirtusizeEvent.UserLoggedOut(data = json)
        EventName.UserDeletedData -> VirtusizeEvent.UserDeletedData(data = json)
        EventName.UserClosedWidget -> VirtusizeEvent.UserClosedWidget(data = json)
        EventName.UserClickedStart -> VirtusizeEvent.UserClickedStart(data = json)
        EventName.UserClickedLanguage -> VirtusizeEvent.UserClickedLanguage(data = json)
        null -> VirtusizeEvent.Undefined(name = name, data = json)
    }
