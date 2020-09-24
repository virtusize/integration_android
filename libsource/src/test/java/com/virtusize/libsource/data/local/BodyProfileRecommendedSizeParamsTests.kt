package com.virtusize.libsource.data.local

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.fixtures.ProductFixtures
import com.virtusize.libsource.fixtures.TestFixtures
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class BodyProfileRecommendedSizeParamsTests {

    @Test
    fun testCreateAdditionalInfoParams_fullInfo_getExpectedRequestBody() {
        val bodyProfileRecommendedSizeParams = BodyProfileRecommendedSizeParams(ProductFixtures.storeProduct(), TestFixtures.userBodyProfile)
        val actualAdditionalInfoParams = bodyProfileRecommendedSizeParams.createAdditionalInfoParams()
        assertThat(JSONObject(actualAdditionalInfoParams).toString()).isEqualTo(
            """
                {"fit":"regular","sizes":{"38":{"bust":660,"sleeve":845,"height":760},"36":{"bust":645,"sleeve":825,"height":750}},"gender":"female","brand":"Virtusize","model_info":{"waist":56,"bust":78,"size":"38","hip":85,"height":165}}
            """.trimIndent()
        )
    }

    @Test
    fun testCreateAdditionalInfoParams_hasEmptyValues_getExpectedRequestBody() {
        val bodyProfileRecommendedSizeParams = BodyProfileRecommendedSizeParams(
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(),
                brand = "",
                modelInfo = null,
                gender = null
            ),
            TestFixtures.userBodyProfile
        )
        val actualAdditionalInfoParams = bodyProfileRecommendedSizeParams.createAdditionalInfoParams()
        assertThat(JSONObject(actualAdditionalInfoParams).toString()).isEqualTo(
            """
                {"fit":"regular","sizes":{},"gender":null,"brand":"","model_info":"{}"}
            """.trimIndent()
        )
    }
}