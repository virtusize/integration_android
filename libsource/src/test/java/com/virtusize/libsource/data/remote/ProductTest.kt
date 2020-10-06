package com.virtusize.libsource.data.remote

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.SizeComparisonRecommendedSize
import com.virtusize.libsource.fixtures.ProductFixtures
import com.virtusize.libsource.fixtures.TestFixtures
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
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
            context.getString(R.string.inpage_mutli_size_comparison_text),
            context.getString(R.string.inpage_mutli_size_body_profile_text),
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
    fun getRecommendationText_productIsNotAnAccessory_returnNoDataText() {
        val noDataText = context.getString(R.string.inpage_no_data_text)
        assertThat(ProductFixtures.storeProduct(4).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(noDataText)
        assertThat(ProductFixtures.storeProduct(7).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(noDataText)
        assertThat(ProductFixtures.storeProduct(15).getRecommendationText(i18nLocalization, sizeComparisonRecommendedSize, bodyProfileRecommendedSizeName)).isEqualTo(noDataText)
    }
}