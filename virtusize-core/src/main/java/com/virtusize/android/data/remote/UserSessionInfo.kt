package com.virtusize.android.data.remote

/**
 * This class represents the response from the request to get the user session data
 * @param accessToken the access token
 * @param bid the browser ID
 * @param authToken the auth token
 * @param userSessionResponse the API response as a string
 */
data class UserSessionInfo(
    val accessToken: String,
    internal val bid: String?,
    val authToken: String,
    val userSessionResponse: String,
)
