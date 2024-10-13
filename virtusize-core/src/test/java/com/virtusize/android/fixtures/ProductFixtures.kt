package com.virtusize.android.fixtures

import com.virtusize.android.data.parsers.ProductTypeJsonParser
import com.virtusize.android.data.remote.BrandSizing
import com.virtusize.android.data.remote.Measurement
import com.virtusize.android.data.remote.Product
import com.virtusize.android.data.remote.ProductSize
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.StoreProductAdditionalInfo
import com.virtusize.android.data.remote.StoreProductMeta
import org.json.JSONArray
import org.json.JSONObject

internal object ProductFixtures {
    val PRODUCT_TYPE_ID_ONE_JSON_OBJECT_STRING =
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

    val PRODUCT_TYPE_JSON_ARRAY =
        JSONArray(
            """
            [
                $PRODUCT_TYPE_ID_ONE_JSON_OBJECT_STRING,
                $PRODUCT_TYPE_ID_TWO_JSON_OBJECT_STRING,
                $PRODUCT_TYPE_ID_EIGHT_JSON_OBJECT_STRING,
                $PRODUCT_TYPE_ID_EIGHTEEN_JSON_OBJECT_STRING
            ]
            """.trimIndent(),
        )

    fun productTypes() =
        run {
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
        sizeList: List<ProductSize> =
            mutableListOf(
                ProductSize(
                    "38",
                    mutableSetOf(
                        Measurement("height", 760),
                        Measurement("bust", 660),
                        Measurement("sleeve", 845),
                    ),
                ),
                ProductSize(
                    "36",
                    mutableSetOf(
                        Measurement("height", 750),
                        Measurement("bust", 645),
                        Measurement("sleeve", 825),
                    ),
                ),
            ),
        brand: String = "Virtusize",
        modelInfo: Map<String, Any>? =
            mutableMapOf(
                "hip" to 85,
                "size" to "38",
                "waist" to 56,
                "bust" to 78,
                "height" to 165,
            ),
        gender: String? = "female",
    ): Product =
        Product(
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
                    BrandSizing("large", false),
                ),
                brand,
                gender,
            ),
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

    val USER_PRODUCT_ONE_JSON_OBJECT = JSONObject(USER_PRODUCT_ONE_JSON_STRING)
}
