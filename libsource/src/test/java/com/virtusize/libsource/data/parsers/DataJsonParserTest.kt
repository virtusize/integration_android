package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.TestFixtures
import com.virtusize.libsource.data.remote.Data
import org.junit.Test

class DataJsonParserTest {

    @Test
    fun parse_shouldReturnExpectedObject() {
        val actualData = DataJsonParser().parse(TestFixtures.PRODUCT_DATA_CHECK_DATA)
        val expectedData = Data(
            true,
            false,
            false,
            7110384,
            "pants",
            "virtusize",
            2,
            5
        )

        assertThat(actualData).isEqualTo(expectedData)
    }
}