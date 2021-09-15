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
@Config(sdk = [Build.VERSION_CODES.Q])
class ExtensionsKtTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun getStringResourceByName_existingResources_shouldBeNotNull() {
        assertThat(context.getStringResourceByName("inpage_default_accessory_text")).isNotNull()
        assertThat(context.getStringResourceByName("inpage_no_data_text")).isNotNull()
        assertThat(context.getStringResourceByName("inpage_loading_text")).isNotNull()
        assertThat(context.getStringResourceByName("inpage_long_error_text")).isNotNull()
    }

    @Test
    fun getStringResourceByName_nonExistingResources_shouldBeNull() {
        assertThat(context.getStringResourceByName("random_text")).isNull()
        assertThat(context.getStringResourceByName("inpage_size_text")).isNull()
        assertThat(context.getStringResourceByName("inpage_standard_loading_text")).isNull()
    }
}
