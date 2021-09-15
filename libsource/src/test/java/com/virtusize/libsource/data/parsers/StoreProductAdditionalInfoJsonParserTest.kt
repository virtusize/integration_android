package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.remote.BrandSizing
import com.virtusize.libsource.fixtures.TestFixtures
import org.json.JSONObject
import org.junit.Test

class StoreProductAdditionalInfoJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedStoreProductAdditionalInfo() {
        val actualAdditionalInfo = StoreProductAdditionalInfoJsonParser().parse(
            ADDITIONAL_INFO_JSON_DATA
        )

        assertThat(actualAdditionalInfo?.brand).isEqualTo("Virtusize")
        assertThat(actualAdditionalInfo?.sizes?.size).isEqualTo(0)
        assertThat(actualAdditionalInfo?.modelInfo).isEqualTo(mutableMapOf<String, Any>())
        assertThat(actualAdditionalInfo?.fit).isEqualTo("wide")
        assertThat(actualAdditionalInfo?.style).isEqualTo("regular")
        assertThat(actualAdditionalInfo?.brandSizing).isEqualTo(
            BrandSizing(
                "true",
                true
            )
        )
    }

    @Test
    fun parse_emptyJsonData_shouldReturnNull() {
        val actualAdditionalInfo =
            StoreProductAdditionalInfoJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)
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
