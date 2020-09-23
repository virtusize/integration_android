package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.fixtures.ProductFixtures
import com.virtusize.libsource.fixtures.TestFixtures
import org.junit.Test

class StoreProductJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualStoreProduct = StoreProductJsonParser().parse(ProductFixtures.STORE_PRODUCT_INFO_JSON_DATA)

        val expectedStoreProduct = ProductFixtures.storeProduct()

        assertThat(actualStoreProduct).isEqualTo(expectedStoreProduct)
    }

    @Test
    fun parse_emptyStoreProductData_shouldReturnNull() {
        val actualStoreProduct = StoreProductJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualStoreProduct).isNull()
    }
}