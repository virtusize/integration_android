package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.fixtures.TestFixtures
import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductSize
import com.virtusize.libsource.fixtures.ProductFixtures
import org.junit.Test

class UserProductsJsonParserTest {

    @Test
    fun parse_validUserProduct_returnExpectedUserProduct() {
        val actualUserProduct = UserProductJsonParser().parse(ProductFixtures.USER_PRODUCT_ONE_JSON_OBJECT)

        val expectedUserProduct = Product(
            123456,
            mutableListOf(
                ProductSize(
                    "S",
                    mutableSetOf(
                        Measurement("height", 1000),
                        Measurement("bust", 400),
                        Measurement("waist", 340)
                    )
                )
            ),
            null,
            11,
            "Test Womenswear Strapless Dress",
            "",
            false,
            0,
            null
        )

        assertThat(actualUserProduct).isEqualTo(expectedUserProduct)
    }

    @Test
    fun parse_emptyJSONObject_returnNull() {
        val actualUserProduct = UserProductJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualUserProduct).isNull()
    }
}