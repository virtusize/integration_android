package com.virtusize.android.data.remote

/**
 * This class represents the product size info
 * @param name the size name
 * @param measurements the measurements of the size
 * @see Measurement
 */
data class ProductSize(
    val name: String,
    val measurements: Set<Measurement>,
)
