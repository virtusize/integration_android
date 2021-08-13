package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.fixtures.TestFixtures
import com.virtusize.libsource.data.remote.Data
import com.virtusize.libsource.data.remote.ProductCheck
import org.junit.Test

class ProductCheckJsonParserTest {

    @Test
    fun parse_shouldReturnExpectedObject() {
        val actualProductCheck = ProductCheckJsonParser().parse(TestFixtures.PRODUCT_DATA_CHECK)

        val expectedData = Data(
            true,
            false,
            true,
            7110384,
            "pants",
            "virtusize",
            2,
            5
        )
        val expectedProductCheck = ProductCheck(
            expectedData,
            "694",
            "backend-checked-product",
            TestFixtures.PRODUCT_DATA_CHECK.toString()
        )

        assertThat(actualProductCheck).isEqualTo(expectedProductCheck)
    }
}