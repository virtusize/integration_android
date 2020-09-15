package com.virtusize.libsource.data.parsers

import android.content.Context
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.VirtusizeLanguage
import com.virtusize.libsource.data.remote.I18nLocalization
import com.virtusize.libsource.util.VirtusizeUtils
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [I18nLocalization] object
 */
internal class I18nLocalizationJsonParser(val context: Context, private val virtusizeLanguage: VirtusizeLanguage?): VirtusizeJsonParser {

    enum class TrimType {
        ONELINE, MULTIPLELINES
    }

    override fun parse(json: JSONObject): I18nLocalization? {
        val aoyamaJSONObject = json.optJSONObject(FIELD_KEYS)?.optJSONObject(FIELD_APPS)?.optJSONObject(FIELD_AOYAMA)
        val inpageJSONObject = aoyamaJSONObject?.optJSONObject(FIELD_INPAGE)

        val configuredContext = VirtusizeUtils.getConfiguredContext(context, virtusizeLanguage)
        val defaultAccessoryText = inpageJSONObject?.optString(
            FIELD_DEFAULT_ACCESSORY_TEXT,
            configuredContext?.getString(R.string.inpage_default_accessory_text) ?: ""
        ) ?: ""

        val defaultNoDataText = inpageJSONObject?.optString(
            FIELD_NO_DATA_TEXT,
            configuredContext?.getString(R.string.inpage_no_data_text) ?: ""
        ) ?: ""

        return I18nLocalization(
            defaultAccessoryText,
            defaultNoDataText
        )
    }

    companion object {
        private const val FIELD_KEYS = "keys"
        private const val FIELD_APPS = "apps"
        private const val FIELD_AOYAMA = "aoyama"
        private const val FIELD_INPAGE = "inpage"
        private const val FIELD_DEFAULT_ACCESSORY_TEXT = "defaultAccessoryText"
        private const val FIELD_NO_DATA_TEXT = "noDataText"
    }
}