package com.virtusize.libsource.model

import org.json.JSONObject

enum class VirtusizeEvents {
    UserSawProduct,
    UserSawWidgetButton,
    UserOpenedWidget
}

fun VirtusizeEvents.getEventName(): String {
    return when(this) {
        VirtusizeEvents.UserSawProduct -> "user-saw-product"
        VirtusizeEvents.UserSawWidgetButton -> "user-saw-widget-button"
        VirtusizeEvents.UserOpenedWidget -> "user-opened-widget"
    }
}

data class VirtusizeEvent(val name: String, val data: JSONObject? = null)