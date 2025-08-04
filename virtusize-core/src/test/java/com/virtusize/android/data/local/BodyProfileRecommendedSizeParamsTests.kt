package com.virtusize.android.data.local

import com.google.common.truth.Truth.assertThat
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.fixtures.ProductFixtures
import com.virtusize.android.fixtures.TestFixtures
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

internal class BodyProfileRecommendedSizeParamsTests {
    private var productTypes = mutableListOf<ProductType>()

    @Before
    fun setup() {
        productTypes = ProductFixtures.productTypes()
    }

    @Test
    fun testCreateAdditionalInfoParams_fullInfo_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams =
            BodyProfileRecommendedSizeParams(
                productTypes,
                ProductFixtures.storeProduct(),
                TestFixtures.userBodyProfile,
            )
        val actualAdditionalInfoParams =
            bodyProfileRecommendedSizeParams.createAdditionalInfoParams()
        assertThat(JSONObject(actualAdditionalInfoParams).toString()).isEqualTo(
            """
            {
                "fit": "regular",
                "style": "fashionable",
                "sizes": {
                    "38": {
                        "bust": 660,
                        "sleeve": 845,
                        "height": 760
                    },
                    "36": {
                        "bust": 645,
                        "sleeve": 825,
                        "height": 750
                    }
                },
                "gender": "female",
                "brand": "Virtusize",
                "model_info": {
                    "waist": 56,
                    "bust": 78,
                    "size": "38",
                    "hip": 85,
                    "height": 165
                }
            }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), ""),
        )
    }

    @Test
    fun testCreateAdditionalInfoParams_hasEmptyValues_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams =
            BodyProfileRecommendedSizeParams(
                productTypes,
                ProductFixtures.storeProduct(
                    sizeList = mutableListOf(),
                    brand = "",
                    modelInfo = null,
                    gender = null,
                ),
                TestFixtures.userBodyProfile,
            )
        val actualAdditionalInfoParams =
            bodyProfileRecommendedSizeParams.createAdditionalInfoParams()
        assertThat(JSONObject(actualAdditionalInfoParams).toString()).isEqualTo(
            """
            {
                "fit": "regular",
                "style": "fashionable",
                "sizes": {},
                "gender": "male",
                "brand": "",
                "model_info": null
            }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), ""),
        )
    }

    @Test
    fun testCreateBodyDataParams_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams =
            BodyProfileRecommendedSizeParams(
                productTypes,
                ProductFixtures.storeProduct(),
                TestFixtures.userBodyProfile,
            )
        val bodyDataParams = bodyProfileRecommendedSizeParams.createBodyDataParams()
        assertThat(JSONObject(bodyDataParams).toString()).isEqualTo(
            """
            {
              "armpit_height": {
                "value": 1130,
                "predicted": true
              },
              "shoulder_height": {
                "value": 1240,
                "predicted": true
              },
              "chest": {
                "value": 755,
                "predicted": true
              },
              "bust_width": {
                "value": 245,
                "predicted": true
              },
              "hip_height": {
                "value": 750,
                "predicted": true
              },
              "thigh": {
                "value": 480,
                "predicted": true
              },
              "knee_height": {
                "value": 395,
                "predicted": true
              },
              "head_height": {
                "value": 215,
                "predicted": true
              },
              "hip_width": {
                "value": 300,
                "predicted": true
              },
              "neck": {
                "value": 300,
                "predicted": true
              },
              "hip": {
                "value": 830,
                "predicted": true
              },
              "waist_width": {
                "value": 225,
                "predicted": true
              },
              "waist_height": {
                "value": 920,
                "predicted": true
              },
              "shoulder_width": {
                "value": 340,
                "predicted": true
              },
              "bicep": {
                "value": 220,
                "predicted": true
              },
              "inseam": {
                "value": 700,
                "predicted": true
              },
              "sleeve": {
                "value": 720,
                "predicted": true
              },
              "bust": {
                "value": 755,
                "predicted": true
              },
              "waist": {
                "value": 630,
                "predicted": true
              },
              "rise": {
                "value": 215,
                "predicted": true
              },
              "shoulder": {
                "value": 370,
                "predicted": true
              },
              "sleeve_length": {
                "value": 520,
                "predicted": true
              }
            }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), ""),
        )
    }

    @Test
    fun testCreateItemSizesParams_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams =
            BodyProfileRecommendedSizeParams(
                productTypes,
                ProductFixtures.storeProduct(),
                TestFixtures.userBodyProfile,
            )
        val itemSizesParams = bodyProfileRecommendedSizeParams.createItemSizesParams()
        assertThat(JSONObject(itemSizesParams).toString().trimIndent()).isEqualTo(
            """
            {
                "38": {
                    "bust": 660,
                    "sleeve": 845,
                    "height": 760
                },
                "36": {
                    "bust": 645,
                    "sleeve": 825,
                    "height": 750
                }
            }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), ""),
        )
    }

    @Test
    fun testBodyProfileRecommendedSizeParams_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams =
            BodyProfileRecommendedSizeParams(
                productTypes,
                ProductFixtures.storeProduct(),
                TestFixtures.userBodyProfile,
            )
        val bodyProfileRecommendedSizeParamsMap = bodyProfileRecommendedSizeParams.paramsToMap()
        assertThat(bodyProfileRecommendedSizeParamsMap["user_gender"]).isEqualTo("female")
        assertThat(bodyProfileRecommendedSizeParamsMap["user_weight"]).isEqualTo(50)
        assertThat(bodyProfileRecommendedSizeParamsMap["user_height"]).isEqualTo(1630)
        assertThat(bodyProfileRecommendedSizeParamsMap["body_data"]).isEqualTo(
            mutableMapOf(
                "waist_width" to
                    mutableMapOf(
                        "value" to 225,
                        "predicted" to true,
                    ),
                "chest" to
                    mutableMapOf(
                        "value" to 755,
                        "predicted" to true,
                    ),
                "bust_width" to
                    mutableMapOf(
                        "value" to 245,
                        "predicted" to true,
                    ),
                "thigh" to
                    mutableMapOf(
                        "value" to 480,
                        "predicted" to true,
                    ),
                "shoulder_width" to
                    mutableMapOf(
                        "value" to 340,
                        "predicted" to true,
                    ),
                "hip_height" to
                    mutableMapOf(
                        "value" to 750,
                        "predicted" to true,
                    ),
                "knee_height" to
                    mutableMapOf(
                        "value" to 395,
                        "predicted" to true,
                    ),
                "neck" to
                    mutableMapOf(
                        "value" to 300,
                        "predicted" to true,
                    ),
                "waist_height" to
                    mutableMapOf(
                        "value" to 920,
                        "predicted" to true,
                    ),
                "hip" to
                    mutableMapOf(
                        "value" to 830,
                        "predicted" to true,
                    ),
                "armpit_height" to
                    mutableMapOf(
                        "value" to 1130,
                        "predicted" to true,
                    ),
                "bicep" to
                    mutableMapOf(
                        "value" to 220,
                        "predicted" to true,
                    ),
                "inseam" to
                    mutableMapOf(
                        "value" to 700,
                        "predicted" to true,
                    ),
                "head_height" to
                    mutableMapOf(
                        "value" to 215,
                        "predicted" to true,
                    ),
                "hip_width" to
                    mutableMapOf(
                        "value" to 300,
                        "predicted" to true,
                    ),
                "sleeve" to
                    mutableMapOf(
                        "value" to 720,
                        "predicted" to true,
                    ),
                "bust" to
                    mutableMapOf(
                        "value" to 755,
                        "predicted" to true,
                    ),
                "waist" to
                    mutableMapOf(
                        "value" to 630,
                        "predicted" to true,
                    ),
                "sleeve_length" to
                    mutableMapOf(
                        "value" to 520,
                        "predicted" to true,
                    ),
                "rise" to
                    mutableMapOf(
                        "value" to 215,
                        "predicted" to true,
                    ),
                "shoulder" to
                    mutableMapOf(
                        "value" to 370,
                        "predicted" to true,
                    ),
                "shoulder_height" to
                    mutableMapOf(
                        "value" to 1240,
                        "predicted" to true,
                    ),
            ),
        )
        val items = bodyProfileRecommendedSizeParamsMap["items"] as Array<Map<String, Any>>
        for (item in items) {
            assertThat(item["ext_product_id"]).isEqualTo("694")
            assertThat(item["product_type"]).isEqualTo("jacket")
            assertThat(item["item_sizes_orig"]).isEqualTo(
                mutableMapOf(
                    "38" to
                        mutableMapOf(
                            "bust" to 660,
                            "sleeve" to 845,
                            "height" to 760,
                        ),
                    "36" to
                        mutableMapOf(
                            "bust" to 645,
                            "sleeve" to 825,
                            "height" to 750,
                        ),
                ),
            )
            assertThat(item["additional_info"]).isEqualTo(
                mutableMapOf(
                    "fit" to "regular",
                    "sizes" to
                        mutableMapOf(
                            "38" to
                                mutableMapOf(
                                    "bust" to 660,
                                    "sleeve" to 845,
                                    "height" to 760,
                                ),
                            "36" to
                                mutableMapOf(
                                    "bust" to 645,
                                    "sleeve" to 825,
                                    "height" to 750,
                                ),
                        ),
                    "gender" to "female",
                    "brand" to "Virtusize",
                    "model_info" to
                        mutableMapOf(
                            "waist" to 56,
                            "bust" to 78,
                            "size" to "38",
                            "hip" to 85,
                            "height" to 165,
                        ),
                    "style" to "fashionable",
                ),
            )
        }
    }
}
