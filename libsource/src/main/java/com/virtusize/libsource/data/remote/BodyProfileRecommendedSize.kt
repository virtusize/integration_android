package com.virtusize.libsource.data.remote

/**
 * This class represents the response for the recommendation API based on the user body profile
 * @param sizeName the recommended size name
 */
data class BodyProfileRecommendedSize(
    val sizeName: String
)