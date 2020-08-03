package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.TestFixtures
import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.ProductSize
import org.json.JSONObject
import org.junit.Test

class ProductSizeJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualProductSize = ProductSizeJsonParser().parse(PRODUCT_SIZE_JSON_DATA)

        val expectedProductSize = ProductSize(
            " ",
            mutableSetOf(
                Measurement("width", 150),
                Measurement("depth", 100),
                Measurement("height", 160)
            )
        )

        assertThat(actualProductSize).isEqualTo(expectedProductSize)
    }

    @Test
    fun parse_withNullMeasurementData_shouldReturnExpectedObject() {
        val actualProductSize = ProductSizeJsonParser().parse(PRODUCT_SIZE_WITH_NULL_INFO)

        val expectedProductSize = ProductSize(
            "",
            mutableSetOf(
                Measurement("height", 560),
                Measurement("bust", 450),
                Measurement("sleeve", 730),
                Measurement("shoulder", 0),
                Measurement("waist", 0),
                Measurement("hem", 0),
                Measurement("bicep", 0)
            )
        )

        assertThat(actualProductSize).isEqualTo(expectedProductSize)
    }

    @Test
    fun parse_emptyJsonData_shouldReturnExpectedObject() {
        val productSize = ProductSizeJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(productSize).isNull()
    }

    companion object {
        val PRODUCT_SIZE_JSON_DATA = JSONObject(
            """
                {
                    "name": "",
                    "measurements": {
                        "width": 150,
                        "depth": 100,
                        "height": 160
                    }
                }
            """.trimIndent()
        )

        val PRODUCT_SIZE_WITH_NULL_INFO = JSONObject(
            """
                {
                    "name": "",
                    "measurements": {
                        "height": 560,
                        "bust": 450,
                        "sleeve": 730,
                        "shoulder": null,
                        "waist": null,
                        "hem": null,
                        "bicep": null
                    }
                }
            """.trimIndent()
        )
    }
}