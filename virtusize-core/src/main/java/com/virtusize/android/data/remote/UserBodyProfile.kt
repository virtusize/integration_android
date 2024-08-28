package com.virtusize.android.data.remote

/**
 * This class represents the response from the request to get the user body profile data
 * @param gender the user's gender
 * @param age the user's age
 * @param height the user's height
 * @param weight the user's weight
 * @param bodyData the user's body measurement data, such as hip, bust, waist and so on.
 */
data class UserBodyProfile(
    val gender: String,
    val age: Int,
    val height: Int,
    val weight: String,
    val bodyData: Set<Measurement>,
)
