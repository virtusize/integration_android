package com.virtusize.libsource.data.remote

import com.virtusize.libsource.util.I18nConstants

/**
 * This class represents the brand sizing info
 * @param compare the comparing key. eg. "true", "large", or "small"
 * @param itemBrand if the value is true, it is to compare with the brand items. Otherwise, with the items of most brands
 */
data class BrandSizing(
    val compare: String,
    val itemBrand: Boolean
) {
    /**
     * Gets the brand key for the brand comparing InPage texts
     */
    fun getBrandKey(): String {
        return if(itemBrand) {
            I18nConstants.BRAND_ITEM_BRAND_KEY
        } else {
            I18nConstants.BRAND_MOST_BRANDS_KEY
        }
    }
}