package com.virtusize.android.data.remote

/**
 * This class represents the response for the recommendation API based on the user body profile
 * @param product the store product that is associated with this recommendation
 * @param sizeName the recommended size name
 */
data class BodyProfileRecommendedSize(
    val product: Product,
    internal val sizeName: String,
)
