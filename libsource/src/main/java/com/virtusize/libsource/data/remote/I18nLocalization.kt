package com.virtusize.libsource.data.remote

import com.virtusize.libsource.util.I18nConstants

/**
 * This class wraps the i18n localization texts
 */
data class I18nLocalization(
    val defaultText: String,
    val defaultAccessoryText: String,
    val sizingItemBrandLargeText: String,
    val sizingItemBrandTrueText: String,
    val sizingItemBrandSmallText: String,
    val sizingMostBrandsLargeText: String,
    val sizingMostBrandsTrueText: String,
    val sizingMostBrandsSmallText: String,
    val fitLooseText: String,
    val fitRegularText: String,
    val fitTightText: String
) {
    /**
     * Gets the sizing text based on the brand sizing info
     */
    internal fun getSizingText(brandSizing: BrandSizing): String {
        if (brandSizing.itemBrand) {
            when (brandSizing.compare) {
                I18nConstants.SIZING_LARGE_KEY -> {
                    return sizingItemBrandLargeText
                }
                I18nConstants.SIZING_TRUE_KEY -> {
                    return sizingItemBrandTrueText
                }
                I18nConstants.SIZING_SMALL_KEY -> {
                    return sizingItemBrandSmallText
                }
            }
        } else  {
            when (brandSizing.compare) {
                I18nConstants.SIZING_LARGE_KEY -> {
                    return sizingMostBrandsLargeText
                }
                I18nConstants.SIZING_TRUE_KEY -> {
                    return sizingMostBrandsTrueText
                }
                I18nConstants.SIZING_SMALL_KEY -> {
                    return sizingMostBrandsSmallText
                }
            }
        }
        return "apps.aoyama.detailsScreen.sizing.${brandSizing.getBrandKey()}.${brandSizing.compare}"
    }

    /**
     * Gets the general fit text based on the general fit key
     */
    internal fun getFitText(generalFitKey: String?): String {
        return when (generalFitKey) {
            I18nConstants.GENERAL_FIT_LOOSE_KEY -> fitLooseText
            I18nConstants.GENERAL_FIT_REGULAR_KEY -> fitRegularText
            I18nConstants.GENERAL_FIT_TIGHT_KEY -> fitTightText
            else -> "apps.aoyama.detailsScreen.fit.$generalFitKey"
        }
    }
}