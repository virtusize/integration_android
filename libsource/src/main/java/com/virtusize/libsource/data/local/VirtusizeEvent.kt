package com.virtusize.libsource.data.local

import org.json.JSONObject

/**
 * This enum contains all available events in Virtusize API
 */
enum class VirtusizeEvents {
    UserSawProduct,
    UserSawWidgetButton,
    UserOpenedWidget,
    UserSelectedProduct,
    UserOpenedPanelCompare,
    UserAddedProduct,
    UserAuthData,
    UserLoggedIn,
    UserLoggedOut
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
        VirtusizeEvents.UserSelectedProduct -> "user-selected-product"
        VirtusizeEvents.UserOpenedPanelCompare -> "user-opened-panel-compare"
        VirtusizeEvents.UserAddedProduct -> "user-added-product"
        VirtusizeEvents.UserAuthData -> "user-auth-data"
        VirtusizeEvents.UserLoggedIn -> "user-logged-in"
        VirtusizeEvents.UserLoggedOut -> "user-logged-out"
    }
}

/**
 * Represents a VirtusizeEvent object
 * @param name the name of the event
 * @param data the additional data as JSONObject in the event
 */
data class VirtusizeEvent(val name: String, val data: JSONObject? = null)