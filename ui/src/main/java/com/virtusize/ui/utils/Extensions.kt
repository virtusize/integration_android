package com.virtusize.ui.utils

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

/**
 * The Context extension function to get a Typeface by the font file name
 */
internal fun Context.getTypefaceByName(fontFileName: String): Typeface? {
    val resId = resources.getIdentifier(fontFileName, "font", packageName)
    if(resId == 0) {
        return null
    }
    return ResourcesCompat.getFont(this, resId)
}