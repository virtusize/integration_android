package com.virtusize.android.auth.data

import com.virtusize.android.data.parsers.VirtusizeJsonParser
import org.json.JSONObject

/**
 * Parses the JSON response from Google API's endpoint to retrieve the user's data.
 */
class GoogleUserJsonParser: VirtusizeJsonParser<GoogleUser> {
    override fun parse(json: JSONObject): GoogleUser {
        return GoogleUser(
            json.optString(FIELD_SUB),
            json.optString(FIELD_GIVEN_NAME),
            json.optString(FIELD_FAMILY_NAME),
            json.optString(FIELD_NAME),
            json.optString(FIELD_EMAIL),
            json.optString(FIELD_LOCALE),
            json.optString(FIELD_PICTURE)
        )
    }

    companion object {
        private const val FIELD_SUB = "sub"
        private const val FIELD_GIVEN_NAME = "given_name"
        private const val FIELD_FAMILY_NAME = "family_name"
        private const val FIELD_NAME = "name"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_LOCALE = "locale"
        private const val FIELD_PICTURE = "picture"
    }
}