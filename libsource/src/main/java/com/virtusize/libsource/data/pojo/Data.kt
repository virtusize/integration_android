package com.virtusize.libsource.data.pojo

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

/**
 * This class represents response for data field in ProductCheckResponse
 * @see ProductCheckResponse
 */
@Generated("com.robohorse.robopojogenerator")
data class Data(

	@field:SerializedName("validProduct")
	val validProduct: Boolean,

	@field:SerializedName("fetchMetaData")
	val fetchMetaData: Boolean,

	@field:SerializedName("userData")
	val userData: UserData,

	@field:SerializedName("productDataId")
	val productDataId: Int,

	@field:SerializedName("productTypeName")
	val productTypeName: String,

	@field:SerializedName("storeName")
	val storeName: String,

	@field:SerializedName("storeId")
	val storeId: Int,

	@field:SerializedName("productTypeId")
	val productTypeId: Int
)