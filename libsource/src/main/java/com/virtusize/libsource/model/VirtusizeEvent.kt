package com.virtusize.libsource.model

import org.json.JSONObject

/**
 * This enum contains all available events in Virtusize
 */
enum class VirtusizeEvents {
    UserSawProduct,
    UserSawWidgetButton,
    UserOpenedWidget
}

/**
 * This method returns the name of event that it is called upon
 * @return Event name
 */
fun VirtusizeEvents.getEventName(): String {
    return when(this) {
        VirtusizeEvents.UserSawProduct -> "user-saw-product"
        VirtusizeEvents.UserSawWidgetButton -> "user-saw-widget-button"
        VirtusizeEvents.UserOpenedWidget -> "user-opened-widget"
    }
}

/**
 * This class represents VirtusizeEvent object
 * @param name is the name of the event
 * @param data is the additional data as JSONObject in the event
 */
data class VirtusizeEvent(val name: String, val data: JSONObject? = null)