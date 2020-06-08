package com.virtusize.libsource.data.remote.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.JsonResponseSamples
import com.virtusize.libsource.data.remote.Data
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.remote.UserData
import org.junit.Test

class ProductCheckJsonParserTest {

    @Test
    fun parse_shouldReturnExpectedObject() {
        val actualProductCheck = ProductCheckJsonParser().parse(JsonResponseSamples.PRODUCT_DATA_CHECK)

        val expectedData = Data(
            true,
            false,
            UserData(false, false, false, false, false),
            7110384,
            "pants",
            "virtusize",
            2,
            5
        )
        val expectedProductCheck = ProductCheck(
            expectedData,
            "694",
            "backend-checked-product"
        )

        assertThat(actualProductCheck).isEqualTo(expectedProductCheck)
    }
}