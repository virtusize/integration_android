package com.virtusize.android.data.local

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.data.remote.Measurement
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class KidBodyDataTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    @Before
    fun setup() {
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)
        sharedPreferencesHelper.deleteKidBodyData()
    }

    @Test
    fun isKidsEvent_onlyForSourceKids() {
        assertThat(KidBodyData.isKidsEvent(JSONObject("""{"source":"kids","gender":"boy"}"""))).isTrue()
        assertThat(KidBodyData.isKidsEvent(JSONObject("""{"gender":"boy"}"""))).isFalse()
        assertThat(KidBodyData.isKidsEvent(JSONObject("""{"source":"footwear"}"""))).isFalse()
        assertThat(KidBodyData.isKidsEvent(null)).isFalse()
    }

    @Test
    fun cache_overridesOnlyReceivedValuesAndDefaultsToGirl() {
        assertThat(KidBodyData.cached(sharedPreferencesHelper)).isNull()

        KidBodyData.cache(sharedPreferencesHelper, JSONObject("""{"source":"kids","age":"5","height":"107"}"""))
        assertThat(KidBodyData.cached(sharedPreferencesHelper)).isNull()

        KidBodyData.cache(sharedPreferencesHelper, JSONObject("""{"source":"kids","weight":"17"}"""))
        assertThat(KidBodyData.cached(sharedPreferencesHelper))
            .isEqualTo(KidBodyData(gender = "girl", height = 1070, weight = 17, age = 5))

        KidBodyData.cache(sharedPreferencesHelper, JSONObject("""{"source":"kids","gender":"boy"}"""))
        KidBodyData.cache(sharedPreferencesHelper, JSONObject("""{"source":"kids","height":110}"""))
        assertThat(KidBodyData.cached(sharedPreferencesHelper))
            .isEqualTo(KidBodyData(gender = "boy", height = 1100, weight = 17, age = 5))

        sharedPreferencesHelper.deleteKidBodyData()
        assertThat(KidBodyData.cached(sharedPreferencesHelper)).isNull()
        assertThat(sharedPreferencesHelper.getKidGender()).isNull()
    }

    @Test
    fun intValue_parsesWidgetEventValues() {
        assertThat(KidBodyData.intValue("107")).isEqualTo(107)
        assertThat(KidBodyData.intValue(" 17 ")).isEqualTo(17)
        assertThat(KidBodyData.intValue("17.6")).isEqualTo(18)
        assertThat(KidBodyData.intValue(5)).isEqualTo(5)
        assertThat(KidBodyData.intValue(5.0)).isEqualTo(5)
        assertThat(KidBodyData.intValue("3'6\"")).isNull()
        assertThat(KidBodyData.intValue("")).isNull()
        assertThat(KidBodyData.intValue(null)).isNull()
    }

    @Test
    fun paramsToMap_matchesPredictRequestBody() {
        val params = KidBodyData(gender = "boy", height = 1070, weight = 17, age = 5).paramsToMap()
        assertThat(JSONObject(params).toString()).isEqualTo("""{"gender":"boy","height":1070,"weight":17,"age":5}""")
    }

    @Test
    fun toUserBodyProfile_usesPredictedMeasurements() {
        val profile =
            KidBodyData(gender = "boy", height = 1070, weight = 17, age = 5).toUserBodyProfile(
                mapOf("ankleHeight" to 50, "hipWidth" to 195.0, "unknown" to null, "text" to "n/a"),
            )
        assertThat(profile.gender).isEqualTo("boy")
        assertThat(profile.age).isEqualTo(5)
        assertThat(profile.height).isEqualTo(1070)
        assertThat(profile.weight).isEqualTo("17")
        assertThat(profile.bodyData).isEqualTo(setOf(Measurement("ankleHeight", 50), Measurement("hipWidth", 195)))
    }
}
