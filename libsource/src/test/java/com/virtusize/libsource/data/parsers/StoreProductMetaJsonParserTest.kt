package com.virtusize.libsource.data.parsers

import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.fixtures.TestFixtures
import com.virtusize.libsource.data.remote.BrandSizing
import com.virtusize.libsource.data.remote.StoreProductAdditionalInfo
import com.virtusize.libsource.data.remote.StoreProductMeta
import org.json.JSONObject
import org.junit.Test

class StoreProductMetaJsonParserTest {

    @Test
    fun parse_validJsonData_shouldReturnExpectedObject() {
        val actualStoreProductMeta = StoreProductMetaJsonParser().parse(STORE_PRODUCT_META_JSON_DATA)

        val expectedStoreProductMeta = StoreProductMeta(
            123,
            StoreProductAdditionalInfo("loose", "fashionable", BrandSizing("small", true))
        )

        assertThat(actualStoreProductMeta).isEqualTo(expectedStoreProductMeta)
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