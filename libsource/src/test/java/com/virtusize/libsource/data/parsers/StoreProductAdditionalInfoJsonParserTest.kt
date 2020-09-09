package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.TestFixtures
import com.virtusize.libsource.data.remote.BrandSizing
import com.virtusize.libsource.data.remote.StoreProductAdditionalInfo
import org.json.JSONObject
import org.junit.Test

class StoreProductAdditionalInfoJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualAdditionalInfo = StoreProductAdditionalInfoJsonParser().parse(ADDITIONAL_INFO_JSON_DATA)

        val expectedAdditionalInfo = StoreProductAdditionalInfo(
            "wide",
            "regular",
            BrandSizing(
                "true",
                true
            )
        )

        assertThat(actualAdditionalInfo).isEqualTo(expectedAdditionalInfo)
    }

    @Test
    fun parse_emptyJsonData_shouldReturnExpectedObject() {
        val actualAdditionalInfo = StoreProductAdditionalInfoJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualAdditionalInfo).isNull()
    }


    companion object {
        private val ADDITIONAL_INFO_JSON_DATA = JSONObject(
            """
                {
                    "brand":"Virtusize",
                    "gender":"null",
                    "sizes":{},
                    "modelInfo":{},
                    "type":"wide",
                    "style":"regular",
                    "fit":"wide",
                    "brandSizing":{
                        "compare":"true",
                        "itemBrand":true
                    }
                }
            """.trimIndent()
        )
    }
}