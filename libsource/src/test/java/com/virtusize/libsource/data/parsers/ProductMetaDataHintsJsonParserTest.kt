package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.TestFixtures
import com.virtusize.libsource.data.remote.ProductMetaDataHints
import org.junit.Test

class ProductMetaDataHintsJsonParserTest {

    @Test
    fun parse_shouldReturnExpectedObject() {
        val actualProductMetaDataHints = ProductMetaDataHintsJsonParser().parse(TestFixtures.PRODUCT_META_DATA_HINTS)

        val expectedProductMetaDataHints = ProductMetaDataHints(
            "test_apiKey",
            "http://www.test.com/goods/31/12/11/71/1234_COL_COL02_570.jpg",
            "test_cloudinaryPublicId",
            "694"
        )

        assertThat(actualProductMetaDataHints).isEqualTo(expectedProductMetaDataHints)
    }
}