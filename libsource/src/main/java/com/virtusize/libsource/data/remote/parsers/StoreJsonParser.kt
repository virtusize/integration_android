package com.virtusize.libsource.data.remote.parsers

import com.virtusize.libsource.data.remote.JsonUtils
import com.virtusize.libsource.data.remote.Store
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [Store] object
 */
internal class StoreJsonParser: VirtusizeJsonParser {
    override fun parse(json: JSONObject): Store? {
        val id = json.optInt(FIELD_ID)
        val surveyLink = JsonUtils.optString(json, FIELD_SURVEY_LINK)
        val name = JsonUtils.optString(json, FIELD_NAME)
        val shortName = JsonUtils.optString(json, FIELD_SHORT_NAME)
        val lengthUnitId = json.optInt(FIELD_LENGTH_UNIT_ID)
        val apiKey = JsonUtils.optString(json, FIELD_API_KEY)
        val created = JsonUtils.optString(json, FIELD_CREATED)
        val updated = JsonUtils.optString(json, FIELD_UPDATED)
        val disabled = JsonUtils.optString(json, FIELD_DISABLED)
        val typeMapperEnabled = json.optBoolean(FIELD_TYPE_MAPPER_ENABLED)
        val region = JsonUtils.optString(json, FIELD_REGION)
        return Store(id, surveyLink, name, shortName, lengthUnitId, apiKey, created, updated, disabled, typeMapperEnabled, region)
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