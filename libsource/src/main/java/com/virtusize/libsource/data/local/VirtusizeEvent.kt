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
    UserAddedProduct,
    UserChangedRecommendationType,
    UserCreatedSilhouette,
    UserUpdatedBodyMeasurements,
    UserAuthData,
    UserLoggedIn,
    UserLoggedOut,
    UserDeletedData
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
        VirtusizeEvents.UserAddedProduct -> "user-added-product"
        VirtusizeEvents.UserChangedRecommendationType -> "user-changed-recommendation-type"
        VirtusizeEvents.UserCreatedSilhouette -> "user-created-silhouette"
        VirtusizeEvents.UserUpdatedBodyMeasurements -> "user-updated-body-measurements"
        VirtusizeEvents.UserAuthData -> "user-auth-data"
        VirtusizeEvents.UserLoggedIn -> "user-logged-in"
        VirtusizeEvents.UserLoggedOut -> "user-logged-out"
        VirtusizeEvents.UserDeletedData -> "user-deleted-data"
    }
}

/**
 * This enum contains the size comparison types Virtusize provides
 * Based on a user's selection of the type in the web view, the SDK displays a corresponding InPage comparison
 */
enum class SizeRecommendationType {
    body,
    compareProduct
}

/**
 * Represents a VirtusizeEvent object
 * @param name the name of the event
 * @param data the additional data as JSONObject in the event
 */
data class VirtusizeEvent(val name: String, val data: JSONObject? = null)