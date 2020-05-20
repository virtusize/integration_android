package com.virtusize.libsource.data.pojo

import javax.annotation.Generated
import com.google.gson.annotations.SerializedName

/**
 * This class represents the response for the user data field in the data field of ProductCheckResponse
 * @see Data
 */
@Generated("com.robohorse.robopojogenerator")
data class UserData(

	@field:SerializedName("should_see_ph_tooltip")
	val shouldSeePhTooltip: Boolean,

	@field:SerializedName("wardrobeHasP")
	val wardrobeHasP: Boolean,

	@field:SerializedName("wardrobeHasR")
	val wardrobeHasR: Boolean,

	@field:SerializedName("wardrobeHasM")
	val wardrobeHasM: Boolean,

	@field:SerializedName("wardrobeActive")
	val wardrobeActive: Boolean
)