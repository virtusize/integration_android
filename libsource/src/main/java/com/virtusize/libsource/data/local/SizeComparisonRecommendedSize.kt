package com.virtusize.libsource.data.local

import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductSize

/**
 * This data class wraps the product comparison recommended size info
 * @param bestFitScore the best fit score for product comparison
 * @param bestStoreProductSize the best store product size for the user
 * @param bestUserProduct the best fit user product out of all the comparable user products
 * @param isStoreProductSmaller the boolean value for whether the best fit user product is smaller than the store product
 * @see ProductSize
 * @see Product
 */
internal data class SizeComparisonRecommendedSize(
    var bestFitScore: Float = 0f,
    var bestStoreProductSize: ProductSize? = null,
    var bestUserProduct: Product? = null,
    var isStoreProductSmaller: Boolean? = false
)