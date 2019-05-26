package com.virtusize.libsource.data.pojo

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

/**
 * This class represents response for user data field in the data field of ProductCheckResponse
 * @see Data
 */
@Generated("com.robohorse.robopojogenerator")
data class UserData(

	@field:SerializedName("should_see_ph_tooltip")
	val shouldSeePhTooltip: Boolean? = null,

	@field:SerializedName("wardrobeHasP")
	val wardrobeHasP: Boolean? = null,

	@field:SerializedName("wardrobeHasR")
	val wardrobeHasR: Boolean? = null,

	@field:SerializedName("wardrobeHasM")
	val wardrobeHasM: Boolean? = null,

	@field:SerializedName("wardrobeActive")
	val wardrobeActive: Boolean? = null
)