package com.virtusize.android.fixtures

import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeOrderItem
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.parsers.ProductCheckJsonParser
import com.virtusize.android.data.remote.Measurement
import com.virtusize.android.data.remote.UserBodyProfile
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

    val VIRTUSIZE_ORDER = VirtusizeOrder(
        "888400111032",
        mutableListOf(
            VirtusizeOrderItem(
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
            )
        )
    )

    val USER_BODY_JSONObject = JSONObject(
        """
                {
                  "wardrobe": "1234567",
                  "gender": "female",
                  "age": 32,
                  "height": 1630,
                  "weight": "50.00",
                  "braSize": {}, 
                  "concernAreas": {},
                  "bodyData": {
                    "hip": 830,
                    "bust": 755,
                    "neck": 300,
                    "rise": 215,
                    "bicep": 220,
                    "thigh": 480,
                    "waist": 630,
                    "inseam": 700,
                    "sleeve": 720,
                    "shoulder": 370,
                    "hipWidth": 300,
                    "bustWidth": 245,
                    "hipHeight": 750,
                    "headHeight": 215,
                    "kneeHeight": 395,
                    "waistWidth": 225,
                    "waistHeight": 920,
                    "armpitHeight": 1130,
                    "sleeveLength": 520,
                    "shoulderWidth": 340,
                    "shoulderHeight": 1240
                  }
                }
        """.trimIndent()
    )

    val NULL_USER_BODY_PROFILE = JSONObject(
        """
                {   
                    "gender": "",
                    "age": null,
                    "height": null,
                    "weight": null,
                    "braSize": null,
                    "concernAreas": null,
                    "bodyData": null
                }
        """.trimIndent()
    )

    val userBodyProfile = UserBodyProfile(
        "female",
        32,
        1630,
        "50.00",
        mutableSetOf(
            Measurement("hip", 830),
            Measurement("hip", 830),
            Measurement("bust", 755),
            Measurement("neck", 300),
            Measurement("rise", 215),
            Measurement("bicep", 220),
            Measurement("thigh", 480),
            Measurement("waist", 630),
            Measurement("inseam", 700),
            Measurement("sleeve", 720),
            Measurement("shoulder", 370),
            Measurement("hipWidth", 300),
            Measurement("bustWidth", 245),
            Measurement("hipHeight", 750),
            Measurement("headHeight", 215),
            Measurement("kneeHeight", 395),
            Measurement("waistWidth", 225),
            Measurement("waistHeight", 920),
            Measurement("armpitHeight", 1130),
            Measurement("sleeveLength", 520),
            Measurement("shoulderWidth", 340),
            Measurement("shoulderHeight", 1240)
        )
    )
}
