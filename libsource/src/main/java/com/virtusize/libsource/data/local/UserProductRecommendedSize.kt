package com.virtusize.libsource.data.local

import com.virtusize.libsource.data.remote.Product

// TODO: comment
internal data class UserProductRecommendedSize(
    var bestFitScore: Float = 0f,
    var bestUserProduct: Product? = null,
    var isStoreProductSmaller: Boolean? = false
)