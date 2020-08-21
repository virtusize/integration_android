package com.virtusize.libsource.util

import android.content.Context

/**
 * The Context extension function to get the string by the string resource name
 */
fun Context.getStringResourceByName(stringName: String): String? {
    val resId = resources.getIdentifier(stringName, "string", packageName)
    if(resId == 0) {
        return null
    }
    return getString(resId)
}