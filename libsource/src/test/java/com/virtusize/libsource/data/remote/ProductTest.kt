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
class ProductTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var i18nLocalization: I18nLocalization

    @Before
    fun setup() {
        i18nLocalization = I18nLocalization(
            context.getString(R.string.inpage_default_accessory_text),
            context.getString(R.string.inpage_no_data_text)
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
    fun getRecommendationText_productIsNotAnAccessory_returnNoDataText() {
        val noDataText = context.getString(R.string.inpage_no_data_text)
        assertThat(TestFixtures.storeProduct(4).getRecommendationText(i18nLocalization)).isEqualTo(noDataText)
        assertThat(TestFixtures.storeProduct(7).getRecommendationText(i18nLocalization)).isEqualTo(noDataText)
        assertThat(TestFixtures.storeProduct(15).getRecommendationText(i18nLocalization)).isEqualTo(noDataText)
    }
}