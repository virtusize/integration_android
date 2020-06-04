package com.virtusize.libsource.data

import org.json.JSONObject

internal object JsonResources {

    val PRODUCT_DATA_CHECK_JSON = JSONObject(
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

    val PRODUCT_DATA_CHECK_DATA_JSON = JSONObject(
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
}