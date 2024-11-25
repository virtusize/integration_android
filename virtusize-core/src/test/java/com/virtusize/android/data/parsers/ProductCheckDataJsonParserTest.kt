package com.virtusize.android.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.android.data.remote.Data
import com.virtusize.android.data.remote.ProductCheckData
import com.virtusize.android.fixtures.TestFixtures
import org.junit.Test

class ProductCheckDataJsonParserTest {
    @Test
    fun parse_shouldReturnExpectedObject() {
        val actualProductCheck = ProductCheckDataJsonParser().parse(TestFixtures.PRODUCT_CHECK_DATA)

        val expectedData =
            Data(
                true,
                false,
                true,
                7110384,
                "pants",
                "virtusize",
                2,
                5,
            )
        val expectedProductCheck =
            ProductCheckData(
                expectedData,
                "694",
                "backend-checked-product",
                TestFixtures.PRODUCT_CHECK_DATA.toString(),
            )

        assertThat(actualProductCheck).isEqualTo(expectedProductCheck)
    }
}
