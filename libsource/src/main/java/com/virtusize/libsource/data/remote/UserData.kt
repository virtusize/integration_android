package com.virtusize.libsource.data.remote

/**
 * This class represents the response for the user data field in the data field of ProductCheckResponse
 * @see Data
 */
data class UserData(
	val shouldSeePhTooltip: Boolean,
	val wardrobeHasP: Boolean?,
	val wardrobeHasR: Boolean?,
	val wardrobeHasM: Boolean?,
	val wardrobeActive: Boolean?
)