package com.virtusize.android.util

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import com.virtusize.android.data.local.VirtusizeLanguage
import java.util.Locale

object ConfigurationUtils {
    // The context wrapper that is configured to a designated locale
    class ConfiguredContext(base: Context?) : ContextWrapper(base)

    /**
     * Gets the ContextWrapper that is switched to a designated locale
     * @param context the base application Context
     * @param locale the locale to switch to
     */
    private fun configureLocale(
        context: Context,
        locale: Locale?,
    ): ContextWrapper {
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

    /**
     * Gets configured context base on the language that clients set up with the Virtusize Builder in the application
     */
    fun getConfiguredContext(
        context: Context,
        language: VirtusizeLanguage?,
    ): ContextWrapper {
        return when (language) {
            VirtusizeLanguage.EN -> configureLocale(context, Locale.ENGLISH)
            VirtusizeLanguage.JP -> configureLocale(context, Locale.JAPAN)
            VirtusizeLanguage.KR -> configureLocale(context, Locale.KOREA)
            else -> configureLocale(context, Locale.getDefault())
        }
    }
}
