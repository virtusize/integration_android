package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.TestFixtures
import com.virtusize.libsource.data.remote.*
import org.json.JSONObject
import org.junit.Test

class StoreProductJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualStoreProduct = StoreProductJsonParser().parse(TestFixtures.STORE_PRODUCT_INFO_JSON_DATA)

        val expectedStoreProduct = StoreProduct(
            7110384,
            mutableListOf(
                ProductSize("38",
                    mutableSetOf(
                        Measurement("height", 760),
                        Measurement("bust", 660),
                        Measurement("sleeve", 845)
                    )
                ),
                ProductSize("36",
                    mutableSetOf(
                        Measurement("height", 750),
                        Measurement("bust", 645),
                        Measurement("sleeve", 825)
                    )
                )
            ),
            "694",
            8,
            "Test Product Name",
            2,
            StoreProductMeta(
                1,
                StoreProductAdditionalInfo(
                "regular",
                    BrandSizing(
                        "large",
                        false
                    )
                )
            )
        )

        assertThat(actualStoreProduct).isEqualTo(expectedStoreProduct)
    }

    @Test
    fun parse_emptyStoreProductData_shouldReturnNull() {
        val actualStoreProduct = StoreProductJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualStoreProduct).isNull()
    }
}