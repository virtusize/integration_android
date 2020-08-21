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

        val expectedStoreProduct = TestFixtures.storeProduct()

        assertThat(actualStoreProduct).isEqualTo(expectedStoreProduct)
    }

    @Test
    fun parse_emptyStoreProductData_shouldReturnNull() {
        val actualStoreProduct = StoreProductJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualStoreProduct).isNull()
    }
}