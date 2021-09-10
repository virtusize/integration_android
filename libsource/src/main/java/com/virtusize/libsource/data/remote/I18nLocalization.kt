package com.virtusize.libsource.data.remote

import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.util.I18nConstants

/**
 * This class wraps the i18n localization texts
 */
data class I18nLocalization(
    val defaultAccessoryText: String,
    val hasProductAccessoryTopText: String,
    val hasProductAccessoryBottomText: String,
    val oneSizeCloseTopText: String,
    val oneSizeSmallerTopText: String,
    val oneSizeLargerTopText: String,
    val oneSizeCloseBottomText: String,
    val oneSizeSmallerBottomText: String,
    val oneSizeLargerBottomText: String,
    val bodyProfileOneSizeText: String,
    val sizeComparisonMultiSizeText: String,
    val bodyProfileMultiSizeText: String,
    val defaultNoDataText: String
) {

    /**
     * Gets the text for an accessory where the recommendation for product comparison is provided
     */
    internal fun getHasProductAccessoryText(): String {
        return "$hasProductAccessoryTopText ${I18nConstants.BOLD_START_PLACEHOLDER}$hasProductAccessoryBottomText${I18nConstants.BOLD_END_PLACEHOLDER}"
    }

    /**
     * Gets the product comparison text for a one-size product
     */
    internal fun getOneSizeProductComparisonText(sizeComparisonRecommendedSize: SizeComparisonRecommendedSize): String {
        if (sizeComparisonRecommendedSize.bestFitScore > 84)
            return "$oneSizeCloseTopText ${I18nConstants.BOLD_START_PLACEHOLDER}$oneSizeCloseBottomText${I18nConstants.BOLD_END_PLACEHOLDER}"
        if (sizeComparisonRecommendedSize.isStoreProductSmaller == true)
            return "$oneSizeSmallerTopText ${I18nConstants.BOLD_START_PLACEHOLDER}$oneSizeSmallerBottomText${I18nConstants.BOLD_END_PLACEHOLDER}"
        return "$oneSizeLargerTopText ${I18nConstants.BOLD_START_PLACEHOLDER}$oneSizeLargerBottomText${I18nConstants.BOLD_END_PLACEHOLDER}"
    }

    /**
     * Gets the product comparison text for a multi-size product
     */
    internal fun getMultiSizeProductComparisonText(sizeComparisonRecommendedSizeName: String): String {
        return "$sizeComparisonMultiSizeText ${I18nConstants.BOLD_START_PLACEHOLDER}$sizeComparisonRecommendedSizeName${I18nConstants.BOLD_END_PLACEHOLDER}"
    }

    /**
     * Gets the recommendation text for a multi-size product based on a user body profile
     */
    internal fun getMultiSizeBodyProfileText(bodyProfileRecommendedSizeName: String): String {
        return "$bodyProfileMultiSizeText ${I18nConstants.BOLD_START_PLACEHOLDER}$bodyProfileRecommendedSizeName${I18nConstants.BOLD_END_PLACEHOLDER}"
    }
}
