package com.virtusize.libsource.data.remote

/**
 * This class represents the response from the request to get the user session data
 * @param accessToken the access token
 * @param bid the browser ID
 * @param authToken the auth token
 */
internal data class UserSessionInfo(
    val accessToken: String,
    val bid: String?,
    val authToken: String
)