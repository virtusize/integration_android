package com.virtusize.libsource.util

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.Product
import com.virtusize.libsource.data.remote.ProductSize
import com.virtusize.libsource.data.remote.Weight
import com.virtusize.libsource.fixtures.ProductFixtures
import org.junit.Test

class VirtusizeUtilsTest {

    @Test
    fun getStoreProductFitInfo_withUserShirtXS_shouldHaveExpectedFitInfo() {
        val actualStoreProductFitInfo = VirtusizeUtils.getProductComparisonFitInfo(
            userShirtXS.sizes[0],
            storeShirt.sizes[0],
            shirtProductWeights
        )

        assertThat(actualStoreProductFitInfo.fitScore).isEqualTo(88.75f)
        assertThat(actualStoreProductFitInfo.isSmaller).isFalse()
    }

    @Test
    fun getStoreProductFitInfo_withUserShirtS_shouldHaveExpectedFitInfo() {
        val actualStoreProductFitInfo = VirtusizeUtils.getProductComparisonFitInfo(
            userShirtS.sizes[0],
            storeShirt.sizes[0],
            shirtProductWeights
        )

        assertThat(actualStoreProductFitInfo.fitScore).isEqualTo(95.25f)
        assertThat(actualStoreProductFitInfo.isSmaller).isFalse()
    }

    @Test
    fun getStoreProductFitInfo_withUserShirtM_shouldHaveExpectedFitInfo() {
        val actualStoreProductFitInfo = VirtusizeUtils.getProductComparisonFitInfo(
            userShirtM.sizes[0],
            storeShirt.sizes[0],
            shirtProductWeights
        )

        assertThat(actualStoreProductFitInfo.fitScore).isEqualTo(96.25f)
        assertThat(actualStoreProductFitInfo.isSmaller).isTrue()
    }

    @Test
    fun getStoreProductFitInfo_withUserShirtL_shouldHaveExpectedFitInfo() {
        val actualStoreProductFitInfo = VirtusizeUtils.getProductComparisonFitInfo(
            userShirtL.sizes[0],
            storeShirt.sizes[0],
            shirtProductWeights
        )

        assertThat(actualStoreProductFitInfo.fitScore).isEqualTo(89.75f)
        assertThat(actualStoreProductFitInfo.isSmaller).isTrue()
    }

    @Test
    fun getFindBestMatch_shouldReturnExpectedRecommendedSize() {

        val userProductRecommendedSize = VirtusizeUtils.findBestFitProductSize(userShirts, storeShirt, ProductFixtures.productTypes())

        assertThat(userProductRecommendedSize?.bestFitScore).isEqualTo(96.25f)
        assertThat(userProductRecommendedSize?.bestUserProduct).isEqualTo(userShirts[2])
        assertThat(userProductRecommendedSize?.isStoreProductSmaller).isEqualTo(true)
    }

    private companion object {

        val userShirtXS = getProduct(
            1,
            mutableListOf(
                ProductSize(
                    "XS",
                    mutableSetOf(
                        Measurement("bust", 500),
                        Measurement("sleeve", 730),
                        Measurement("height", 665),
                        Measurement("shoulder", 400),
                        Measurement("hem", 610),
                    )
                )
            )
        )

        val userShirtS = getProduct(
            2,
            mutableListOf(
                ProductSize(
                    "S",
                    mutableSetOf(
                        Measurement("bust", 520),
                        Measurement("sleeve", 745),
                        Measurement("height", 685),
                        Measurement("shoulder", 410),
                        Measurement("hem", 630),
                    )
                )
            )
        )

        val userShirtM = getProduct(
            3,
            mutableListOf(
                ProductSize(
                    "M",
                    mutableSetOf(
                        Measurement("bust", 540),
                        Measurement("sleeve", 760),
                        Measurement("height", 705),
                        Measurement("shoulder", 420),
                        Measurement("hem", 650),
                    )
                )
            )
        )

        val userShirtL = getProduct(
            4,
            mutableListOf(
                ProductSize(
                    "L",
                    mutableSetOf(
                        Measurement("bust", 570),
                        Measurement("sleeve", 775),
                        Measurement("height", 725),
                        Measurement("shoulder", 430),
                        Measurement("hem", 680),
                    )
                )
            )
        )

        val userShirts = mutableListOf(userShirtXS, userShirtS, userShirtM, userShirtL)

        val storeShirt = getProduct(
            123,
            mutableListOf(
                ProductSize(
                    "Free",
                    mutableSetOf(
                        Measurement("bust", 530),
                        Measurement("sleeve", 770),
                        Measurement("height", 690),
                        Measurement("shoulder", 430),
                        Measurement("collar", 360),
                    )
                )
            )
        )

        val shirtProductWeights = mutableSetOf(
            Weight("bust", 2f),
            Weight("sleeve", 1f),
            Weight("height", 0.5f)
        )

        fun getProduct(productId: Int, sizes: List<ProductSize>): Product {
            return Product(
                productId,
                sizes,
                null,
                2,
                "",
                "",
                null,
                2,
                null
            )
        }
    }
}
