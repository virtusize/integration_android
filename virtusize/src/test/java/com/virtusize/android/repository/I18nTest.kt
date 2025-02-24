package com.virtusize.android.repository

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.TestUtils
import com.virtusize.android.VirtusizeRepository
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.fixtures.ProductFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
@ExperimentalCoroutinesApi
class I18nTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val messageHandler =
        object : VirtusizeMessageHandler {
            override fun onEvent(
                product: VirtusizeProduct,
                event: VirtusizeEvent,
            ) {
                // nothing
            }

            override fun onError(error: VirtusizeError) {
                // nothing
            }
        }

    @Test
    fun no_specific_texts() =
        runTest {
            val apiService = MockVirtusizeApiService(context)
            val repo = VirtusizeRepository(context, messageHandler, apiService)

            apiService.mockStoreInfo = { ProductFixtures.store() }
            apiService.mockI18n = { _ -> TestUtils.readFileFromAssets("/i18n_en.json") }
            apiService.mockStoreSpecificI18n = { null }

            val i18n = repo.fetchLanguage(null)
            assertThat(i18n).isNotNull()
            assertThat(i18n!!.bodyDataEmptyText).isEqualTo("Find your right size")
        }

    @Test
    fun handle_specific_texts_no_override() =
        runTest {
            val apiService = MockVirtusizeApiService(context)
            val repo = VirtusizeRepository(context, messageHandler, apiService)

            apiService.mockStoreInfo = { ProductFixtures.store() }
            apiService.mockI18n = { _ -> TestUtils.readFileFromAssets("/i18n_en.json") }
            apiService.mockStoreSpecificI18n = { TestUtils.readFileFromAssets("/store_specific_i18n.json") }

            val i18n = repo.fetchLanguage(VirtusizeLanguage.EN)
            assertThat(i18n).isNotNull()
            assertThat(i18n!!.bodyDataEmptyText).isEqualTo("Find your right size")
        }

    @Test
    fun handle_specific_texts_with_override() =
        runTest {
            val apiService = MockVirtusizeApiService(context)
            val repo = VirtusizeRepository(context, messageHandler, apiService)

            apiService.mockStoreInfo = { ProductFixtures.store() }
            apiService.mockI18n = { _ -> TestUtils.readFileFromAssets("/i18n_en.json") }
            apiService.mockStoreSpecificI18n = { TestUtils.readFileFromAssets("/store_specific_i18n.json") }

            val i18n = repo.fetchLanguage(VirtusizeLanguage.JP)
            assertThat(i18n).isNotNull()
            assertThat(i18n!!.bodyDataEmptyText).isEqualTo("サイズチ")
        }
}
