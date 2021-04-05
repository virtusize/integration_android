package com.virtusize.libsource.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * An abstract class representing the VirtusizeView that is a RelativeLayout
 */
abstract class VirtusizeInPageView(context: Context, attrs: AttributeSet): VirtusizeView, RelativeLayout(context, attrs) {
    // The text size of the message to be set
    var messageTextSize: Float = -1f
        set(value) {
            field = value
            setStyle()
        }

    // The text size of the Check Size button to be set
    var buttonTextSize: Float = -1f
        set(value) {
            field = value
            setStyle()
        }

    internal abstract fun setStyle()

    /**
     * An abstract function to set up the recommendation text
     */
    internal abstract fun setupRecommendationText(text: String)
    /**
     * An abstract function to show the error screen
     */
    internal abstract fun showErrorScreen()
}