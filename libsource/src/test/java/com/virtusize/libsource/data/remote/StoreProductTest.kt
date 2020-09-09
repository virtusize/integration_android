package com.virtusize.libsource.data.remote

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.R
import com.virtusize.libsource.TestFixtures
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class StoreProductTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var i18nLocalization: I18nLocalization

    @Before
    fun setup() {
        i18nLocalization = I18nLocalization(
            context.getString(R.string.inpage_default_text),
            context.getString(R.string.inpage_default_accessory_text),
            context.getString(R.string.inpage_sizing_itemBrand_large_text),
            context.getString(R.string.inpage_sizing_itemBrand_true_text),
            context.getString(R.string.inpage_sizing_itemBrand_small_text),
            context.getString(R.string.inpage_sizing_mostBrands_large_text),
            context.getString(R.string.inpage_sizing_mostBrands_true_text),
            context.getString(R.string.inpage_sizing_mostBrands_small_text),
            context.getString(R.string.inpage_fit_loose_text),
            context.getString(R.string.inpage_fit_regular_text),
            context.getString(R.string.inpage_fit_tight_text)
        )
    }

    @Test
    fun getRecommendationText_productIsAnAccessory_returnDefaultAccessoryText() {

        val defaultAccessoryText = context.getString(R.string.inpage_default_accessory_text)
        assertThat(TestFixtures.storeProduct(18).getRecommendationText(i18nLocalization)).isEqualTo(defaultAccessoryText)
        assertThat(TestFixtures.storeProduct(19).getRecommendationText(i18nLocalization)).isEqualTo(defaultAccessoryText)
        assertThat(TestFixtures.storeProduct(25).getRecommendationText(i18nLocalization)).isEqualTo(defaultAccessoryText)
        assertThat(TestFixtures.storeProduct(26).getRecommendationText(i18nLocalization)).isEqualTo(defaultAccessoryText)
    }

    @Test
    fun getRecommendationText_brandSizingIsNotNull_returnBrandSizingText() {
        assertThat(
            TestFixtures.storeProduct(
                1,
                brandSizing = BrandSizing("large", true)
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_sizing_itemBrand_large_text))

        assertThat(
            TestFixtures.storeProduct(
                15,
                brandSizing = BrandSizing("true", true)
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_sizing_itemBrand_true_text))

        assertThat(
            TestFixtures.storeProduct(
                20,
                brandSizing = BrandSizing("small", false)
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_sizing_mostBrands_small_text))

        assertThat(
            TestFixtures.storeProduct(
                24,
                brandSizing = BrandSizing("true", false)
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_sizing_mostBrands_true_text))
    }

    @Test
    fun getRecommendationText_brandSizingIsNullAndGeneralFitIsNull_returnDefaultText() {
        assertThat(
            TestFixtures.storeProduct(
                2,
                brandSizing = null,
                fit = null
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_default_text))
    }

    @Test
    fun getRecommendationText_brandSizingIsNullAndGeneralFitIsNotNull_returnGeneralFitText() {
        assertThat(
            TestFixtures.storeProduct(
                4,
                brandSizing = null,
                fit = "regular"
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_fit_regular_text))

        assertThat(
            TestFixtures.storeProduct(
                6,
                brandSizing = null,
                fit = "loose"
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_fit_loose_text))

        assertThat(
            TestFixtures.storeProduct(
                8,
                brandSizing = null,
                fit = "flared"
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_fit_loose_text))

        assertThat(
            TestFixtures.storeProduct(
                10,
                brandSizing = null,
                fit = "slim"
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_fit_tight_text))

        assertThat(
            TestFixtures.storeProduct(
                12,
                brandSizing = null,
                fit = "random"
            ).getRecommendationText(i18nLocalization)
        ).isEqualTo(context.getString(R.string.inpage_fit_regular_text))
    }
}