package com.virtusize.android.auth.network

import android.content.Context
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.auth.data.GoogleUser
import com.virtusize.android.auth.data.GoogleUserJsonParser
import com.virtusize.android.auth.utils.VirtusizeAuthConstants
import com.virtusize.android.network.ApiRequest
import com.virtusize.android.network.HttpMethod
import com.virtusize.android.network.VirtusizeApiResponse
import com.virtusize.android.network.VirtusizeApiTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAPIService {
    companion object {
        private const val BASE_URL = "https://www.googleapis.com/oauth2/v3"

        private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

        @Volatile
        private var instance: GoogleAPIService? = null

        fun getInstance(context: Context): GoogleAPIService {
            return instance ?: synchronized(this) {
                instance ?: GoogleAPIService().also {
                    sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)
                    instance = it
                }
            }
        }
    }

    /**
     * Get the user's Google profile information
     *
     * @param accessToken The access token to use for the request
     * @return VirtusizeApiResponse with the user's Google profile information
     */
    suspend fun getUserInfo(accessToken: String): VirtusizeApiResponse<GoogleUser> =
        withContext(Dispatchers.IO) {
            val apiRequest =
                ApiRequest(
                    "$BASE_URL/userinfo",
                    HttpMethod.GET,
                    mutableMapOf(
                        "alt" to "json",
                        VirtusizeAuthConstants.SNS_ACCESS_TOKEN_KEY to accessToken,
                    ),
                )
            VirtusizeApiTask(
                null,
                sharedPreferencesHelper,
                null,
            )
                .setJsonParser(GoogleUserJsonParser())
                .execute(apiRequest)
        }
}
