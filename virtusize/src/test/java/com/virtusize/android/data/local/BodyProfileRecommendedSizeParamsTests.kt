package com.virtusize.android.data.local

import com.google.common.truth.Truth.assertThat
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.fixtures.ProductFixtures
import com.virtusize.android.fixtures.TestFixtures
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class BodyProfileRecommendedSizeParamsTests {

    private var productTypes = mutableListOf<ProductType>()

    @Before
    fun setup() {
        productTypes = ProductFixtures.productTypes()
    }

    @Test
    fun testCreateAdditionalInfoParams_fullInfo_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams = BodyProfileRecommendedSizeParams(
            productTypes,
            ProductFixtures.storeProduct(),
            TestFixtures.userBodyProfile
        )
        val actualAdditionalInfoParams =
            bodyProfileRecommendedSizeParams.createAdditionalInfoParams()
        assertThat(JSONObject(actualAdditionalInfoParams).toString()).isEqualTo(
            """
                {
                    "fit": "regular",
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
                    "modelInfo": {
                        "waist": 56,
                        "bust": 78,
                        "size": "38",
                        "hip": 85,
                        "height": 165
                    },
                    "gender": "female",
                    "brand": "Virtusize"
                }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")
        )
    }

    @Test
    fun testCreateAdditionalInfoParams_hasEmptyValues_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams = BodyProfileRecommendedSizeParams(
            productTypes,
            ProductFixtures.storeProduct(
                sizeList = mutableListOf(),
                brand = "",
                modelInfo = null,
                gender = null
            ),
            TestFixtures.userBodyProfile
        )
        val actualAdditionalInfoParams =
            bodyProfileRecommendedSizeParams.createAdditionalInfoParams()
        assertThat(JSONObject(actualAdditionalInfoParams).toString()).isEqualTo(
            """
                {
                    "fit": "regular",
                    "sizes": {},
                    "modelInfo": {},
                    "gender": null,
                    "brand": ""
                }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")
        )
    }

    @Test
    fun testCreateBodyDataParams_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams = BodyProfileRecommendedSizeParams(
            productTypes,
            ProductFixtures.storeProduct(),
            TestFixtures.userBodyProfile
        )
        val bodyDataParams = bodyProfileRecommendedSizeParams.createBodyDataParams()
        assertThat(JSONObject(bodyDataParams).toString()).isEqualTo(
            """
            {
                "waistWidth": {
                    "value": 225,
                    "predicted": true
                },
                "chest": {
                    "value": 755,
                    "predicted": true
                },
                "bustWidth": {
                    "value": 245,
                    "predicted": true
                },
                "thigh": {
                    "value": 480,
                    "predicted": true
                },
                "shoulderWidth": {
                    "value": 340,
                    "predicted": true
                },
                "hipHeight": {
                    "value": 750,
                    "predicted": true
                },
                "kneeHeight": {
                    "value": 395,
                    "predicted": true
                },
                "neck": {
                    "value": 300,
                    "predicted": true
                },
                "waistHeight": {
                    "value": 920,
                    "predicted": true
                },
                "hip": {
                    "value": 830,
                    "predicted": true
                },
                "armpitHeight": {
                    "value": 1130,
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
                "headHeight": {
                    "value": 215,
                    "predicted": true
                },
                "hipWidth": {
                    "value": 300,
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
                "sleeveLength": {
                    "value": 520,
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
                "shoulderHeight": {
                    "value": 1240,
                    "predicted": true
                }
            }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")
        )
    }

    @Test
    fun testCreateItemSizesParams_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams = BodyProfileRecommendedSizeParams(
            productTypes,
            ProductFixtures.storeProduct(),
            TestFixtures.userBodyProfile
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
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")
        )
    }

    @Test
    fun testBodyProfileRecommendedSizeParams_getExpectedRequestBodyString() {
        val bodyProfileRecommendedSizeParams = BodyProfileRecommendedSizeParams(
            productTypes,
            ProductFixtures.storeProduct(),
            TestFixtures.userBodyProfile
        )
        val bodyProfileRecommendedSizeParamsMap = bodyProfileRecommendedSizeParams.paramsToMap()
        assertThat(bodyProfileRecommendedSizeParamsMap["userGender"]).isEqualTo("female")
        assertThat(bodyProfileRecommendedSizeParamsMap["userWeight"]).isEqualTo(50)
        assertThat(bodyProfileRecommendedSizeParamsMap["userHeight"]).isEqualTo(1630)
        assertThat(bodyProfileRecommendedSizeParamsMap["extProductId"]).isEqualTo("694")
        assertThat(bodyProfileRecommendedSizeParamsMap["productType"]).isEqualTo("jacket")
        assertThat(
            JSONObject(
                bodyProfileRecommendedSizeParamsMap["itemSizesOrig"] as Map<String, Any>
            ).toString()
        ).isEqualTo(
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
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")
        )
        assertThat(
            JSONObject(
                bodyProfileRecommendedSizeParamsMap["additionalInfo"] as Map<String, Any>
            ).toString()
        ).isEqualTo(
            """
                {
                    "fit": "regular",
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
                    "modelInfo": {
                        "waist": 56,
                        "bust": 78,
                        "size": "38",
                        "hip": 85,
                        "height": 165
                    },
                    "gender": "female",
                    "brand": "Virtusize"
                }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")
        )
        assertThat(
            JSONObject(
                bodyProfileRecommendedSizeParamsMap["bodyData"] as Map<String, Any>
            ).toString()
        ).isEqualTo(
            """
                {
                    "waistWidth": {
                        "value": 225,
                        "predicted": true
                    },
                    "chest": {
                        "value": 755,
                        "predicted": true
                    },
                    "bustWidth": {
                        "value": 245,
                        "predicted": true
                    },
                    "thigh": {
                        "value": 480,
                        "predicted": true
                    },
                    "shoulderWidth": {
                        "value": 340,
                        "predicted": true
                    },
                    "hipHeight": {
                        "value": 750,
                        "predicted": true
                    },
                    "kneeHeight": {
                        "value": 395,
                        "predicted": true
                    },
                    "neck": {
                        "value": 300,
                        "predicted": true
                    },
                    "waistHeight": {
                        "value": 920,
                        "predicted": true
                    },
                    "hip": {
                        "value": 830,
                        "predicted": true
                    },
                    "armpitHeight": {
                        "value": 1130,
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
                    "headHeight": {
                        "value": 215,
                        "predicted": true
                    },
                    "hipWidth": {
                        "value": 300,
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
                    "sleeveLength": {
                        "value": 520,
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
                    "shoulderHeight": {
                        "value": 1240,
                        "predicted": true
                    }
                }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")
        )
    }
}
