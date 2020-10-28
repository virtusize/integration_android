package com.virtusize.libsource.data.remote

/**
 * This class represents the brand sizing info
 * @param compare the comparing key. eg. "true", "large", or "small"
 * @param itemBrand if the value is true, it is to compare with the brand items. Otherwise, with the items of most brands
 */
data class BrandSizing(
    val compare: String,
    val itemBrand: Boolean
)