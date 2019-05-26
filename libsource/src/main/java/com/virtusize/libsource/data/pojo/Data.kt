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
	val validProduct: Boolean? = null,

	@field:SerializedName("fetchMetaData")
	val fetchMetaData: Boolean? = null,

	@field:SerializedName("userData")
	val userData: UserData? = null,

	@field:SerializedName("productDataId")
	val productDataId: Int? = null,

	@field:SerializedName("productTypeName")
	val productTypeName: String? = null,

	@field:SerializedName("storeName")
	val storeName: String? = null,

	@field:SerializedName("storeId")
	val storeId: Int? = null,

	@field:SerializedName("productTypeId")
	val productTypeId: Int? = null
)