package com.virtusize.android.data.parsers

import android.content.Context
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.util.ConfigurationUtils
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [I18nLocalization] object
 */
internal class I18nLocalizationJsonParser(
    val context: Context,
    private val virtusizeLanguage: VirtusizeLanguage?,
) : VirtusizeJsonParser<I18nLocalization> {
    override fun parse(json: JSONObject): I18nLocalization {
        val aoyamaJSONObject = json.optJSONObject(FIELD_KEYS)?.optJSONObject(FIELD_APPS)?.optJSONObject(FIELD_AOYAMA)
        val inpageJSONObject = aoyamaJSONObject?.optJSONObject(FIELD_INPAGE)
        val oneSizeJSONObject = inpageJSONObject?.optJSONObject(FIELD_ONE_SIZE)
        val multiSizeJSONObject = inpageJSONObject?.optJSONObject(FIELD_MULTI_SIZE)
        val accessoryJSONObject = inpageJSONObject?.optJSONObject(FIELD_ACCESSORY)

        val configuredContext = ConfigurationUtils.getConfiguredContext(context, virtusizeLanguage)
        val defaultAccessoryText =
            inpageJSONObject?.optString(
                FIELD_DEFAULT_ACCESSORY_TEXT,
                configuredContext.getString(com.virtusize.android.core.R.string.inpage_default_accessory_text),
            )?.trim().orEmpty()

        val hasProductAccessoryTopText =
            accessoryJSONObject?.optString(
                FIELD_HAS_PRODUCT_LEAD,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_has_product_top_text,
                ),
            )?.trim().orEmpty()

        val hasProductAccessoryBottomText =
            accessoryJSONObject?.optString(
                FIELD_HAS_PRODUCT,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_has_product_bottom_text,
                ),
            )?.trim().orEmpty()

        val oneSizeCloseTopText =
            oneSizeJSONObject?.optString(
                FIELD_CLOSE_LEAD,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_one_size_close_top_text,
                ),
            ).orEmpty()

        val oneSizeSmallerTopText =
            oneSizeJSONObject?.optString(
                FIELD_SMALLER_LEAD,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_one_size_smaller_top_text,
                ),
            )?.trim().orEmpty()

        val oneSizeLargerTopText =
            oneSizeJSONObject?.optString(
                FIELD_LARGER_LEAD,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_one_size_larger_top_text,
                ),
            )?.trim().orEmpty()

        val oneSizeCloseBottomText =
            oneSizeJSONObject?.optString(
                FIELD_CLOSE,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_one_size_close_bottom_text,
                ),
            )?.trim().orEmpty()

        val oneSizeSmallerBottomText =
            oneSizeJSONObject?.optString(
                FIELD_SMALLER,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_one_size_smaller_bottom_text,
                ),
            )?.trim().orEmpty()

        val oneSizeLargerBottomText =
            oneSizeJSONObject?.optString(
                FIELD_LARGER,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_one_size_larger_bottom_text,
                ),
            )?.trim().orEmpty()

        val oneSizeWillFitResultText =
            inpageJSONObject?.optString(
                FIELD_ONE_SIZE_WILL_FIT_RESULT,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_one_size_will_fit_result_text,
                ),
            )?.trim().orEmpty()

        val sizeComparisonMultiSizeText =
            multiSizeJSONObject?.optString(
                FIELD_SIZE_COMPARISON,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_multi_size_comparison_text,
                ),
            )?.trim().orEmpty()

        val willFitResultText =
            inpageJSONObject?.optString(
                FIELD_WILL_FIT_RESULT,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_will_fit_result_text,
                ),
            )?.trim().orEmpty()

        val willNotFitResultText =
            inpageJSONObject?.optString(
                FIELD_WILL_NOT_FIT_RESULT,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_will_not_fit_result_text,
                ),
            )?.trim().orEmpty()

        val defaultNoDataText =
            inpageJSONObject?.optString(
                FIELD_NO_DATA_TEXT,
                configuredContext.getString(
                    com.virtusize.android.core.R.string.inpage_no_data_text,
                ),
            )?.trim().orEmpty()

        return I18nLocalization(
            language = virtusizeLanguage,
            defaultAccessoryText = defaultAccessoryText,
            hasProductAccessoryTopText = hasProductAccessoryTopText,
            hasProductAccessoryBottomText = hasProductAccessoryBottomText,
            oneSizeCloseTopText = oneSizeCloseTopText,
            oneSizeSmallerTopText = oneSizeSmallerTopText,
            oneSizeLargerTopText = oneSizeLargerTopText,
            oneSizeCloseBottomText = oneSizeCloseBottomText,
            oneSizeSmallerBottomText = oneSizeSmallerBottomText,
            oneSizeLargerBottomText = oneSizeLargerBottomText,
            oneSizeWillFitResultText = oneSizeWillFitResultText,
            sizeComparisonMultiSizeText = sizeComparisonMultiSizeText,
            willFitResultText = willFitResultText,
            willNotFitResultText = willNotFitResultText,
            defaultNoDataText = defaultNoDataText,
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
        private const val FIELD_ONE_SIZE_WILL_FIT_RESULT = "oneSizeWillFitResult"
        private const val FIELD_WILL_FIT_RESULT = "willFitResult"
        private const val FIELD_WILL_NOT_FIT_RESULT = "willNotFitResult"
        private const val FIELD_SIZE_COMPARISON = "sizeComparison"
        private const val FIELD_DEFAULT_ACCESSORY_TEXT = "defaultAccessoryText"
        private const val FIELD_NO_DATA_TEXT = "noDataText"
    }
}
