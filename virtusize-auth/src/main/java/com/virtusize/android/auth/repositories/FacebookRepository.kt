package com.virtusize.android.auth.repositories

import com.virtusize.android.auth.data.FacebookUser
import com.virtusize.android.auth.network.FacebookAPIService

internal class FacebookRepository(
    private val facebookAPIService: FacebookAPIService,
) {
    /**
     * Get the user's Facebook profile
     *
     * @param accessToken The user's Facebook access token
     */
    suspend fun getUser(accessToken: String): FacebookUser? {
        val response = facebookAPIService.getUserInfo(accessToken)
        return response.successData
    }
}
