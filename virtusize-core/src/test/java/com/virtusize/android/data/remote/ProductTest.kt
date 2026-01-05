package com.virtusize.android.data.remote

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.core.R
import com.virtusize.android.data.local.SizeComparisonRecommendedSize
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.fixtures.ProductFixtures
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
        i18nLocalization =
            I18nLocalization(
                language = VirtusizeLanguage.EN,
                defaultAccessoryText = context.getString(R.string.inpage_default_accessory_text),
                hasProductAccessoryTopText = context.getString(R.string.inpage_has_product_top_text),
                hasProductAccessoryBottomText = context.getString(R.string.inpage_has_product_bottom_text),
                oneSizeCloseTopText = context.getString(R.string.inpage_one_size_close_top_text),
                oneSizeSmallerTopText = context.getString(R.string.inpage_one_size_smaller_top_text),
                oneSizeLargerTopText = context.getString(R.string.inpage_one_size_larger_top_text),
                oneSizeCloseBottomText = context.getString(R.string.inpage_one_size_close_bottom_text),
                oneSizeSmallerBottomText = context.getString(R.string.inpage_one_size_smaller_bottom_text),
                oneSizeLargerBottomText = context.getString(R.string.inpage_one_size_larger_bottom_text),
                oneSizeWillFitResultText = context.getString(R.string.inpage_one_size_will_fit_result_text),
                sizeComparisonMultiSizeText = context.getString(R.string.inpage_multi_size_comparison_text),
                willFitResultText = context.getString(R.string.inpage_will_fit_result_text),
                willNotFitResultText = context.getString(R.string.inpage_will_not_fit_result_text),
                willNotFitResultDefaultText = context.getString(R.string.inpage_will_not_fit_result_default_text),
                bodyDataEmptyText = context.getString(R.string.inpage_body_data_empty_text),
            )
    }

    @Test
    fun getRecommendationText_productIsAnAccessory_returnDefaultAccessoryText() {
        val defaultAccessoryText = context.getString(R.string.inpage_default_accessory_text)
        assertThat(
            ProductFixtures.storeProduct(18).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).isEqualTo(defaultAccessoryText)
        assertThat(
            ProductFixtures.storeProduct(19).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).isEqualTo(defaultAccessoryText)
        assertThat(
            ProductFixtures.storeProduct(25).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).isEqualTo(defaultAccessoryText)
        assertThat(
            ProductFixtures.storeProduct(26).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).isEqualTo(defaultAccessoryText)
    }

    @Test
    fun getRecommendationText_productIsAnAcc_hasSizeComparisonRecSize_returnHasProductAccText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.bestStoreProductSize =
            ProductFixtures.storeProduct(18).sizes.get(0)
        val hasProductAccessoryTopText = context.getString(R.string.inpage_has_product_top_text)
        assertThat(
            ProductFixtures.storeProduct(18).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(hasProductAccessoryTopText)
        assertThat(
            ProductFixtures.storeProduct(19).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(hasProductAccessoryTopText)
        assertThat(
            ProductFixtures.storeProduct(25).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(hasProductAccessoryTopText)
        assertThat(
            ProductFixtures.storeProduct(26).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(hasProductAccessoryTopText)
    }

    @Test
    fun getRecText_oneSizeProduct_fitScoreLargerThan84_returnSizeComparisonOneSizeCloseText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.bestFitScore = 84.5f
        val oneSizeCloseTopText = context.getString(R.string.inpage_one_size_close_top_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList =
                    mutableListOf(
                        ProductSize("FREE", mutableSetOf()),
                    ),
            ).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(oneSizeCloseTopText)
    }

    @Test
    fun getRecText_oneSizeProduct_storeProductIsSmaller_returnSizeComparisonOneSizeSmallerText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.isStoreProductSmaller = true
        val oneSizeSmallerTopText = context.getString(R.string.inpage_one_size_smaller_top_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList =
                    mutableListOf(
                        ProductSize("FREE", mutableSetOf()),
                    ),
            ).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(oneSizeSmallerTopText)
    }

    @Test
    fun getRecText_oneSizeProduct_storeProductIsLarger_returnSizeComparisonOneSizeLargerText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.bestFitScore = 60f
        sizeComparisonRecommendedSize?.isStoreProductSmaller = false
        val oneSizeLargerTopText = context.getString(R.string.inpage_one_size_larger_top_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList =
                    mutableListOf(
                        ProductSize("FREE", mutableSetOf()),
                    ),
            ).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(oneSizeLargerTopText)
    }

    @Test
    fun getRecText_oneSizeProduct_onlyHasBodyProfileRecommendedSize_returnOneSizeBodyProfileText() {
        bodyProfileRecommendedSizeName = "Small"
        val oneSizeBodyProfileText = context.getString(R.string.inpage_one_size_will_fit_result_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList =
                    mutableListOf(
                        ProductSize("FREE", mutableSetOf()),
                    ),
            ).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(oneSizeBodyProfileText)
    }

    @Test
    fun getRecText_multiSizeProduct_hasSizeComparisonRecSize_returnMultiSizeComparisonText() {
        sizeComparisonRecommendedSize = SizeComparisonRecommendedSize()
        sizeComparisonRecommendedSize?.bestStoreProductSize =
            ProductFixtures.storeProduct(
                sizeList =
                    mutableListOf(
                        ProductSize("S", mutableSetOf()),
                        ProductSize("M", mutableSetOf()),
                    ),
            ).sizes[0]
        val multiSizeComparisonText = context.getString(R.string.inpage_multi_size_comparison_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList =
                    mutableListOf(
                        ProductSize("S", mutableSetOf()),
                        ProductSize("M", mutableSetOf()),
                    ),
            ).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(multiSizeComparisonText)
    }

    @Test
    fun getRecText_multiSizeProduct_hasBodyProfileRecommendedSize_returnMultiSizeBodyProfileText() {
        bodyProfileRecommendedSizeName = "S"
        val multiSizeBodyProfileText =
            context.getString(R.string.inpage_will_fit_result_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList =
                    mutableListOf(
                        ProductSize("S", mutableSetOf()),
                        ProductSize("M", mutableSetOf()),
                    ),
            ).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(multiSizeBodyProfileText)
    }

    @Test
    fun getRecommendationText_multiSizeProduct_noRecommendedSizes_returnBodyDataEmptyText() {
        val bodyDataEmptyText = context.getString(R.string.inpage_body_data_empty_text)
        assertThat(
            ProductFixtures.storeProduct(
                sizeList =
                    mutableListOf(
                        ProductSize("S", mutableSetOf()),
                        ProductSize("M", mutableSetOf()),
                    ),
            ).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).contains(bodyDataEmptyText)
    }

    @Test
    fun getRecommendationText_productIsNotAnAccessory_returnBodyDataEmptyText() {
        val bodyDataEmptyText = context.getString(R.string.inpage_body_data_empty_text)
        assertThat(
            ProductFixtures.storeProduct(4).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).isEqualTo(bodyDataEmptyText)
        assertThat(
            ProductFixtures.storeProduct(7).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).isEqualTo(bodyDataEmptyText)
        assertThat(
            ProductFixtures.storeProduct(15).getRecommendationText(
                context = context,
                i18nLocalization = i18nLocalization,
                sizeComparisonRecommendedSize = sizeComparisonRecommendedSize,
                bodyProfileRecommendedSizeName = bodyProfileRecommendedSizeName,
            ),
        ).isEqualTo(bodyDataEmptyText)
    }
}
