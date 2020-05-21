package com.virtusize.libsource.model

import org.json.JSONObject

/**
 * This enum contains all available events in Virtusize API
 */
enum class VirtusizeEvents {
    UserSawProduct,
    UserSawWidgetButton,
    UserOpenedWidget
}

/**
 * Returns the name of the event that it is called upon
 * @return the name of the event
 */
fun VirtusizeEvents.getEventName(): String {
    return when(this) {
        VirtusizeEvents.UserSawProduct -> "user-saw-product"
        VirtusizeEvents.UserSawWidgetButton -> "user-saw-widget-button"
        VirtusizeEvents.UserOpenedWidget -> "user-opened-widget"
    }
}

/**
 * Represents a VirtusizeEvent object
 * @param name the name of the event
 * @param data the additional data as JSONObject in the event
 */
data class VirtusizeEvent(val name: String, val data: JSONObject? = null)