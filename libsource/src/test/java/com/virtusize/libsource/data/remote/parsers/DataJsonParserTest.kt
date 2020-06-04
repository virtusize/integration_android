package com.virtusize.libsource.data.remote.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.JsonResources.PRODUCT_DATA_CHECK_DATA_JSON
import com.virtusize.libsource.data.remote.Data
import com.virtusize.libsource.data.remote.UserData
import org.junit.Test

class DataJsonParserTest {

    @Test
    fun testParseProductDataCheckDataJson() {
        val actualData = DataJsonParser().parse(PRODUCT_DATA_CHECK_DATA_JSON)
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

        assertThat(actualData).isEqualTo(expectedData)
    }
}