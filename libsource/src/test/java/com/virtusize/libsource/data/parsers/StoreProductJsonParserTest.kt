package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.TestFixtures
import com.virtusize.libsource.data.remote.*
import org.json.JSONObject
import org.junit.Test

class StoreProductJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualStoreProduct = StoreProductJsonParser().parse(STORE_PRODUCT_INFO_JSON_DATA)

        val expectedStoreProduct = StoreProduct(
            7110384,
            mutableListOf(
                ProductSize("38",
                    mutableSetOf(
                        Measurement("height", 760),
                        Measurement("bust", 660),
                        Measurement("sleeve", 845)
                    )
                ),
                ProductSize("36",
                    mutableSetOf(
                        Measurement("height", 750),
                        Measurement("bust", 645),
                        Measurement("sleeve", 825)
                    )
                )
            ),
            "694",
            8,
            "Test Product Name",
            2,
            StoreProductMeta(
                1,
                StoreProductAdditionalInfo(
                "regular",
                    BrandSizing(
                        "large",
                        false
                    )
                )
            )
        )

        assertThat(actualStoreProduct).isEqualTo(expectedStoreProduct)
    }

    @Test
    fun parse_emptyStoreProductData_shouldReturnNull() {
        val actualStoreProduct = StoreProductJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualStoreProduct).isNull()
    }

    companion object {
        private val STORE_PRODUCT_INFO_JSON_DATA = JSONObject(
            """
            {
                "id":7110384,
                "sizes":[
                    {
                        "name":"38",
                        "measurements":{
                            "height":760,
                            "bust":660,
                            "sleeve":845
                        }
                    },
                    {
                        "name":"36",
                        "measurements":{
                            "height":750,
                            "bust":645,
                            "sleeve":825
                        }
                    }
                ],
                "isSgi":false,
                "created":"2020-01-29T09:48:55Z",
                "updated":"2020-01-29T09:52:01Z",
                "deleted":null,
                "externalId":"694",
                "productType":8,
                "name":"Test Product Name",
                "cloudinaryPublicId":"Test Cloudinary Public Id",
                "store":2,
                "storeProductMeta":{
                    "id":1,
                    "modelInfo":{
                        "hip":85,
                        "size":"38",
                        "waist":56,
                        "bust":78,
                        "height":165
                    },
                    "materials":{
                        "main":{
                            "polyamide":1.0
                        },
                        "fill":{
                            "feather":0.1,
                            "down":0.9
                        },
                        "sleeve lining":{}
                    },
                    "matAnalysis":{
                        "lining":{
                            "none":true,
                            "some":false,
                            "present":false
                        },
                        "elasticity":{
                            "none":false,
                            "some":true,
                            "present":false
                        },
                        "transparency":{
                            "none":false,
                            "some":true,
                            "present":false
                        },"thickness":{
                            "heavy":true,
                            "light":false,
                            "normal":false
                        }
                    },
                    "additionalInfo":{
                        "brand":"Virtusize",
                        "gender":"female",
                        "sizes":{
                            "38":{
                                "height":760,
                                "bust":660,
                                "sleeve":845
                            },
                            "36":{
                                "height":750,
                                "bust":645,
                                "sleeve":825
                            }
                        },
                        "modelInfo":{
                            "hip":85,
                            "size":"38",
                            "waist":56,
                            "bust":78,
                            "height":165
                        },
                        "type":"regular",
                        "style":"fashionable",
                        "fit":"regular",
                        "brandSizing":{
                            "compare":"large",
                            "itemBrand":false
                        }
                    },
                    "price":{
                        "eur":230
                    },
                    "salePrice":{
                        "eur":60
                    },
                    "availableSizes":[
                        "36",
                        "38"
                    ],
                    "attributes":{
                        "defaults":false,
                        "jacket_style":"fashionable",
                        "tops_fit":"regular"
                    },
                    "created":"2020-01-29T09:48:57Z",
                    "updated":"2020-01-29T09:48:57Z",
                    "brand":"Virtusize",
                    "active":true,
                    "url":"https://www.virtusize.co.jp/shop/goods.html?id=694",
                    "gender":"female",
                    "color":null,
                    "style":null,
                    "storeProduct":7110384
                }
            }
        """.trimIndent()
        )
    }
}