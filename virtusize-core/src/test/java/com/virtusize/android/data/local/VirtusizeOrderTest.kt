package com.virtusize.android.data.local

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VirtusizeOrderTest {

    @Test
    fun paramsToMap_withNoOrderItem_shouldReturnExpectedMap() {
        val actualOrder = VirtusizeOrder("TEST_ORDER")
        actualOrder.setRegion("JP")

        val actualMap = actualOrder.paramsToMap("test_apiKey", "userId")

        val expectedMap = mapOf(
            "apiKey" to "test_apiKey",
            "externalOrderId" to "TEST_ORDER",
            "externalUserId" to "userId",
            "region" to "JP",
            "items" to mutableListOf<VirtusizeOrderItem>()
        )

        assertThat(actualMap).isEqualTo(expectedMap)
    }

    @Test
    fun paramsToMap_withTwoOrderItems_shouldReturnExpectedMap() {
        val actualOrder = VirtusizeOrder("TEST_ORDER")

        val item1 = VirtusizeOrderItem(
            "P001",
            "S",
            imageUrl = "http://images.example.com/products/P001/red/image1xl.jpg",
            unitPrice = 5000.005234,
            currency = "JPY",
            url = "http://example.com/products/P001"
        )

        val item2 = VirtusizeOrderItem(
            "P002",
            "M",
            imageUrl = "http://images.example.com/products/P002/red/image1xl.jpg",
            unitPrice = 5000.005234,
            currency = "JPY",
            url = "http://example.com/products/P002"
        )
        actualOrder.items.addAll(mutableListOf(item1, item2))

        val actualMap = actualOrder.paramsToMap("test_apiKey", "userId")

        val expectedMap = mapOf(
            "apiKey" to "test_apiKey",
            "externalOrderId" to "TEST_ORDER",
            "externalUserId" to "userId",
            "items" to mutableListOf(item1.paramsToMap(), item2.paramsToMap())
        )

        assertThat(actualMap).isEqualTo(expectedMap)
    }
}
