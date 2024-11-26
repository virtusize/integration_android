package com.virtusize.android.data.remote

import android.content.Context
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.isUnitedArrows
import com.virtusize.android.network.VirtusizeApi
import com.virtusize.android.util.ConfigurationUtils
import com.virtusize.android.util.I18nConstants

/**
 * This class wraps the i18n localization texts
 */
data class I18nLocalization(
    val language: VirtusizeLanguage?,
    val defaultAccessoryText: String,
    val hasProductAccessoryTopText: String,
    val hasProductAccessoryBottomText: String,
    val oneSizeCloseTopText: String,
    val oneSizeSmallerTopText: String,
    val oneSizeLargerTopText: String,
    val oneSizeCloseBottomText: String,
    val oneSizeSmallerBottomText: String,
    val oneSizeLargerBottomText: String,
    val oneSizeWillFitResultText: String,
    val sizeComparisonMultiSizeText: String,
    val willFitResultText: String,
    val willNotFitResultText: String,
    val bodyDataEmptyText: String,
) {
    enum class TrimType {
        ONELINE,
        MULTIPLELINES,
    }

    /**
     * Gets the text for an accessory where the recommendation for product comparison is provided
     */
    internal fun getHasProductAccessoryText(): String {
        return "$hasProductAccessoryTopText ${I18nConstants.BOLD_START_PLACEHOLDER}" +
            "$hasProductAccessoryBottomText${I18nConstants.BOLD_END_PLACEHOLDER}"
    }

    /**
     * Gets the product comparison text for a one-size product
     */
    internal fun getOneSizeProductComparisonText(sizeComparisonRecommendedSize: SizeComparisonRecommendedSize): String {
        if (sizeComparisonRecommendedSize.bestFitScore > 84) {
            return "$oneSizeCloseTopText ${I18nConstants.BOLD_START_PLACEHOLDER}" +
                "$oneSizeCloseBottomText${I18nConstants.BOLD_END_PLACEHOLDER}"
        }
        if (sizeComparisonRecommendedSize.isStoreProductSmaller == true) {
            return "$oneSizeSmallerTopText ${I18nConstants.BOLD_START_PLACEHOLDER}" +
                "$oneSizeSmallerBottomText${I18nConstants.BOLD_END_PLACEHOLDER}"
        }
        return "$oneSizeLargerTopText ${I18nConstants.BOLD_START_PLACEHOLDER}" +
            "$oneSizeLargerBottomText${I18nConstants.BOLD_END_PLACEHOLDER}"
    }

    /**
     * Gets the product comparison text for a multi-size product
     */
    internal fun getMultiSizeProductComparisonText(sizeComparisonRecommendedSizeName: String): String {
        return "$sizeComparisonMultiSizeText ${I18nConstants.BOLD_START_PLACEHOLDER}" +
            "$sizeComparisonRecommendedSizeName${I18nConstants.BOLD_END_PLACEHOLDER}"
    }

    /**
     * Gets the recommendation text for a multi-size product based on a user body profile
     */
    internal fun getMultiSizeBodyProfileText(
        context: Context,
        bodyProfileRecommendedSizeName: String,
    ): String {
        val configuredContext = ConfigurationUtils.getConfiguredContext(context, language)
        // Override the willFitResultText for United Arrows
        val adjustedWillFitResultText =
            if (VirtusizeApi.currentStoreId.isUnitedArrows) {
                configuredContext.getString(com.virtusize.android.core.R.string.inpage_will_fit_result_text_united_arrows)
            } else {
                willFitResultText
            }
        return "$adjustedWillFitResultText ${I18nConstants.BOLD_START_PLACEHOLDER}" +
            "$bodyProfileRecommendedSizeName${I18nConstants.BOLD_END_PLACEHOLDER}"
    }
}
