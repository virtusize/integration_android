package com.virtusize.android.ui.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat

/**
 * The Context extension function to get a Typeface by the font file name
 */
internal fun Context.getTypefaceByName(fontFileName: String): Typeface? {
    val resId = resources.getIdentifier(fontFileName, "font", packageName)
    if (resId == 0) {
        return null
    }
    return ResourcesCompat.getFont(this, resId)
}

/**
 * Integer extension function to convert dp to px
 */
val Int.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

/**
 * Float extension function to convert sp to px
 */
val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )
