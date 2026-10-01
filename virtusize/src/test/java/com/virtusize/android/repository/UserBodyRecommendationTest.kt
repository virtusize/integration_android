package com.virtusize.android.repository

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.VirtusizeRepository
import com.virtusize.android.data.local.KidBodyData
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.recommendedSizeName
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class UserBodyRecommendationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)
    private lateinit var repository: VirtusizeRepository

    @Before
    fun setup() {
        sharedPreferencesHelper.deleteKidBodyData()
        repository =
            VirtusizeRepository(
                context = context,
                messageHandler =
                    object : VirtusizeMessageHandler {
                        override fun onEvent(
                            product: VirtusizeProduct,
                            event: VirtusizeEvent,
                        ) = Unit

                        override fun onError(error: VirtusizeError) = Unit
                    },
                virtusizeAPIService = MockVirtusizeApiService(),
            )
    }

    @After
    fun tearDown() {
        sharedPreferencesHelper.deleteKidBodyData()
    }

    @Test
    fun updateUserBodyRecommendedSize_emptyOrNull_keepsStoredSize() {
        repository.updateUserBodyRecommendedSize("M")

        repository.updateUserBodyRecommendedSize("")
        assertThat(repository.storedUserBodyRecommendedSize()).isEqualTo("M")

        repository.updateUserBodyRecommendedSize(null)
        assertThat(repository.storedUserBodyRecommendedSize()).isEqualTo("M")
    }

    @Test
    fun userUpdatedBodyMeasurements_missingSizeRecName_returnsNoRecommendedSize() {
        val event =
            VirtusizeEvent.UserUpdatedBodyMeasurements(
                JSONObject()
                    .put("source", "fit-illustrator")
                    .put("age", 30)
                    .put("height", 175)
                    .put("weight", 70),
            )

        assertThat(event.recommendedSizeName()).isNull()
    }

    @Test
    fun userUpdatedBodyMeasurements_nonEmptySizeRecName_returnsRecommendedSize() {
        val event =
            VirtusizeEvent.UserUpdatedBodyMeasurements(
                JSONObject()
                    .put("source", "fit-illustrator")
                    .put("sizeRecName", "M"),
            )

        assertThat(event.recommendedSizeName()).isEqualTo("M")
    }

    @Test
    fun updateKidBodyData_kidsSource_cachesBodyInputs() {
        val eventData =
            JSONObject()
                .put("source", "kids")
                .put("age", 8)
                .put("height", 128)
                .put("weight", 27)

        assertThat(repository.updateKidBodyData(eventData)).isTrue()
        assertThat(KidBodyData.cached(sharedPreferencesHelper))
            .isEqualTo(
                KidBodyData(
                    gender = KidBodyData.DEFAULT_GENDER,
                    height = 1280,
                    weight = 27,
                    age = 8,
                ),
            )
    }

    private fun VirtusizeRepository.storedUserBodyRecommendedSize(): String? {
        val field = VirtusizeRepository::class.java.getDeclaredField("userBodyRecommendedSize")
        field.isAccessible = true
        return field.get(this) as String?
    }
}
