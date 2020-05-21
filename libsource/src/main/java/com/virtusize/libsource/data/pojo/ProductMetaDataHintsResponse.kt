package com.virtusize.libsource.data.pojo

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

/**
 * This class represents the response for the request ProductMetaDataHints
 * @see VirtusizeEndpoint.ProductMetaDataHints
 */
@Generated("com.robohorse.robopojogenerator")
data class ProductMetaDataHintsResponse(

	@field:SerializedName("apiKey")
	val apiKey: String,

	@field:SerializedName("imageUrl")
	val imageUrl: String,

	@field:SerializedName("cloudinaryPublicId")
	val cloudinaryPublicId: String,

	@field:SerializedName("externalProductId")
	val externalProductId: String
)