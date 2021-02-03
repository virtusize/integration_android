package com.virtusize.libsource.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import com.virtusize.libsource.data.parsers.I18nLocalizationJsonParser

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

/**
 * The Context extension function to get a drawable resource by name in string
 */
internal fun Context.getDrawableResourceByName(drawableName: String): Drawable? {
    val resId = resources.getIdentifier(drawableName, "drawable", packageName)
    if(resId == 0) {
        return null
    }
    return ContextCompat.getDrawable(this, resId)
}

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

fun Context.lifecycleOwner(): LifecycleOwner? {
    var curContext = this
    var maxDepth = 20
    while (maxDepth-- > 0 && curContext !is LifecycleOwner) {
        curContext = (curContext as ContextWrapper).baseContext
    }
    return if (curContext is LifecycleOwner) {
        curContext
    } else {
        null
    }
}

/**
 * The String extension function to trim the text from i18n localization
 */
internal fun String.trimI18nText(trimType: I18nLocalizationJsonParser.TrimType = I18nLocalizationJsonParser.TrimType.ONELINE): String {
    return when (trimType) {
        I18nLocalizationJsonParser.TrimType.ONELINE ->
            replace(I18nConstants.BOLD_START_PLACEHOLDER, "")
                .replace("<br>", "")
                .replace(I18nConstants.BOLD_END_PLACEHOLDER, "")
        I18nLocalizationJsonParser.TrimType.MULTIPLELINES ->
            replace(I18nConstants.BOLD_START_PLACEHOLDER, "<br>")
                .replace(I18nConstants.BOLD_END_PLACEHOLDER, "")
    }
}

/**
 * Integer extension function to convert dp to px
 */
val Int.dpInPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()