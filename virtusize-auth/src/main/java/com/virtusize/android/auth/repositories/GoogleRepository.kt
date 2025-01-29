package com.virtusize.android.auth.repositories

import com.virtusize.android.auth.data.GoogleUser
import com.virtusize.android.auth.network.GoogleAPIService

internal class GoogleRepository(
    private val googleAPIService: GoogleAPIService,
) {
    /**
     * Get the user's Google profile
     *
     * @param accessToken The user's access token
     */
    suspend fun getUser(accessToken: String): GoogleUser? {
        val response = googleAPIService.getUserInfo(accessToken)
        return response.successData
    }
}
