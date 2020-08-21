package com.virtusize.libsource.data.remote

/**
 * This class represents the weight corresponding to the product type
 * @param factor the factor of the weight, e.g. "bust", "waist", "width", etc.
 * @param value the value of the weight
 */
data class Weight(
    val factor: String,
    val value: Float
)