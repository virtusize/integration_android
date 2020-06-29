package com.virtusize.libsource

import com.virtusize.libsource.data.local.VirtusizeOrder
import com.virtusize.libsource.data.local.VirtusizeOrderItem
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.data.remote.parsers.ProductCheckJsonParser
import org.json.JSONObject

internal object TestFixtures {

    const val API_KEY = "test_apiKey"
    const val BROWSER_ID = "browserID"
    const val USER_ID = "123"
    const val EXTERNAL_ID = "7110384"
    const val LANGUAGE = "en"
    const val ORIENTATION = "orientation"

    val USER_DATA_ONLY_WITH_ONE_INFO = JSONObject(
        """
            {
               "should_see_ph_tooltip": false
            }
        """.trimIndent()
    )

    val USER_DATA_WITH_FULL_INFO = JSONObject(
        """
            {
               "should_see_ph_tooltip": false,
                "wardrobeActive": false,
                "wardrobeHasM": true,
                "wardrobeHasP": true,
                "wardrobeHasR": true
            }
        """.trimIndent()
    )

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
                            "should_see_ph_tooltip": false
                        }
                }, 
                "name": "backend-checked-product", 
                "productId": "694"
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
                "externalProductId": "694"
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
    )
    ))
}