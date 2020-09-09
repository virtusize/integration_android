package com.virtusize.libsource.util

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

// The object that wraps Virtusize utility functions
internal object VirtusizeUtils {

    // The context wrapper that is configured to a designated locale
    class ConfiguredContext(base: Context?) : ContextWrapper(base)

    /**
     * Gets the ContextWrapper that is switched to a designated locale
     * @param context the base application Context
     * @param locale the locale to switch to
     */
    fun configureLocale(context: Context, locale: Locale?): ContextWrapper? {
        var updatedContext = context
        val resources = context.resources
        val configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            configuration.locale = locale
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            updatedContext = context.createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return ConfiguredContext(updatedContext)
    }
}