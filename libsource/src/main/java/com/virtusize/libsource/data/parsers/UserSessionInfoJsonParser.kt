package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.UserSessionInfo
import org.json.JSONObject

internal class UserSessionInfoJsonParser : VirtusizeJsonParser<UserSessionInfo> {

    override fun parse(json: JSONObject): UserSessionInfo? {
        val id = json.optString(FIELD_ID)
        val authHeader = json.optString(FIELD_VS_AUTH)
        val bid = json.optJSONObject(FIELD_USER)?.optString(FIELD_BID)
        return UserSessionInfo(id, bid, authHeader)
    }

    private companion object {
        const val FIELD_ID = "id"
        const val FIELD_VS_AUTH = "x-vs-auth"
        const val FIELD_USER = "user"
        const val FIELD_BID = "bid"
    }
}