package com.virtusize.android.data.remote

/**
 * This class represents the response from the request to get the user session data
 * @param accessToken the access token
 * @param bid the browser ID
 * @param authToken the auth token
 * @param userSessionResponse the API response as a string
 * @param hasBodyMeasurement the flag to indicate if a user specified body measurements
 */
data class UserSessionInfo(
    val accessToken: String,
    internal val bid: String?,
    val authToken: String,
    val hasBodyMeasurement: Boolean,
    val userSessionResponse: String,
)
