package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

class JsonUtilsTest {

    @Test
    fun optString_whenValuePresent_findsAndReturnsValue() {
        val jsonObject = JSONObject()
            .put("key", "value")
        Truth.assertThat(JsonUtils.optString(jsonObject, "key")).isEqualTo("value")
    }

    @Test
    fun optString_whenValueContainsNull_returnsEmptyString() {
        val jsonObject = JSONObject()
            .put("key", "null")
        Truth.assertThat(JsonUtils.optString(jsonObject, "key")).isEmpty()
    }

    @Test
    fun optString_whenNameNotPresent_returnsEmptyString() {
        val jsonObject = JSONObject()
            .put("key", "value")
        Truth.assertThat(JsonUtils.optString(jsonObject, "notpresent")).isEmpty()
    }

    @Test
    fun jsonObjectToMap_forEmptyJSONObject_returnsEmptyMap() {
        Truth.assertThat(JsonUtils.jsonObjectToMap(JSONObject())).isEmpty()
    }

    @Test
    fun jsonObjectToMap_forSimpleJSONObject_returnsExpectedMap() {
        val expectedMap = mapOf(
            "key" to "value",
            "boolkey" to true,
            "numkey" to 123,
            "nullkey" to JSONObject.NULL,
            "floatkey" to 1234.54
        )

        val mappedObject = JsonUtils.jsonObjectToMap(SIMPLE_JSON)
        Truth.assertThat(expectedMap).isEqualTo(mappedObject)
    }

    @Test
    fun jsonObjectToMap_forNestedJSONObject_returnsExpectedMap() {
        val expectedMap = mapOf(
            "key_one" to mapOf(
                "key_one_one" to mapOf(
                    "key_one_one_number" to 123,
                    "key_one_one_string" to "hello"
                ),
                "key_two_one" to false),
            "key_two" to mapOf(
                "key_two_one" to "world",
                "key_two_two" to emptyMap<String, Any>()
            ),
            "key_three" to JSONObject.NULL
        )

        val mappedObject = JsonUtils.jsonObjectToMap(NESTED_JSON)
        Truth.assertThat(expectedMap).isEqualTo(mappedObject)
    }

    @Test
    fun jsonObjectToMap_forNestedJSONObjectWithMixedArrays_returnsExpectedMap() {
        val items = listOf(
            mapOf("id" to 2020324),
            mapOf("id" to "stringOrderId"),
            "a string item",
            323,
            listOf(1, 22.2, "a", true),
            listOf(mapOf("something" to "deep"))
        )
        val expectedMap = mapOf(
            "expired" to false,
            "order" to mapOf(
                "items" to items,
                "timestamp" to "time"
            )
        )

        val convertedMap = JsonUtils.jsonObjectToMap(NESTED_JSON_MIXED_ARRAY)
        Truth.assertThat(expectedMap).isEqualTo(convertedMap)
    }

    @Test
    fun jsonArrayToList_forEmptyJSONArray_returnsEmptyList() {
        Truth.assertThat(JsonUtils.jsonArrayToList(JSONArray())).isEmpty()
    }

    @Test
    fun jsonArrayToList_forSimpleJSONArray_returnsExpectedList() {
        val expectedList = listOf(1, 22.2, 3, "a", true, "cde")
        val convertedJsonArray = JsonUtils.jsonArrayToList(SIMPLE_JSON_ARRAY)
        Truth.assertThat(expectedList).isEqualTo(convertedJsonArray)
    }

    private companion object {

        private val SIMPLE_JSON = JSONObject(
            """
            {
                "key": "value",
                "boolkey": true,
                "numkey": 123,
                "nullkey": null,
                "floatkey": 1234.54
            }
            """.trimIndent()
        )

        private val NESTED_JSON = JSONObject(
            """
            {
                "key_one": {
                    "key_one_one": {
                        "key_one_one_number": 123,
                        "key_one_one_string": "hello"
                    },
                    "key_two_one": false
                },
                "key_two": {
                    "key_two_one": "world",
                    "key_two_two": {}
                },
                "key_three": null
            }
            """.trimIndent()
        )

        private val SIMPLE_JSON_ARRAY = JSONArray(
            """
            [1, 22.2, 3, "a", true, "cde"]
            """.trimIndent()
        )

        private val NESTED_JSON_MIXED_ARRAY = JSONObject(
            """
            {
                "order": {
                    "items": [{
                            "id": 2020324
                        },
                        {
                            "id": "stringOrderId"
                        },
                        "a string item",
                        323,
                        [1, 22.2, "a", true],
                        [{
                            "something": "deep"
                        }]
                    ],
                    "timestamp": "time"
                },
                "expired": false
            }
            """.trimIndent()
        )
    }
}