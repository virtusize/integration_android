package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.remote.BrandSizing
import com.virtusize.libsource.data.remote.Measurement
import com.virtusize.libsource.data.remote.ProductSize
import com.virtusize.libsource.fixtures.ProductFixtures
import com.virtusize.libsource.fixtures.TestFixtures
import org.junit.Test

class StoreProductJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedStoreProduct() {
        val actualStoreProduct =
            StoreProductJsonParser().parse(ProductFixtures.STORE_PRODUCT_INFO_JSON_DATA)
        assertThat(actualStoreProduct?.id).isEqualTo(7110384)
        assertThat(actualStoreProduct?.sizes?.size).isEqualTo(2)
        assertThat(actualStoreProduct?.sizes?.toMutableList()).isEqualTo(
            mutableListOf(
                ProductSize(
                    "38",
                    mutableSetOf(
                        Measurement("height", 760),
                        Measurement("bust", 660),
                        Measurement("sleeve", 845)
                    )
                ),
                ProductSize(
                    "36",
                    mutableSetOf(
                        Measurement("height", 750),
                        Measurement("bust", 645),
                        Measurement("sleeve", 825)
                    )
                )
            )
        )
        assertThat(actualStoreProduct?.externalId).isEqualTo("694")
        assertThat(actualStoreProduct?.productType).isEqualTo(8)
        assertThat(actualStoreProduct?.name).isEqualTo("Test Product Name")
        assertThat(actualStoreProduct?.cloudinaryPublicId).isEqualTo("Test Cloudinary Public Id")
        assertThat(actualStoreProduct?.isFavorite).isNull()
        assertThat(actualStoreProduct?.storeId).isEqualTo(2)
        assertThat(actualStoreProduct?.storeProductMeta?.id).isEqualTo(1)
        assertThat(actualStoreProduct?.storeProductMeta?.brand).isEqualTo("Virtusize")
        assertThat(
            actualStoreProduct?.storeProductMeta?.additionalInfo?.brand
        ).isEqualTo(
            "Virtusize"
        )
        assertThat(
            actualStoreProduct?.storeProductMeta?.additionalInfo?.sizes?.toMutableSet()
        ).isEqualTo(
            mutableSetOf(
                ProductSize(
                    "38",
                    mutableSetOf(
                        Measurement("height", 760),
                        Measurement("bust", 660),
                        Measurement("sleeve", 845)
                    )
                ),
                ProductSize(
                    "36",
                    mutableSetOf(
                        Measurement("height", 750),
                        Measurement("bust", 645),
                        Measurement("sleeve", 825)
                    )
                )
            )
        )
        assertThat(actualStoreProduct?.storeProductMeta?.additionalInfo?.modelInfo).isEqualTo(
            mutableMapOf(
                "hip" to 85,
                "size" to "38",
                "waist" to 56,
                "bust" to 78,
                "height" to 165
            )
        )
        assertThat(
            actualStoreProduct?.storeProductMeta?.additionalInfo?.fit
        ).isEqualTo("regular")
        assertThat(
            actualStoreProduct?.storeProductMeta?.additionalInfo?.style
        ).isEqualTo("fashionable")
        assertThat(actualStoreProduct?.storeProductMeta?.additionalInfo?.brandSizing).isEqualTo(
            BrandSizing("large", false)
        )
    }

    @Test
    fun parse_emptyStoreProductData_shouldReturnNull() {
        val actualStoreProduct = StoreProductJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualStoreProduct).isNull()
    }
}
