package com.virtusize.libsource.data.remote

import com.virtusize.libsource.util.I18nConstants

data class BrandSizing(
    val compare: String,
    val itemBrand: Boolean
) {
    fun getBrandKey(): String {
        return if(itemBrand) {
            I18nConstants.BRAND_ITEM_BRAND_KEY
        } else {
            I18nConstants.BRAND_MOST_BRANDS_KEY
        }
    }
}