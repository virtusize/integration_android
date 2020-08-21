package com.virtusize.libsource.util

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ExtensionsKtTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun getStringResourceByName_existingResources_shouldBeNotNull() {
        assertThat(context.getStringResourceByName("inpage_sizing_itemBrand_true_text")).isNotNull()
        assertThat(context.getStringResourceByName("inpage_sizing_itemBrand_small_text")).isNotNull()
        assertThat(context.getStringResourceByName("inpage_sizing_mostBrands_true_text")).isNotNull()
        assertThat(context.getStringResourceByName("inpage_sizing_mostBrands_large_text")).isNotNull()
        assertThat(context.getStringResourceByName("inpage_fit_regular_text")).isNotNull()
    }

    @Test
    fun getStringResourceByName_nonExistingResources_shouldBeNull() {
        assertThat(context.getStringResourceByName("inpage_sizing_itembrand_true_text")).isNull()
        assertThat(context.getStringResourceByName("inpage_sizing_itemBrand_regular_text")).isNull()
        assertThat(context.getStringResourceByName("inpage_sizing_mostbrands_true_text")).isNull()
        assertThat(context.getStringResourceByName("inpage_sizing_mostBrands_regular_text")).isNull()
        assertThat(context.getStringResourceByName("inpage_fit_slim_text")).isNull()
    }
}