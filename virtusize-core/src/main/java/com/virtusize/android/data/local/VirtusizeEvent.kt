package com.virtusize.android.data.local

import androidx.annotation.Keep
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
 * This enum contains the event names that Virtusize provides
 */
enum class EventName(val value: String) {
    UserSawProduct("user-saw-product"),
    UserSawWidgetButton("user-saw-widget-button"),
    UserOpenedWidget("user-opened-widget"),
    UserSelectedProduct("user-selected-product"),
    UserAddedProduct("user-added-product"),
    UserDeletedProduct("user-deleted-product"),
    UserChangedRecommendationType("user-changed-recommendation-type"),
    UserCreatedSilhouette("user-created-silhouette"),
    UserUpdatedBodyMeasurements("user-updated-body-measurements"),
    UserAuthData("user-auth-data"),
    UserLoggedIn("user-logged-in"),
    UserLoggedOut("user-logged-out"),
    UserDeletedData("user-deleted-data"),
    ;

    companion object {
        fun valueOf(value: String): EventName? = entries.find { it.value == value }
    }
}

/**
 * Represents a [VirtusizeEvent] that is sent from the Virtusize web view
 * @property name the name of the event
 * @property data the additional data as JSONObject in the event
 */
@Keep
sealed interface VirtusizeEvent {
    val name: String
    val data: JSONObject?

    data class UserSawProduct(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserSawProduct.value
    }

    data class UserSawWidgetButton(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserSawWidgetButton.value
    }

    data class UserOpenedWidget(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserOpenedWidget.value
    }

    data class UserSelectedProduct(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserSelectedProduct.value
    }

    data class UserAddedProduct(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserAddedProduct.value
    }

    data class UserDeletedProduct(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserDeletedProduct.value
    }

    data class UserChangedRecommendationType(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserChangedRecommendationType.value
    }

    data class UserCreatedSilhouette(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserCreatedSilhouette.value
    }

    data class UserUpdatedBodyMeasurements(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserUpdatedBodyMeasurements.value
    }

    data class UserAuthData(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserAuthData.value
    }

    data class UserLoggedIn(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserLoggedIn.value
    }

    data class UserLoggedOut(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserLoggedOut.value
    }

    data class UserDeletedData(override val data: JSONObject? = null) : VirtusizeEvent {
        override val name: String = EventName.UserDeletedData.value
    }

    data class Undefined(override val name: String, override val data: JSONObject? = null) : VirtusizeEvent
}

fun EventName.toVirtusizeEvent(json: JSONObject): VirtusizeEvent =
    EventName.entries.find { entry -> entry.value == name }?.let { entry ->
        when (entry) {
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
        }
    } ?: Undefined(name = name, data = json)

/**
 * This enum contains the size comparison types Virtusize provides
 * Based on a user's selection of the type in the web view, the SDK displays a corresponding InPage comparison
 */
enum class SizeRecommendationType {
    Body,
    CompareProduct,
}
