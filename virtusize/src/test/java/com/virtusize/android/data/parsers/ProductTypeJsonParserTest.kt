package com.virtusize.android.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.Weight
import com.virtusize.android.fixtures.ProductFixtures
import com.virtusize.android.fixtures.TestFixtures
import org.junit.Test

class ProductTypeJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualProductType =
            ProductTypeJsonParser().parse(ProductFixtures.PRODUCT_TYPE_JSON_OBJECT)

        val expectedProductType = ProductType(
            1,
            "dress",
            mutableSetOf(
                Weight("bust", 1f),
                Weight("waist", 1f),
                Weight("height", 0.25f)
            ),
            mutableListOf(1, 16)
        )

        assertThat(actualProductType).isEqualTo(expectedProductType)
    }

    @Test
    fun parse_emptyJsonData_shouldReturnExpectedObject() {
        val productType = ProductTypeJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(productType).isNull()
    }
}
