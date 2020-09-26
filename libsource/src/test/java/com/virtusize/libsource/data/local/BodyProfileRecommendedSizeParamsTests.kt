package com.virtusize.libsource.data.local

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.parsers.ProductTypeJsonParser
import com.virtusize.libsource.data.remote.ProductType
import com.virtusize.libsource.fixtures.ProductFixtures
import com.virtusize.libsource.fixtures.TestFixtures
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
        val actualAdditionalInfoParams = bodyProfileRecommendedSizeParams.createAdditionalInfoParams()
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
        val actualAdditionalInfoParams = bodyProfileRecommendedSizeParams.createAdditionalInfoParams()
        assertThat(JSONObject(actualAdditionalInfoParams).toString()).isEqualTo(
            """
                {
                    "fit": "regular",
                    "sizes": {},
                    "gender": null,
                    "brand": "",
                    "model_info": "{}"
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
        assertThat(JSONObject(bodyProfileRecommendedSizeParamsMap).toString()).isEqualTo(
            """
                {
                    "user_gender": "female",
                    "item_sizes_orig": {
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
                    "product_type": "jacket",
                    "additional_info": {
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
                        "gender": "female",
                        "brand": "Virtusize",
                        "model_info": {
                            "waist": 56,
                            "bust": 78,
                            "size": "38",
                            "hip": 85,
                            "height": 165
                        }
                    },
                    "body_data": {
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
                }
            """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")
        )
    }
}