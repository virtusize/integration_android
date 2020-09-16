package com.virtusize.libsource

import com.virtusize.libsource.data.local.VirtusizeOrder
import com.virtusize.libsource.data.local.VirtusizeOrderItem
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.data.parsers.ProductCheckJsonParser
import com.virtusize.libsource.data.remote.*
import org.json.JSONArray
import org.json.JSONObject

internal object TestFixtures {

    const val API_KEY = "test_apiKey"
    const val USER_ID = "123"
    const val STORE_ID = 2
    const val EXTERNAL_ID = "694"
    const val PRODUCT_ID = 7110384
    const val PRODUCT_NAME = "Test Product Name"
    const val ORIENTATION = "orientation"

    val EMPTY_JSON_DATA = JSONObject("{}")

    val PRODUCT_DATA_CHECK = JSONObject(
        """
            {
                "data":{
                    "productTypeName": "pants",
                    "storeName": "virtusize",
                    "storeId": 2, 
                    "validProduct": true,
                    "fetchMetaData": false,
                    "productDataId": 7110384,
                    "productTypeId": 5,
                    "userData": {
                            "should_see_ph_tooltip": true
                        }
                }, 
                "name": "backend-checked-product", 
                "productId": "$EXTERNAL_ID"
            }
        """.trimIndent()
    )

    val INVALID_PRODUCT_DATA_CHECK = JSONObject(
        """
            {
                "data": {
                    "productDataId": null,
                    "userData": {},
                    "storeId": 2,
                    "storeName": "virtusize",
                    "validProduct": false
                },
                "name": "backend-checked-product",
                "productId": "63434343"
            }
        """.trimIndent()
    )

    val PRODUCT_CHECK = ProductCheckJsonParser().parse(PRODUCT_DATA_CHECK)

    val VIRTUSIZE_PRODUCT = VirtusizeProduct(EXTERNAL_ID, "http://image.com/xxx.jpg", PRODUCT_CHECK)

    val PRODUCT_DATA_CHECK_DATA = JSONObject(
        """
            {
                "productTypeName": "pants",
                "storeName": "virtusize",
                "storeId": 2, 
                "validProduct": true,
                "fetchMetaData": false,
                "productDataId": 7110384,
                "productTypeId": 5,
                "userData": {
                        "should_see_ph_tooltip": false
                }
            }
        """.trimIndent()
    )

    val PRODUCT_META_DATA_HINTS = JSONObject(
        """
            {
                "apiKey": "test_apiKey",
                "imageUrl": "http://www.test.com/goods/31/12/11/71/1234_COL_COL02_570.jpg",
                "cloudinaryPublicId": "test_cloudinaryPublicId",
                "externalProductId": "$EXTERNAL_ID"
            }
        """.trimIndent()
    )

    val USER_SAW_PRODUCT_EVENT_RESPONSE = JSONObject(
        """
            {
                "apiKey": "$API_KEY",
                "name": "user-saw-product",
                "type": "user",
                "source": "integration-android",
                "userCohort": "direct",
                "widgetType": "mobile",
                "browserOrientation": "landscape",
                "browserResolution": "794x1080",
                "integrationVersion": "1",
                "snippetVersion": "1",
                "browserIp": "testBrowserIp",
                "browserIpCountry": "JP",
                "browserIpCity": "Setagaya-ku",
                "ruuid": "testRuuid",
                "browserId": "testBrowserId",
                "browserName": "other",
                "browserVersion": "",
                "browserPlatform": "other",
                "browserDevice": "other",
                "browserIsMobile": true,
                "browserIsTablet": true,
                "browserIsPc": false,
                "browserIsBot": false,
                "browserHasTouch": true,
                "browserLanguage": "en",
                "@timestamp": "2020-06-16T13:06:24.842988Z"
            }
        """.trimIndent()
    )

    val STORE_WITH_FULL_INFO = JSONObject(
        """
            {
                "id": 2,
                "surveyLink": "https://www.survey.com/s/xxxxxx",
                "name": "Virtusize",
                "shortName": "virtusize",
                "lengthUnitId":2, 
                "apiKey": "$API_KEY",
                "created": "2011-01-01T00:00:00Z",
                "updated": "2020-04-20T02:33:58Z",
                "disabled": "2018-05-29 04:32:45",
                "typemapperEnabled": false,
                "region": "KR"
            }
      """.trimIndent()
    )

    val STORE_WITH_NULL_VALUES = JSONObject(
        """
            {
                "id": 2,
                "surveyLink": "https://www.survey.com/s/xxxxxx",
                "name": "Virtusize",
                "shortName": "virtusize",
                "lengthUnitId":2, 
                "apiKey": "test_apiKey",
                "created": "2011-01-01T00:00:00Z",
                "updated": "2020-04-20T02:33:58Z",
                "disabled": null,
                "typemapperEnabled": false,
                "region": null
            }
      """.trimIndent()
    )

    val VIRTUSIZE_ORDER = VirtusizeOrder("888400111032", mutableListOf(VirtusizeOrderItem(
        "P001",
        "L",
        "Large",
        "P001_SIZEL_RED",
        "http://images.example.com/products/P001/red/image1xl.jpg",
        "Red",
        "W",
        5100.00,
        "JPY",
        1,
        "http://example.com/products/P001"
    )))

