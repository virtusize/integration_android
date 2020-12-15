package com.virtusize.libsource.data.local

import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductSize

// TODO: comment
internal data class SizeComparisonRecommendedSize(
    var bestFitScore: Float = 0f,
    var bestSize: ProductSize? = null,
    var bestUserProduct: Product? = null,
    var isStoreProductSmaller: Boolean? = false
)