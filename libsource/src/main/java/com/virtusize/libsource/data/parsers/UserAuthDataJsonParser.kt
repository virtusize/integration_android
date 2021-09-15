package com.virtusize.libsource.data.parsers

import com.virtusize.libsource.data.remote.UserAuthData
import org.json.JSONObject

internal class UserAuthDataJsonParser : VirtusizeJsonParser<UserAuthData> {

    override fun parse(json: JSONObject): UserAuthData? {
        val bid = json.optString(FIELD_VS_BID)
        val authHeader = json.optString(FIELD_VS_AUTH)
        return UserAuthData(bid, authHeader)
    }

    private companion object {
        const val FIELD_VS_BID = "x-vs-bid"
        const val FIELD_VS_AUTH = "x-vs-auth"
    }
}
