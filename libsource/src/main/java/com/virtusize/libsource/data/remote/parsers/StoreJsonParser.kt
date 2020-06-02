package com.virtusize.libsource.data.remote.parsers

import android.util.Log
import com.virtusize.libsource.Constants
import com.virtusize.libsource.data.remote.Store
import org.json.JSONException
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [Store] object
 */
internal class StoreJsonParser: VirtusizeJsonParser {
    override fun parse(json: JSONObject): Store? {
        try {
            val id = json.getInt(FIELD_ID)
            val surveyLink = json.getString(FIELD_SURVEY_LINK)
            val name = json.getString(FIELD_NAME)
            val shortName = json.getString(FIELD_SHORT_NAME)
            val lengthUnitId = json.getInt(FIELD_LENGTH_UNIT_ID)
            val apiKey = json.getString(FIELD_API_KEY)
            val created = json.getString(FIELD_CREATED)
            val updated = json.getString(FIELD_UPDATED)
            val disabled = json.optBoolean(FIELD_DISABLED)
            val typeMapperEnabled = json.getBoolean(FIELD_TYPE_MAPPER_ENABLED)
            val region = json.optString(FIELD_REGION)
            return Store(id, surveyLink, name, shortName, lengthUnitId, apiKey, created, updated, disabled, typeMapperEnabled, region)
        } catch(e: JSONException) {
            Log.e(Constants.LOG_TAG, e.localizedMessage)
        }
        return null
    }

    private companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_SURVEY_LINK = "surveyLink"
        private const val FIELD_NAME = "name"
        private const val FIELD_SHORT_NAME = "shortName"
        private const val FIELD_LENGTH_UNIT_ID = "lengthUnitId"
        private const val FIELD_API_KEY = "apiKey"
        private const val FIELD_CREATED = "created"
        private const val FIELD_UPDATED = "updated"
        private const val FIELD_DISABLED = "disabled"
        private const val FIELD_TYPE_MAPPER_ENABLED = "typemapperEnabled"
        private const val FIELD_REGION = "region"
    }
}