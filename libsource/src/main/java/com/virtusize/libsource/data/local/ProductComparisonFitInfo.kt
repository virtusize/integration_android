package com.virtusize.libsource.data.local

/**
 * This class wraps the fit info for product comparison between a user product and a store product
 * @param fitScore the fit score of this user product to compare with the store product
 * @param isSmaller the value is true if this user product is smaller than the store product
 */
internal data class ProductComparisonFitInfo(
    val fitScore: Float,
    var isSmaller: Boolean?
)
