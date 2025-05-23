package com.virtusize.android.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.webkit.WebResourceRequest
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.virtusize.android.data.remote.I18nLocalization
import org.json.JSONException
import org.json.JSONObject

/**
 * The Context extension function to get the string by the string resource name
 */
fun Context.getStringResourceByName(stringName: String): String? {
    val resId = resources.getIdentifier(stringName, "string", packageName)
    if (resId == 0) {
        return null
    }
    return getString(resId)
}

/**
 * The Context extension function to get a drawable resource by name in string
 */
fun Context.getDrawableResourceByName(drawableName: String): Drawable? {
    val resId = resources.getIdentifier(drawableName, "drawable", packageName)
    if (resId == 0) {
        return null
    }
    return ContextCompat.getDrawable(this, resId)
}

/**
 * The Context extension function to get a Typeface by the font file name
 */
fun Context.getTypefaceByName(fontFileName: String): Typeface? {
    val resId = resources.getIdentifier(fontFileName, "font", packageName)
    if (resId == 0) {
        return null
    }
    return ResourcesCompat.getFont(this, resId)
}

/**
 * The String extension function to trim the text from i18n localization
 */
fun String.trimI18nText(trimType: I18nLocalization.TrimType = I18nLocalization.TrimType.ONELINE): String {
    return when (trimType) {
        I18nLocalization.TrimType.ONELINE ->
            replace(I18nConstants.BOLD_START_PLACEHOLDER, "")
                .replace("<br>", "")
                .replace(I18nConstants.BOLD_END_PLACEHOLDER, "")
        I18nLocalization.TrimType.MULTIPLELINES ->
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
 * The View extension function to get the latest size info when the view size gets changed
 */
inline fun View.onSizeChanged(crossinline runnable: (Int, Int) -> Unit) =
    this.apply {
        addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val rect = Rect(left, top, right, bottom)
            val oldRect = Rect(oldLeft, oldTop, oldRight, oldBottom)
            if (rect.width() != oldRect.width() || rect.height() != oldRect.height()) {
                runnable(rect.width(), rect.height())
            }
        }
    }

/**
 * The TextView extension function to set the width and height for the right drawable of a Button
 */
fun TextView.rightDrawable(
    @DrawableRes id: Int = 0,
    width: Float,
    height: Float,
) {
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
 * Float extension function to convert sp to px
*/
val Float.spToPx: Float
    get() =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            Resources.getSystem().displayMetrics,
        )

/*
 * For the Fit Illustrator web view
 */
val String.isFitIllustratorURL: Boolean
    get() =
        this.contains("virtusize") &&
            this.contains("fit-illustrator") &&
            !this.contains("#") &&
            !this.contains("oauth")

val WebResourceRequest.isFitIllustratorURL: Boolean
    get() = this.url.toString().isFitIllustratorURL

val WebResourceRequest.urlString: String
    get() = "${this.url}"

fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

/**
 * Merge "source" into "current". If fields have equal name, merge them recursively.
 * @return the merged object.
 */
@Throws(JSONException::class)
fun JSONObject.deepMerge(source: JSONObject): JSONObject {
    val target = this
    for (key in source.keys()) {
        val value = source[key]
        if (!target.has(key)) {
            // new value for "key":
            target.put(key, value)
        } else {
            // existing value for "key" - recursively deep merge:
            if (value is JSONObject) {
                target.getJSONObject(key).deepMerge(value)
            } else {
                target.put(key, value)
            }
        }
    }
    return target
}
