package com.virtusize.android.data.parsers

import com.virtusize.android.data.local.EventName
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeEvent.Undefined
import com.virtusize.android.data.local.VirtusizeEvent.UserAddedProduct
import com.virtusize.android.data.local.VirtusizeEvent.UserAuthData
import com.virtusize.android.data.local.VirtusizeEvent.UserChangedRecommendationType
import com.virtusize.android.data.local.VirtusizeEvent.UserCreatedSilhouette
import com.virtusize.android.data.local.VirtusizeEvent.UserDeletedData
import com.virtusize.android.data.local.VirtusizeEvent.UserDeletedProduct
import com.virtusize.android.data.local.VirtusizeEvent.UserLoggedIn
import com.virtusize.android.data.local.VirtusizeEvent.UserLoggedOut
import com.virtusize.android.data.local.VirtusizeEvent.UserOpenedWidget
import com.virtusize.android.data.local.VirtusizeEvent.UserSawProduct
import com.virtusize.android.data.local.VirtusizeEvent.UserSawWidgetButton
import com.virtusize.android.data.local.VirtusizeEvent.UserSelectedProduct
import com.virtusize.android.data.local.VirtusizeEvent.UserUpdatedBodyMeasurements
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
        EventName.UserSawProduct -> UserSawProduct(data = json)
        EventName.UserSawWidgetButton -> UserSawWidgetButton(data = json)
        EventName.UserOpenedWidget -> UserOpenedWidget(data = json)
        EventName.UserSelectedProduct -> UserSelectedProduct(data = json)
        EventName.UserAddedProduct -> UserAddedProduct(data = json)
        EventName.UserDeletedProduct -> UserDeletedProduct(data = json)
        EventName.UserChangedRecommendationType -> UserChangedRecommendationType(data = json)
        EventName.UserCreatedSilhouette -> UserCreatedSilhouette(data = json)
        EventName.UserUpdatedBodyMeasurements -> UserUpdatedBodyMeasurements(data = json)
        EventName.UserAuthData -> UserAuthData(data = json)
        EventName.UserLoggedIn -> UserLoggedIn(data = json)
        EventName.UserLoggedOut -> UserLoggedOut(data = json)
        EventName.UserDeletedData -> UserDeletedData(data = json)
        null -> Undefined(name = name, data = json)
    }
