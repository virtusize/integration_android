package com.virtusize.android.data.parsers

import com.google.common.truth.Truth
import com.virtusize.android.data.remote.BrandSizing
import com.virtusize.android.fixtures.TestFixtures
import org.json.JSONObject
import org.junit.Test

class BrandSizingJsonParserTest {
    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualBrandSizing = BrandSizingJsonParser().parse(BRAND_SIZING_JSON_DATA)

        val expectedBrandSizing =
            BrandSizing(
                "small",
                false,
            )

        Truth.assertThat(actualBrandSizing).isEqualTo(expectedBrandSizing)
    }

    @Test
    fun parse_emptyJsonData_shouldReturnExpectedObject() {
        val actualAdditionalInfo =
            StoreProductAdditionalInfoJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        Truth.assertThat(actualAdditionalInfo).isNull()
    }

    companion object {
        private val BRAND_SIZING_JSON_DATA =
            JSONObject(
                """
                {
                    "compare":"small",
                    "itemBrand":false
                }
                """.trimIndent(),
            )
    }
}
