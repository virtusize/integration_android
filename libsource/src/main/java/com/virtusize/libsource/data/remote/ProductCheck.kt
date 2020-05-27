package com.virtusize.libsource.data.remote

/**
 * This class represents the response for the API request ProductCheck
 * @see VirtusizeEndpoint.ProductCheck
 */
data class ProductCheck(
	val data: Data?,
	val productId: String,
	val name: String
)