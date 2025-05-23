package com.virtusize.android.data.parsers

import com.virtusize.android.data.remote.UserSessionInfo
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [UserSessionInfo] object
 */
class UserSessionInfoJsonParser : VirtusizeJsonParser<UserSessionInfo> {
    override fun parse(json: JSONObject): UserSessionInfo {
        val accessToken = json.optString(FIELD_ID)
        val authToken = json.optString(FIELD_VS_AUTH)
        val bid = json.optJSONObject(FIELD_USER)?.optString(FIELD_BID)
        val hasBodyMeasurement = json.optJSONObject(FIELD_STATUS)?.optBoolean(FIELD_BODY_MEASUREMENT) ?: false
        return UserSessionInfo(accessToken, bid, authToken, hasBodyMeasurement, json.toString())
    }

    private companion object {
        const val FIELD_ID = "id"
        const val FIELD_VS_AUTH = "x-vs-auth"
        const val FIELD_USER = "user"
        const val FIELD_BID = "bid"
        const val FIELD_STATUS = "status"
        const val FIELD_BODY_MEASUREMENT = "hasBodyMeasurement"
    }
}
