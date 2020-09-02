package com.virtusize.libsource.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * An abstract class representing the VirtusizeView that is a RelativeLayout
 */
abstract class VirtusizeInPageView(context: Context, attrs: AttributeSet): VirtusizeView, RelativeLayout(context, attrs) {
    abstract fun setupRecommendationText(text: String)
}