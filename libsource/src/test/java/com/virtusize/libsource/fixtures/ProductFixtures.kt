package com.virtusize.libsource.fixtures

import com.virtusize.libsource.data.parsers.ProductTypeJsonParser
import org.json.JSONArray
import org.json.JSONObject

internal object ProductFixtures {

    private val PRODUCT_TYPE_ID_ONE_JSON_OBJECT_STRING =
        """
            {
                "id": 1,
                "name": "dress",
                "weights": {
                  "bust": 1,
                  "waist": 1,
                  "height": 0.25
                },
                "compatibleWith": [
                  1,
                  16
                ]
            }
        """.trimIndent()

    private val PRODUCT_TYPE_ID_TWO_JSON_OBJECT_STRING =
        """
            {
                "id": 2,
                "name": "shirt",
                "weights": {
                    "bust": 2,
                    "height": 0.5,
                    "sleeve": 1
                },
                "compatibleWith":[
                    2
                ]
            }
        """.trimIndent()

    private val PRODUCT_TYPE_ID_EIGHT_JSON_OBJECT_STRING =
        """
            {
                "id": 8,
                "name": "jacket",
                "weights": {
                  "bust": 2,
                  "height": 1,
                  "sleeve": 1
                },
                "compatibleWith": [
                  8,
                  14
                ]
            }
        """.trimIndent()

    private val PRODUCT_TYPE_ID_EIGHTEEN_JSON_OBJECT_STRING =
        """
            {
                "id":18,
                "name": "bag",
                "weights":{
                    "depth":1,
                    "width":2,
                    "height":1
                },
                "compatibleWith":[
                    18,
                    19,
                    25,
                    26
                ]
            }
        """.trimIndent()

    val PRODUCT_TYPE_JSON_OBJECT = JSONObject(PRODUCT_TYPE_ID_ONE_JSON_OBJECT_STRING)

    val PRODUCT_TYPE_JSON_ARRAY = JSONArray(
        """
            [
                $PRODUCT_TYPE_ID_ONE_JSON_OBJECT_STRING,
                $PRODUCT_TYPE_ID_TWO_JSON_OBJECT_STRING,
                $PRODUCT_TYPE_ID_EIGHT_JSON_OBJECT_STRING,
                $PRODUCT_TYPE_ID_EIGHTEEN_JSON_OBJECT_STRING
            ]
        """.trimIndent()
    )

    fun productTypes() = run {
        val productTypes: MutableList<ProductType> = mutableListOf()
        for (i in 0 until PRODUCT_TYPE_JSON_ARRAY.length()) {
            ProductTypeJsonParser().parse(PRODUCT_TYPE_JSON_ARRAY[i] as JSONObject)?.let {
                productTypes.add(it)
            }
        }
        productTypes
    }

    fun storeProduct(
        productType: Int = 8,
        sizeList: List<ProductSize> = mutableListOf(
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
        ),
        brand: String = "Virtusize",
        modelInfo: Map<String, Any>? = mutableMapOf(
            "hip" to 85,
            "size" to "38",
            "waist" to 56,
            "bust" to 78,
            "height" to 165
        ),
        gender: String? = "female"
    ): Product {
        return Product(
            7110384,
            sizeList,
            "694",
            productType,
            "Test Product Name",
            "Test Cloudinary Public Id",
            null,
            2,
            StoreProductMeta(
                1,
                StoreProductAdditionalInfo(
                    brand,
                    gender,
                    sizeList.toMutableSet(),
                    modelInfo,
                    "regular",
                    "fashionable",
                    BrandSizing("large", false)
                ),
                brand,
                gender
            )
        )
    }

    val STORE_PRODUCT_INFO_JSON_DATA = JSONObject(
        """
            {
                "id":${TestFixtures.PRODUCT_ID},
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
                "externalId":"${TestFixtures.EXTERNAL_ID}",
                "productType":8,
                "name":"${TestFixtures.PRODUCT_NAME}",
                "cloudinaryPublicId":"Test Cloudinary Public Id",
                "store":${TestFixtures.STORE_ID},
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
                    "storeProduct":${TestFixtures.PRODUCT_ID}
                }
            }
        """.trimIndent()
    )

    private val USER_PRODUCT_ONE_JSON_STRING =
        """
            {
                "id": 123456,
                "sizes": [
                  {
                    "name": "S",
                    "measurements": {
                      "height": 1000,
                      "bust": 400,
                      "waist": 340,
                      "hip": null,
                      "hem": null,
                      "waistHeight": null
                    }
                  }
                ],
                "productType": 11,
                "created": "2020-09-14T11:06:00Z",
                "updated": "2020-09-14T11:06:00Z",
                "name": "Test Womenswear Strapless Dress",
                "cloudinaryPublicId": null,
                "deleted": false,
                "isFavorite": false,
                "wardrobe": 123,
                "orderItem": null,
                "store": null
            }
        """.trimIndent()

    private val USER_PRODUCT_TWO_JSON_STRING =
        """
            {
                "id": 654321,
                "sizes": [
                    {
                        "name": null,
                        "measurements": {
                            "height": 820,
                            "bust": 520,
                            "sleeve": 930,
                            "collar": null,
                            "shoulder": null,
                            "waist": null,
                            "hem": null,
                            "bicep": null
                        }
                    }
                ],
                "productType": 2,
                "created": "2020-07-22T10:22:19Z",
                "updated": "2020-07-22T10:22:19Z",
                "name": "test2",
                "cloudinaryPublicId": null,
                "deleted": false,
                "isFavorite": true,
                "wardrobe": 123,
                "orderItem": null,
                "store": 2
            }
        """.trimIndent()

    val USER_PRODUCT_ONE_JSON_OBJECT = JSONObject(USER_PRODUCT_ONE_JSON_STRING)

    val USER_PRODUCT_JSON_ARRAY = JSONArray(
        """
            [
                $USER_PRODUCT_ONE_JSON_STRING,
                $USER_PRODUCT_TWO_JSON_STRING
            ]
        """.trimIndent()
    )

    val EMPTY_PRODUCT_JSON_ARRAY = JSONArray("[]")

    val WARDROBE_NOT_FOUND_ERROR_JSONObject = JSONObject("{\"detail\": \"No wardrobe found\"}")
}
