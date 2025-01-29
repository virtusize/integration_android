package com.virtusize.android.auth.network

import android.content.Context
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.auth.data.FacebookUser
import com.virtusize.android.auth.data.FacebookUserJsonParser
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_ACCESS_TOKEN_KEY
import com.virtusize.android.network.ApiRequest
import com.virtusize.android.network.HttpMethod
import com.virtusize.android.network.VirtusizeApiResponse
import com.virtusize.android.network.VirtusizeApiTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FacebookAPIService {
    companion object {
        private const val BASE_URL = "https://graph.facebook.com/v2.9"

        private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

        @Volatile
        private var instance: FacebookAPIService? = null

        fun getInstance(context: Context): FacebookAPIService {
            return instance ?: synchronized(this) {
                instance ?: FacebookAPIService().also {
                    sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)
                    instance = it
                }
            }
        }
    }

    /**
     * Get the user's Facebook profile information
     *
     * @param accessToken The access token to use for the request
     * @return [VirtusizeApiResponse] with the Facebook user data
     */
    suspend fun getUserInfo(accessToken: String): VirtusizeApiResponse<FacebookUser> =
        withContext(Dispatchers.IO) {
            val apiRequest =
                ApiRequest(
                    "$BASE_URL/me",
                    HttpMethod.GET,
                    mutableMapOf(
                        "fields" to "email,first_name,last_name,name,timezone,verified",
                        SNS_ACCESS_TOKEN_KEY to accessToken,
                    ),
                )
            VirtusizeApiTask(
                null,
                sharedPreferencesHelper,
                null,
            )
                .setJsonParser(FacebookUserJsonParser())
                .execute(apiRequest)
        }
}
