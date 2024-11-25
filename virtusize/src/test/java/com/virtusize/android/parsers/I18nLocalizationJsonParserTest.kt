package com.virtusize.android.data.parsers

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.TestUtils
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.remote.I18nLocalization
import com.virtusize.android.fixtures.TestFixtures
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class I18nLocalizationJsonParserTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun parseI18N_englishLocalization_shouldReturnExpectedObject() {
        val actualI18nLocalization =
            I18nLocalizationJsonParser(context, VirtusizeLanguage.EN).parse(
                TestUtils.readFileFromAssets("/i18n_en.json"),
            )

        val expectedI18nLocalization = getExpectedI18nLocalization(context)

        assertThat(actualI18nLocalization).isEqualTo(expectedI18nLocalization)
    }

    @Test
    fun parseI18N_emptyJsonData_shouldReturnExpectedObject() {
        val actualI18nLocalization =
            I18nLocalizationJsonParser(context, VirtusizeLanguage.EN).parse(
                TestFixtures.EMPTY_JSON_DATA,
            )

        val expectedI18nLocalization =
            I18nLocalization(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
            )

        assertThat(actualI18nLocalization).isEqualTo(expectedI18nLocalization)
    }

    @Test
    fun parseI18NJP_japaneseLocalization_shouldReturnExpectedObject() {
        val actualI18nLocalization =
            I18nLocalizationJsonParser(
                context,
                VirtusizeLanguage.JP,
            ).parse(TestUtils.readFileFromAssets("/i18n_jp.json"))

        var conf: Configuration = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(Locale.JAPAN)
        val localizedContext = context.createConfigurationContext(conf)
        val expectedI18nLocalization = getExpectedI18nLocalization(localizedContext)

        assertThat(actualI18nLocalization).isEqualTo(expectedI18nLocalization)
    }

    @Test
    fun parseI18NKO_koreanLocalization_shouldReturnExpectedObject() {
        val actualI18nLocalization =
            I18nLocalizationJsonParser(
                context,
                VirtusizeLanguage.KR,
            ).parse(TestUtils.readFileFromAssets("/i18n_ko.json"))

        var conf: Configuration = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(Locale.KOREA)
        val localizedContext = context.createConfigurationContext(conf)
        val expectedI18nLocalization = getExpectedI18nLocalization(localizedContext)

        assertThat(actualI18nLocalization).isEqualTo(expectedI18nLocalization)
    }

    private fun getExpectedI18nLocalization(localizedContext: Context): I18nLocalization {
        return I18nLocalization(
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_default_accessory_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_has_product_top_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_has_product_bottom_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_one_size_close_top_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_one_size_smaller_top_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_one_size_larger_top_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_one_size_close_bottom_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_one_size_smaller_bottom_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_one_size_larger_bottom_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_one_size_will_fit_result_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_multi_size_comparison_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_will_fit_result_text,
            ),
            localizedContext.getString(
                com.virtusize.android.core.R.string.inpage_no_data_text,
            ),
        )
    }
}
