package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.fixtures.TestFixtures
import org.json.JSONObject
import org.junit.Test

class StoreProductMetaJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedStoreProductMeta() {
        val actualStoreProductMeta = StoreProductMetaJsonParser().parse(STORE_PRODUCT_META_JSON_DATA)
        assertThat(actualStoreProductMeta?.id).isEqualTo(123)
        assertThat(actualStoreProductMeta?.brand).isEqualTo("Virtusize")
        assertThat(actualStoreProductMeta?.additionalInfo?.brand).isEqualTo("Virtusize")
        assertThat(actualStoreProductMeta?.additionalInfo?.sizes?.toMutableSet()).isEqualTo(
            mutableSetOf(
                ProductSize(
                    "38",
                    mutableSetOf(
                        Measurement("height", 760),
                        Measurement("bust", 660),
                        Measurement("sleeve", 845)
                    )
                ),
                ProductSize(
                    "36",
                    mutableSetOf(
                        Measurement("height", 750),
                        Measurement("bust", 645),
                        Measurement("sleeve", 825)
                    )
                )
            )
        )
        assertThat(actualStoreProductMeta?.additionalInfo?.modelInfo).isEqualTo(
            mutableMapOf(
                "hip" to 85,
                "size" to "38",
                "waist" to 56,
                "bust" to 78,
                "height" to 165
            )
        )
        assertThat(actualStoreProductMeta?.additionalInfo?.fit).isEqualTo("loose")
        assertThat(actualStoreProductMeta?.additionalInfo?.style).isEqualTo("fashionable")
        assertThat(actualStoreProductMeta?.additionalInfo?.brandSizing).isEqualTo(BrandSizing("small", true))
    }

    @Test
    fun parse_emptyStoreProductData_shouldReturnNull() {
        val actualStoreProductMeta = StoreProductMetaJsonParser().parse(TestFixtures.EMPTY_JSON_DATA)

        assertThat(actualStoreProductMeta).isNull()
    }

    companion object {
        private val STORE_PRODUCT_META_JSON_DATA = JSONObject(
            """
                {
                    "id":123,
                    "modelInfo":null,
                    "materials":{
                        "main":{
                            "cotton":1.0
                        }
                    },
                    "matAnalysis":null,
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
                        "type":"loose",
                        "style":"fashionable",
                        "fit":"loose",
                        "brandSizing":{
                            "compare":"small",
                            "itemBrand":true
                        }
                    },
                    "price":{},
                    "salePrice":{},
                    "availableSizes":[],
                    "attributes":{
                        "defaults":false,
                        "topsFit":"loose"
                    },
                    "created":"2020-04-24T22:17:13Z",
                    "updated":"2020-05-30T21:20:39Z",
                    "brand":"Virtusize",
                    "active":true,
                    "url":"",
                    "gender":"female",
                    "color":null,
                    "style":null,
                    "storeProduct":12345
                }
            """.trimIndent()
        )
    }
}
