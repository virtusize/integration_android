package com.virtusize.libsource.data.parsers

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.R
import com.virtusize.libsource.fixtures.TestFixtures
import com.virtusize.libsource.data.local.VirtusizeLanguage
import com.virtusize.libsource.data.remote.I18nLocalization
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class I18nLocalizationJsonParserTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun parseI18N_englishLocalization_shouldReturnExpectedObject() {
        val actualI18nLocalization = I18nLocalizationJsonParser(context, VirtusizeLanguage.EN).parse(readFileFromAssets("/i18n_en.json"))

        val expectedI18nLocalization = I18nLocalization(
            context.getString(R.string.inpage_default_accessory_text),
            context.getString(R.string.inpage_no_data_text)
        )

        assertThat(actualI18nLocalization).isEqualTo(expectedI18nLocalization)
    }

    @Test
    fun parseI18N_emptyJsonData_shouldReturnExpectedObject() {
        val actualI18nLocalization = I18nLocalizationJsonParser(context, VirtusizeLanguage.EN).parse(
            TestFixtures.EMPTY_JSON_DATA)

        val expectedI18nLocalization = I18nLocalization(
            "",
            ""
        )

        assertThat(actualI18nLocalization).isEqualTo(expectedI18nLocalization)
    }

    @Test
    fun parseI18NJP_japaneseLocalization_shouldReturnExpectedObject() {
        val actualI18nLocalization = I18nLocalizationJsonParser(context, VirtusizeLanguage.JP).parse(readFileFromAssets("/i18n_jp.json"))

        var conf: Configuration = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(Locale.JAPAN)
        val localizedContext = context.createConfigurationContext(conf)
        val expectedI18nLocalization = I18nLocalization(
            localizedContext.getString(R.string.inpage_default_accessory_text),
            localizedContext.getString(R.string.inpage_no_data_text)
        )

        assertThat(actualI18nLocalization).isEqualTo(expectedI18nLocalization)
    }

    @Test
    fun parseI18NKO_koreanLocalization_shouldReturnExpectedObject() {
        val actualI18nLocalization = I18nLocalizationJsonParser(context, VirtusizeLanguage.KR).parse(readFileFromAssets("/i18n_ko.json"))

        var conf: Configuration = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(Locale.KOREA)
        val localizedContext = context.createConfigurationContext(conf)
        val expectedI18nLocalization = I18nLocalization(
            localizedContext.getString(R.string.inpage_default_accessory_text),
            localizedContext.getString(R.string.inpage_no_data_text)
        )

        assertThat(actualI18nLocalization).isEqualTo(expectedI18nLocalization)
    }

    private fun readFileFromAssets(fileName: String): JSONObject {
        return try {
            val file = File(javaClass.getResource(fileName).path)
            val `is`: InputStream = file.inputStream()
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            JSONObject(String(buffer))
        } catch (e: IOException) {
            TestFixtures.EMPTY_JSON_DATA
            throw RuntimeException(e)
        }
    }
}