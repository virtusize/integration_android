package com.virtusize.android.data.remote

/**
 * This class represents the data for the user authentication event, that is from the web view
 * @param bid the browser ID
 * @param auth the auth token
 */
internal data class UserAuthData(
    val bid: String,
    val auth: String,
)
