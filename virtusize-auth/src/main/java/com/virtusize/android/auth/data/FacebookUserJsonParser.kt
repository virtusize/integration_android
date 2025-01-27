package com.virtusize.android.auth.data

import com.virtusize.android.data.parsers.VirtusizeJsonParser
import org.json.JSONObject

class FacebookUserJsonParser: VirtusizeJsonParser<FacebookUser> {
    override fun parse(json: JSONObject): FacebookUser {
        return FacebookUser(
            json.optString(FIELD_ID),
            json.optString(FIELD_FIRST_NAME),
            json.optString(FIELD_LAST_NAME),
            json.optString(FIELD_NAME),
            json.optString(FIELD_EMAIL),
        )
    }

    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_FIRST_NAME = "first_name"
        private const val FIELD_LAST_NAME = "last_name"
        private const val FIELD_NAME = "name"
        private const val FIELD_EMAIL = "email"
    }
}