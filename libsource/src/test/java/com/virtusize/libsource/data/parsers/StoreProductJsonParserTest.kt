package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth
import com.virtusize.libsource.TestFixtures
import com.virtusize.libsource.data.remote.*
import org.json.JSONObject
import org.junit.Test

class StoreProductJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualStoreProduct = StoreProductJsonParser().parse(TestFixtures.STORE_PRODUCT_INFO)

        val expectedStoreProduct = StoreProduct(
            7110384,
            mutableListOf(
                ProductSize("38",
                    mutableListOf(
                        Measurement("height", 760),
                        Measurement("bust", 660),
                        Measurement("sleeve", 845)
                    )
                ),
                ProductSize("36",
                    mutableListOf(
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

        Truth.assertThat(actualStoreProduct).isEqualTo(expectedStoreProduct)
    }

    @Test
    fun parse_InvalidStoreProductData_shouldReturnNull() {
        val actualStoreProduct = StoreProductJsonParser().parse(JSONObject("{}"))

        Truth.assertThat(actualStoreProduct).isNull()
    }
}