package com.virtusize.android.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.android.data.remote.ProductMetaDataHints
import com.virtusize.android.fixtures.TestFixtures
import org.junit.Test

class ProductMetaDataHintsJsonParserTest {

    @Test
    fun parse_shouldReturnExpectedObject() {
        val actualProductMetaDataHints =
            ProductMetaDataHintsJsonParser().parse(TestFixtures.PRODUCT_META_DATA_HINTS)

        val expectedProductMetaDataHints = ProductMetaDataHints(
            "test_apiKey",
            "http://www.test.com/goods/31/12/11/71/1234_COL_COL02_570.jpg",
            "test_cloudinaryPublicId",
            "694"
        )

        assertThat(actualProductMetaDataHints).isEqualTo(expectedProductMetaDataHints)
    }
}
