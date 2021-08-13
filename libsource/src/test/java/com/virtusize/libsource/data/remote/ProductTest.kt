package com.virtusize.libsource.data.remote

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.fixtures.ProductFixtures
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class ProductTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var i18nLocalization: I18nLocalization
    private var sizeComparisonRecommendedSize: SizeComparisonRecommendedSize? = null
    private var bodyProfileRecommendedSizeName: String? = null

    @Before
    fun setup() {
        i18nLocalization = I18nLocalization(
            context.getString(R.string.inpage_default_accessory_text),
            context.getString(R.string.inpage_has_product_top_text),
            context.getString(R.string.inpage_has_product_bottom_text),
            context.getString(R.string.inpage_one_size_close_top_text),
            context.getString(R.string.inpage_one_size_smaller_top_text),
            context.getString(R.string.inpage_one_size_larger_top_text),
            context.getString(R.string.inpage_one_size_close_bottom_text),
            context.getString(R.string.inpage_one_size_smaller_bottom_text),
            context.getString(R.string.inpage_one_size_larger_bottom_text),
            context.getString(R.string.inpage_one_size_body_profile_text),
            context.getString(R.string.inpage_multi_size_comparison_text),
            context.getString(R.string.inpage_multi_size_body_profile_text),
            context.getString(R.string.inpage_no_data_text)
        )
    }

    @Test
    fun getRecommendationText_productIsAnAccessory_returnDefaultAccessoryText() {
        val defaultAccessoryText = context.getString(R.string.inpage_default_accessory_text)
        assertThat(ProductFixtures.storeProduct(18).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(defaultAccessoryText)
        assertThat(ProductFixtures.storeProduct(19).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(defaultAccessoryText)
        assertThat(ProductFixtures.storeProduct(25).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(defaultAccessoryText)
        assertThat(ProductFixtures.storeProduct(26).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(defaultAccessoryText)
    }

    @Test
    fun getRecommendationText_productIsAnAccessory_hasSizeComparisonRecommendedSize_returnHasProductAccessoryText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.bestStoreProductSize = ProductFixtures.storeProduct(18).sizes.get(0)
        val hasProductAccessoryTopText = context.getString(R.string.inpage_has_product_top_text)
        assertThat(ProductFixtures.storeProduct(18).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).contains(hasProductAccessoryTopText)
        assertThat(ProductFixtures.storeProduct(19).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).contains(hasProductAccessoryTopText)
        assertThat(ProductFixtures.storeProduct(25).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).contains(hasProductAccessoryTopText)
        assertThat(ProductFixtures.storeProduct(26).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).contains(hasProductAccessoryTopText)
    }

    @Test
    fun getRecommendationText_oneSizeProduct_fitScoreLargerThan84_returnSizeComparisonOneSizeCloseText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.bestFitScore = 84.5f
        val oneSizeCloseTopText = context.getString(R.string.inpage_one_size_close_top_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(
                    ProductSize("FREE", mutableSetOf())
                )
            ).getRecommendationText(
                i18nLocalization,
                sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName
            )
        ).contains(oneSizeCloseTopText)
    }

    @Test
    fun getRecommendationText_oneSizeProduct_storeProductIsSmaller_returnSizeComparisonOneSizeSmallerText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.isStoreProductSmaller = true
        val oneSizeSmallerTopText = context.getString(R.string.inpage_one_size_smaller_top_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(
                    ProductSize("FREE", mutableSetOf())
                )
            ).getRecommendationText(
                i18nLocalization,
                sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName
            )
        ).contains(oneSizeSmallerTopText)
    }

    @Test
    fun getRecommendationText_oneSizeProduct_storeProductIsLarger_returnSizeComparisonOneSizeLargerText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.bestFitScore = 60f
        sizeComparisonRecommendedSize?.isStoreProductSmaller = false
        val oneSizeLargerTopText = context.getString(R.string.inpage_one_size_larger_top_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(
                    ProductSize("FREE", mutableSetOf())
                )
            ).getRecommendationText(
                i18nLocalization,
                sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName
            )
        ).contains(oneSizeLargerTopText)
    }

    @Test
    fun getRecommendationText_oneSizeProduct_onlyHasBodyProfileRecommendedSize_returnOneSizeBodyProfileText() {
        bodyProfileRecommendedSizeName = "Small"
        val oneSizeBodyProfileText = context.getString(R.string.inpage_one_size_body_profile_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(
                    ProductSize("FREE", mutableSetOf())
                )
            ).getRecommendationText(
                i18nLocalization,
                sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName
            )
        ).contains(oneSizeBodyProfileText)
    }

    @Test
    fun getRecommendationText_multiSizeProduct_hasSizeComparisonRecommendedSize_returnMultiSizeComparisonText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.bestStoreProductSize = ProductFixtures.storeProduct(
            sizeList = mutableListOf(
                ProductSize("S", mutableSetOf()),
                ProductSize("M", mutableSetOf())
            )
        ).sizes[0]
        val multiSizeComparisonText = context.getString(R.string.inpage_multi_size_comparison_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(
                    ProductSize("S", mutableSetOf()),
                    ProductSize("M", mutableSetOf())
                )
            ).getRecommendationText(
                i18nLocalization,
                sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName
            )
        ).contains(multiSizeComparisonText)
    }

    @Test
    fun getRecommendationText_multiSizeProduct_hasBodyProfileRecommendedSize_returnMultiSizeBodyProfileText() {
        bodyProfileRecommendedSizeName = "S"
        val multiSizeBodyProfileText = context.getString(R.string.inpage_multi_size_body_profile_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(
                    ProductSize("S", mutableSetOf()),
                    ProductSize("M", mutableSetOf())
                )
            ).getRecommendationText(
                i18nLocalization,
                sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName
            )
        ).contains(multiSizeBodyProfileText)
    }

    @Test
    fun getRecommendationText_multiSizeProduct_noRecommendedSizes_returnNoDataText() {
        val noDataText = context.getString(R.string.inpage_no_data_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(
                    ProductSize("S", mutableSetOf()),
                    ProductSize("M", mutableSetOf())
                )
            ).getRecommendationText(
                i18nLocalization,
                sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName
            )
        ).contains(noDataText)
    }

    @Test
    fun getRecommendationText_productIsNotAnAccessory_returnNoDataText() {
        val noDataText = context.getString(R.string.inpage_no_data_text)
        assertThat(ProductFixtures.storeProduct(4).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(noDataText)
        assertThat(ProductFixtures.storeProduct(7).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(noDataText)
        assertThat(ProductFixtures.storeProduct(15).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(noDataText)
    }
}