package com.virtusize.libsource.data.remote

/**
 * This class represents the product type info
 * @param id the ID of a product type
 * @param weights the weights of this product type for the calculation of the fitting score
 * @see Weight
 */
data class ProductType(
    val id: Int,
    val name: String,
    val weights: Set<Weight>
)