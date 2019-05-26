package com.virtusize.libsource.data.pojo

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

/**
 * This class represents response for request ProductMetaDataHints
 * @see VirtusizeEndpoint.ProductMetaDataHints
 */
@Generated("com.robohorse.robopojogenerator")
data class ProductMetaDataHintsResponse(

	@field:SerializedName("apiKey")
	val apiKey: String? = null,

	@field:SerializedName("imageUrl")
	val imageUrl: String? = null,

	@field:SerializedName("cloudinaryPublicId")
	val cloudinaryPublicId: String? = null,

	@field:SerializedName("externalProductId")
	val externalProductId: String? = null
)