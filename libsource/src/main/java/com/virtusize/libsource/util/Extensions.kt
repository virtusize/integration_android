package com.virtusize.libsource.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

/**
 * The Context extension function to get the string by the string resource name
 */
internal fun Context.getStringResourceByName(stringName: String): String? {
    val resId = resources.getIdentifier(stringName, "string", packageName)
    if(resId == 0) {
        return null
    }
    return getString(resId)
}

internal fun Context.getDrawableResourceByName(drawableName: String): Drawable? {
    val resId = resources.getIdentifier(drawableName, "drawable", packageName)
    if(resId == 0) {
        return null
    }
    return ContextCompat.getDrawable(this, resId)
}

internal fun Context.getTypefaceByName(fontFileName: String): Typeface? {
    val resId = resources.getIdentifier(fontFileName, "font", packageName)
    if(resId == 0) {
        return null
    }
    return ResourcesCompat.getFont(this, resId)
}

val Int.dpInPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.pxInDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()