package com.virtusize.libsource.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
 * The Enum extension function to convert a string to an enum type safely
 */
inline fun <reified T : Enum<T>> valueOf(type: String): T? {
    return try {
        java.lang.Enum.valueOf(T::class.java, type)
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * The TextView extension function to set the width and height for the right drawable of a Button
 */
internal fun TextView.rightDrawable(@DrawableRes id: Int = 0, width: Float, height: Float) {
    val drawable = ContextCompat.getDrawable(context, id)
    drawable?.setBounds(0, 0, width.toInt(), height.toInt())
    this.setCompoundDrawables(null, null, drawable, null)
}

/**
 * Integer extension function to convert dp to px
 */
val Int.dpInPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * Float extension function to convert dp to px
 */
val Float.dpInPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * Float extension function to convert sp to px
*/
val Float.spToPx: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )
