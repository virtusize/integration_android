package com.virtusize.libsource.data.pojo

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

/**
 * This class represents response for request ProductCheck
 * @see VirtusizeEndpoint.ProductCheck
 */
@Generated("com.robohorse.robopojogenerator")
data class ProductCheckResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("productId")
	val productId: String? = null,

	@field:SerializedName("name")
	val name: String? = null
)