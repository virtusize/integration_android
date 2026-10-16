package com.virtusize.android.data.remote

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.fixtures.TestFixtures
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class UserBodyProfileCacheTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    @Before
    fun setup() {
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)
        UserBodyProfile.clearCache(sharedPreferencesHelper)
        KidBodyProfile.clearCache(sharedPreferencesHelper)
    }

    @Test
    fun isKidsEvent_onlyForSourceKids() {
        assertThat(KidBodyProfile.isKidsEvent(JSONObject("""{"source":"kids","gender":"boy"}"""))).isTrue()
        assertThat(KidBodyProfile.isKidsEvent(JSONObject("""{"gender":"boy"}"""))).isFalse()
        assertThat(KidBodyProfile.isKidsEvent(JSONObject("""{"source":"footwear"}"""))).isFalse()
        assertThat(KidBodyProfile.isKidsEvent(null)).isFalse()
    }

    @Test
    fun cacheKidsEvent_overridesOnlyReceivedValuesAndLeavesUserProfile() {
        assertThat(KidBodyProfile.cached(sharedPreferencesHelper)).isNull()

        KidBodyProfile.cacheKidsEvent(
            sharedPreferencesHelper,
            JSONObject("""{"source":"kids","age":"5","height":"107"}"""),
        )
        val incomplete = KidBodyProfile.cached(sharedPreferencesHelper)
        assertThat(incomplete).isNotNull()
        assertThat(incomplete!!.hasKidPredictInputs).isFalse()
        assertThat(UserBodyProfile.cached(sharedPreferencesHelper)).isNull()

        KidBodyProfile.cacheKidsEvent(
            sharedPreferencesHelper,
            JSONObject("""{"source":"kids","weight":"17"}"""),
        )
        val complete = KidBodyProfile.cached(sharedPreferencesHelper)!!
        assertThat(complete.gender).isEqualTo(KidBodyProfile.DEFAULT_GENDER)
        assertThat(complete.height).isEqualTo(1070)
        assertThat(complete.weight).isEqualTo("17")
        assertThat(complete.age).isEqualTo(5)
        assertThat(complete.hasKidPredictInputs).isTrue()

        KidBodyProfile.cacheKidsEvent(
            sharedPreferencesHelper,
            JSONObject("""{"source":"kids","gender":"boy"}"""),
        )
        KidBodyProfile.cacheKidsEvent(
            sharedPreferencesHelper,
            JSONObject("""{"source":"kids","height":110}"""),
        )
        val updated = KidBodyProfile.cached(sharedPreferencesHelper)!!
        assertThat(updated.gender).isEqualTo("boy")
        assertThat(updated.height).isEqualTo(1100)
        assertThat(updated.weight).isEqualTo("17")
        assertThat(updated.age).isEqualTo(5)
        assertThat(UserBodyProfile.cached(sharedPreferencesHelper)).isNull()

        KidBodyProfile.clearCache(sharedPreferencesHelper)
        assertThat(KidBodyProfile.cached(sharedPreferencesHelper)).isNull()
    }

    @Test
    fun cache_storesWholeApiProfileForAdultAndShoe() {
        val apiProfile = TestFixtures.userBodyProfile
        UserBodyProfile.cache(sharedPreferencesHelper, apiProfile)

        val cached = UserBodyProfile.cached(sharedPreferencesHelper)!!
        assertThat(cached.gender).isEqualTo("female")
        assertThat(cached.age).isEqualTo(32)
        assertThat(cached.height).isEqualTo(1630)
        assertThat(cached.weight).isEqualTo("50.00")
        assertThat(cached.bodyData).isEqualTo(apiProfile.bodyData)
    }

    @Test
    fun intValue_parsesWidgetEventValues() {
        assertThat(KidBodyProfile.intValue("107")).isEqualTo(107)
        assertThat(KidBodyProfile.intValue(" 17 ")).isEqualTo(17)
        assertThat(KidBodyProfile.intValue("17.6")).isEqualTo(18)
        assertThat(KidBodyProfile.intValue(5)).isEqualTo(5)
        assertThat(KidBodyProfile.intValue(5.0)).isEqualTo(5)
        assertThat(KidBodyProfile.intValue("3'6\"")).isNull()
        assertThat(KidBodyProfile.intValue("")).isNull()
        assertThat(KidBodyProfile.intValue(null)).isNull()
    }

    @Test
    fun predictParams_matchesPredictRequestBody() {
        assertThat(TestFixtures.kidsPredictInputs.predictParams()).isEqualTo(
            mapOf("gender" to "boy", "height" to 1070, "weight" to 17, "age" to 5),
        )
    }

    @Test
    fun applyingPredictedMeasurements_usesPredictedMeasurements() {
        val profile =
            TestFixtures.kidsPredictInputs.applyingPredictedMeasurements(
                mapOf("ankleHeight" to 50, "hipWidth" to 195.0, "unknown" to null, "text" to "n/a"),
            )
        assertThat(profile.gender).isEqualTo("boy")
        assertThat(profile.age).isEqualTo(5)
        assertThat(profile.height).isEqualTo(1070)
        assertThat(profile.weight).isEqualTo("17")
        assertThat(profile.origin).isNull()
        assertThat(profile.bodyData).isEqualTo(setOf(Measurement("ankleHeight", 50), Measurement("hipWidth", 195)))
    }
}