    fun storeProduct(
        productType: Int = 8,
        sizes: List<ProductSize> = mutableListOf(
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
    ) : Product {
        return Product(
            7110384,
            sizes,
            "694",
            productType,
            "Test Product Name",
            "Test Cloudinary Public Id",
            null,
            2,
            StoreProductMeta(
                1,
                StoreProductAdditionalInfo(
                    "regular",
                    "fashionable",
                    BrandSizing("large", false)
                )
            )
        )
    }

    fun oneSizeProduct(productType: Int = 8): Product {
        return storeProduct(
            productType = productType,
            sizes = mutableListOf(
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
    }

    val STORE_PRODUCT_INFO_JSON_DATA = JSONObject(
        """
            {
                "id":$PRODUCT_ID,
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
                "externalId":"$EXTERNAL_ID",
                "productType":8,
                "name":"$PRODUCT_NAME",
                "cloudinaryPublicId":"Test Cloudinary Public Id",
                "store":$STORE_ID,
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
                    "storeProduct":$PRODUCT_ID
                }
            }
        """.trimIndent()
    )

    private val PRODUCT_TYPE_ID_ONE_JSON_OBJECT_STRING =
        """
            {
                "id": 1,
                "name": "dress",
                "optionalMeasurements": [
                  "hip",
                  "sleeveOpening",
                  "hem",
                  "waistHeight"
                ],
                "priority": [
                  "bust",
                  "waist",
                  "height"
                ],
                "requiredMeasurements": [
                  "height",
                  "bust",
                  "waist"
                ],
                "supportsLengthComparison": true,
                "weights": {
                  "bust": 1,
                  "waist": 1,
                  "height": 0.25
                },
                "anchorPoint": "shoulders",
                "compatibleWith": [
                  1,
                  16
                ],
                "defaultMeasurements": {
                  "hem": 470,
                  "hip": 440,
                  "bust": 430,
                  "waist": 395,
                  "height": 900,
                  "waistHeight": 410,
                  "sleeveOpening": 200
                },
                "displayMode": "portrait",
                "isDraggable": false,
                "isReserved": false,
                "maxMeasurements": {
                  "hem": 2000,
                  "hip": 1500,
                  "bust": 1200,
                  "waist": 1200,
                  "height": 2500,
                  "waistHeight": 750,
                  "sleeveOpening": 400
                },
                "minMeasurements": {
                  "hem": 200,
                  "hip": 150,
                  "bust": 150,
                  "waist": 100,
                  "height": 500,
                  "waistHeight": 150,
                  "sleeveOpening": 50
                },
                "sgiGenders": [
                  "female"
                ],
                "sgiStyles": [
                  "regular"
                ],
                "sgiTypes": [
                  "medium",
                  "long",
                  "short"
                ]
            }
        """.trimIndent()

    private val PRODUCT_TYPE_ID_EIGHTEEN_JSON_OBJECT_STRING =
        """
            {
                "id":18,
                "name":"bag",
                "optionalMeasurements":[
                    "topWidth",
                    "handleDrop",
                    "handleWidth"
                ],
                "priority":[
                    "width",
                    "height",
                    "depth"
                ],
                "requiredMeasurements":[
                    "height",
                    "width",
                    "depth"
                ],
                "supportsLengthComparison":true,
                "weights":{
                    "depth":1,
                    "width":2,
                    "height":1
                },
                "anchorPoint":"shoulders",
                "compatibleWith":[
                    18,
                    19,
                    25,
                    26
                ],
                "defaultMeasurements":{
                    "depth":70,
                    "width":340,
                    "height":190,
                    "topWidth":300,
                    "handleDrop":100,
                    "handleWidth":140
                },
                "displayMode":"landscape",
                "isDraggable":true,
                "isReserved":false,
                "maxMeasurements":{
                    "depth":1000,
                    "width":1000,
                    "height":1000,
                    "topWidth":1000,
                    "handleDrop":1000,
                    "handleWidth":1000
                },
                "minMeasurements":{
                    "depth":10,
                    "width":50,
                    "height":50,
                    "topWidth":50,
                    "handleDrop":10,
                    "handleWidth":10
                },
                "sgiGenders":[
                    "male",
                    "female",
                    "unisex"
                ],
                "sgiStyles":[
            
                ],
                "sgiTypes":[
            
                ]
            }
        """.trimIndent()

    val PRODUCT_TYPE_JSON_OBJECT = JSONObject(PRODUCT_TYPE_ID_ONE_JSON_OBJECT_STRING)

    val PRODUCT_TYPE_JSON_ARRAY = JSONArray(
        """
            [
                $PRODUCT_TYPE_ID_ONE_JSON_OBJECT_STRING,
                $PRODUCT_TYPE_ID_EIGHTEEN_JSON_OBJECT_STRING
            ]
        """.trimIndent()
    )

    val USER_PRODUCT_ONE_JSON_STRING =
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

    val USER_PRODUCT_TWO_JSON_STRING =
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

    val USER_PRODUCT_ONE_JSON_OBJECT  = JSONObject(USER_PRODUCT_ONE_JSON_STRING)

    val USER_PRODUCT_JSON_ARRAY = JSONArray(
        """
            [
                $USER_PRODUCT_ONE_JSON_STRING,
                $USER_PRODUCT_TWO_JSON_STRING
            ]
        """.trimIndent()
    )

    val WARDROBE_NOT_FOUND_ERROR_JSONObject = JSONObject("{\"detail\": \"No wardrobe found\"}")

}