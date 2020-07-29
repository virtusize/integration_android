package com.virtusize.android.util

import android.view.View
import androidx.test.espresso.IdlingResource
import java.util.*

class ViewVisibilityIdlingResource(
    private val view: View,
    private val visibility: Int
) : IdlingResource {
    private var resourceCallback: IdlingResource.ResourceCallback? = null
    private var isIdle: Boolean = false

    // Give it a unique id to work around an Espresso bug where you cannot register/unregister
    // an idling resource with the same name.
    private val id = UUID.randomUUID().toString()

    override fun getName() = "ViewVisibility $id"

    override fun isIdleNow(): Boolean {
        if (isIdle) {
            return true
        }
        isIdle = view.visibility == visibility
        if (isIdle) {
            resourceCallback?.onTransitionToIdle()
        }

        return isIdle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

}