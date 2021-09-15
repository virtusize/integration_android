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
internal class I18nLocalizationJsonParser(
    val context: Context,
    private val virtusizeLanguage: VirtusizeLanguage?
) : VirtusizeJsonParser<I18nLocalization> {

    enum class TrimType {
        ONELINE, MULTIPLELINES
    }

    override fun parse(json: JSONObject): I18nLocalization? {
        val aoyamaJSONObject = json.optJSONObject(FIELD_KEYS)
            ?.optJSONObject(FIELD_APPS)
            ?.optJSONObject(FIELD_AOYAMA)
        val inpageJSONObject = aoyamaJSONObject?.optJSONObject(FIELD_INPAGE)
        val oneSizeJSONObject = inpageJSONObject?.optJSONObject(FIELD_ONE_SIZE)
        val multiSizeJSONObject = inpageJSONObject?.optJSONObject(FIELD_MULTI_SIZE)
        val accessoryJSONObject = inpageJSONObject?.optJSONObject(FIELD_ACCESSORY)

        val configuredContext = VirtusizeUtils.getConfiguredContext(context, virtusizeLanguage)
        val defaultAccessoryText = inpageJSONObject?.optString(
            FIELD_DEFAULT_ACCESSORY_TEXT,
            configuredContext?.getString(R.string.inpage_default_accessory_text) ?: ""
        ) ?: ""

        val hasProductAccessoryTopText = accessoryJSONObject?.optString(
            FIELD_HAS_PRODUCT_LEAD,
            configuredContext?.getString(R.string.inpage_has_product_top_text) ?: ""
        ) ?: ""

        val hasProductAccessoryBottomText = accessoryJSONObject?.optString(
            FIELD_HAS_PRODUCT,
            configuredContext?.getString(R.string.inpage_has_product_bottom_text) ?: ""
        ) ?: ""

        val oneSizeCloseTopText = oneSizeJSONObject?.optString(
            FIELD_CLOSE_LEAD,
            configuredContext?.getString(R.string.inpage_one_size_close_top_text) ?: ""
        ) ?: ""

        val oneSizeSmallerTopText = oneSizeJSONObject?.optString(
            FIELD_SMALLER_LEAD,
            configuredContext?.getString(R.string.inpage_one_size_smaller_top_text) ?: ""
        ) ?: ""

        val oneSizeLargerTopText = oneSizeJSONObject?.optString(
            FIELD_LARGER_LEAD,
            configuredContext?.getString(R.string.inpage_one_size_larger_top_text) ?: ""
        ) ?: ""

        val oneSizeCloseBottomText = oneSizeJSONObject?.optString(
            FIELD_CLOSE,
            configuredContext?.getString(R.string.inpage_one_size_close_bottom_text) ?: ""
        ) ?: ""

        val oneSizeSmallerBottomText = oneSizeJSONObject?.optString(
            FIELD_SMALLER,
            configuredContext?.getString(R.string.inpage_one_size_smaller_bottom_text) ?: ""
        ) ?: ""

        val oneSizeLargerBottomText = oneSizeJSONObject?.optString(
            FIELD_LARGER,
            configuredContext?.getString(R.string.inpage_one_size_larger_bottom_text) ?: ""
        ) ?: ""

        val bodyProfileOneSizeText = oneSizeJSONObject?.optString(
            FIELD_BODY_PROFILE,
            configuredContext?.getString(R.string.inpage_one_size_body_profile_text) ?: ""
        ) ?: ""

        val sizeComparisonMultiSizeText = multiSizeJSONObject?.optString(
            FIELD_SIZE_COMPARISON,
            configuredContext?.getString(R.string.inpage_multi_size_comparison_text) ?: ""
        ) ?: ""

        val bodyProfileMultiSizeText = multiSizeJSONObject?.optString(
            FIELD_BODY_PROFILE,
            configuredContext?.getString(R.string.inpage_multi_size_body_profile_text) ?: ""
        ) ?: ""

        val defaultNoDataText = inpageJSONObject?.optString(
            FIELD_NO_DATA_TEXT,
            configuredContext?.getString(R.string.inpage_no_data_text) ?: ""
        ) ?: ""

        return I18nLocalization(
            defaultAccessoryText,
            hasProductAccessoryTopText,
            hasProductAccessoryBottomText,
            oneSizeCloseTopText,
            oneSizeSmallerTopText,
            oneSizeLargerTopText,
            oneSizeCloseBottomText,
            oneSizeSmallerBottomText,
            oneSizeLargerBottomText,
            bodyProfileOneSizeText,
            sizeComparisonMultiSizeText,
            bodyProfileMultiSizeText,
            defaultNoDataText
        )
    }

    companion object {
        private const val FIELD_KEYS = "keys"
        private const val FIELD_APPS = "apps"
        private const val FIELD_AOYAMA = "aoyama"
        private const val FIELD_INPAGE = "inpage"
        private const val FIELD_ONE_SIZE = "oneSize"
        private const val FIELD_MULTI_SIZE = "multiSize"
        private const val FIELD_ACCESSORY = "accessory"
        private const val FIELD_HAS_PRODUCT = "hasProduct"
        private const val FIELD_HAS_PRODUCT_LEAD = "hasProductLead"
        private const val FIELD_CLOSE = "close"
        private const val FIELD_SMALLER = "smaller"
        private const val FIELD_LARGER = "larger"
        private const val FIELD_CLOSE_LEAD = "closeLead"
        private const val FIELD_SMALLER_LEAD = "smallerLead"
        private const val FIELD_LARGER_LEAD = "largerLead"
        private const val FIELD_BODY_PROFILE = "bodyProfile"
        private const val FIELD_SIZE_COMPARISON = "sizeComparison"
        private const val FIELD_DEFAULT_ACCESSORY_TEXT = "defaultAccessoryText"
        private const val FIELD_NO_DATA_TEXT = "noDataText"
    }
}
