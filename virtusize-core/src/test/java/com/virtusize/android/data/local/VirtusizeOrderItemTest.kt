package com.virtusize.android.data.local

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VirtusizeOrderItemTest {
    @Test
    fun paramsToMap_shouldReturnExpectedMap() {
        val actualMap =
            VirtusizeOrderItem(
                "P001",
                "L",
                "Large",
                "P001_SIZEL_RED",
                "http://images.example.com/products/P001/red/image1xl.jpg",
                "Red",
                "W",
                5000.00,
                "JPY",
                1,
                "http://example.com/products/P001",
            ).paramsToMap()

        val expectedMap =
            mapOf(
                "externalProductId" to "P001",
                "size" to "L",
                "sizeAlias" to "Large",
                "variantId" to "P001_SIZEL_RED",
                "imageUrl" to "http://images.example.com/products/P001/red/image1xl.jpg",
                "color" to "Red",
                "gender" to "W",
                "unitPrice" to 5000.00,
                "currency" to "JPY",
                "quantity" to 1,
                "url" to "http://example.com/products/P001",
            )

        assertThat(actualMap).isEqualTo(expectedMap)
    }

    @Test
    fun paramsToMap_whenOptionalParamsAreNotProvided_shouldReturnExpectedMap() {
        val actualMap =
            VirtusizeOrderItem(
                "P001",
                "L",
                imageUrl = "http://images.example.com/products/P001/red/image1xl.jpg",
                unitPrice = 5000.00,
                currency = "JPY",
                url = "http://example.com/products/P001",
            ).paramsToMap()

        val expectedMap =
            mapOf(
                "externalProductId" to "P001",
                "size" to "L",
                "imageUrl" to "http://images.example.com/products/P001/red/image1xl.jpg",
                "unitPrice" to 5000.00,
                "currency" to "JPY",
                "quantity" to 1,
                "url" to "http://example.com/products/P001",
            )

        assertThat(actualMap).isEqualTo(expectedMap)
    }

    @Test
    fun paramsToMap_whenUnitPriceHasMoreThanTwoDecimals_shouldReturnExpectedRoundedNumber() {
        val actualMap =
            VirtusizeOrderItem(
                "P002",
                "M",
                imageUrl = "http://images.example.com/products/P002/red/image1xl.jpg",
                unitPrice = 5000.005234,
                currency = "JPY",
                url = "http://example.com/products/P002",
            ).paramsToMap()

        val expectedMap =
            mapOf(
                "externalProductId" to "P002",
                "size" to "M",
                "imageUrl" to "http://images.example.com/products/P002/red/image1xl.jpg",
                "unitPrice" to 5000.01,
                "currency" to "JPY",
                "quantity" to 1,
                "url" to "http://example.com/products/P002",
            )

        assertThat(actualMap).isEqualTo(expectedMap)
    }
}
