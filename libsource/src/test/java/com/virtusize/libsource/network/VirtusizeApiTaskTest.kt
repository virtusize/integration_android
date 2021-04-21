package com.virtusize.libsource.network

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.SharedPreferencesHelper
import com.virtusize.libsource.data.parsers.*
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.fixtures.ProductFixtures
import com.virtusize.libsource.fixtures.TestFixtures
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class VirtusizeApiTaskTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var virtusizeApiTask: VirtusizeApiTask

    @Before
    fun setup() {
        virtusizeApiTask = VirtusizeApiTask(
            null,
            SharedPreferencesHelper.getInstance(context),
            null
        )
    }

    @Test
    fun testParseInputStreamStringToUserBodyProfile_return_expectedUserBodyProfile() {
        virtusizeApiTask
            .setJsonParser(UserBodyProfileJsonParser())

        val parseInputStreamStringToObjectMethod = VirtusizeApiTask::class.java.declaredMethods
            .find { it.name == "parseInputStreamStringToObject" }
        parseInputStreamStringToObjectMethod?.let { method ->
            method.isAccessible = true
            val returnValue = method.invoke(
                virtusizeApiTask,
                "",
                TestFixtures.USER_BODY_JSONObject.toString()
            )
            val actualUserBodyProfile = returnValue as? UserBodyProfile
            assertThat(actualUserBodyProfile?.age).isEqualTo(32)
            assertThat(actualUserBodyProfile?.gender).isEqualTo("female")
            assertThat(actualUserBodyProfile?.height).isEqualTo(1630)
            assertThat(actualUserBodyProfile?.weight).isEqualTo("50.00")
            assertThat(actualUserBodyProfile?.bodyData).isEqualTo(
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
    }

    @Test
    fun testParseInputStreamStringToUserBodyProfile_return_null() {
        virtusizeApiTask
            .setJsonParser(UserBodyProfileJsonParser())

        val parseInputStreamStringToObjectMethod = VirtusizeApiTask::class.java.declaredMethods
            .find { it.name == "parseInputStreamStringToObject" }
        parseInputStreamStringToObjectMethod?.let { method ->
            method.isAccessible = true
            val returnValue = method.invoke(
                virtusizeApiTask,
                "",
                "{\"gender\":\"\",\"age\":null,\"height\":null,\"weight\":null,\"braSize\":null,\"concernAreas\":null,\"bodyData\":null}")
            assertThat(returnValue).isNull()
        }
    }

    @Test
    fun testParseErrorStreamStringToProductCheck_return_expectedProductCheck() {
        virtusizeApiTask
            .setJsonParser(ProductCheckJsonParser())

        val parseErrorStreamStringToObjectMethod = VirtusizeApiTask::class.java.declaredMethods
            .find { it.name == "parseErrorStreamStringToObject" }
        parseErrorStreamStringToObjectMethod?.let { method ->
            method.isAccessible = true
            val returnValue = method.invoke(
                virtusizeApiTask,
                """
                    {
                        "data": {
                            "productDataId": null, 
                            "userData": {}, 
                            "storeId": 2, 
                            "storeName": "virtusize", 
                            "validProduct": false, 
                            "fetchMetaData": false
                        }, 
                        "name": "backend-checked-product", 
                        "productId": "123"
                    }
                """
                    .trimIndent()
            )
            val expectedProductCheck = ProductCheck(
                Data(
                    validProduct = false,
                    fetchMetaData = false,
                    shouldSeePhTooltip = false,
                    productDataId = 0,
                    productTypeName = "",
                    storeName = "virtusize",
                    storeId = 2,
                    productTypeId = 0
                ),
                productId = "123",
                name = "backend-checked-product"
            )
            assertThat(returnValue).isEqualTo(expectedProductCheck)
        }
    }

    @Test
    fun testParseErrorStreamStringToUserProduct_return_null() {
        virtusizeApiTask
            .setJsonParser(UserProductJsonParser())

        val parseErrorStreamStringToObjectMethod = VirtusizeApiTask::class.java.declaredMethods
            .find { it.name == "parseErrorStreamStringToObject" }
        parseErrorStreamStringToObjectMethod?.let { method ->
            method.isAccessible = true
            val returnValue = method.invoke(
                virtusizeApiTask,
                "{\"detail\":\"No wardrobe found\"}"
            )
            assertThat(returnValue).isNull()
        }
    }

    @Test
    fun testParseStringToObjectByProductTypeJsonParser_return_ListOfProductType() {
        virtusizeApiTask
            .setJsonParser(ProductTypeJsonParser())

        val parseStringToObjectMethod = VirtusizeApiTask::class.java.declaredMethods
            .find { it.name == "parseStringToObject" }
        parseStringToObjectMethod?.let { method ->
            method.isAccessible = true
            val returnValue = method.invoke(
                virtusizeApiTask,
                "https://staging.virtusize.jp/a/api/v3/product-types",
                ProductFixtures.PRODUCT_TYPE_JSON_ARRAY.toString()
            )
            val productTypes = returnValue as List<ProductType>
            assertThat(productTypes.size).isEqualTo(4)
        }
    }

    @Test
    fun testParseStringToObjectByUserSessionInfoJsonParser_return_expectedUserSessionInfo() {
        virtusizeApiTask
            .setJsonParser(UserSessionInfoJsonParser())

        val parseStringToObjectMethod = VirtusizeApiTask::class.java.declaredMethods
            .find { it.name == "parseStringToObject" }

        parseStringToObjectMethod?.let { method ->
            method.isAccessible = true
            val streamString = """
                    {
                        "id":"test_access_token",
                        "expiresAt":1619062232,
                        "user":{
                            "id":null,
                            "bid":"test_bid",
                            "authType":"EMPTY",
                            "created":null,
                            "lastLogin":null,
                            "firstName":"Anonymous",
                            "language":null
                        },
                        "x-vs-auth":""
                    }
                """.trimIndent().replace("\\s+|[\\n]+".toRegex(), "")

            val returnValue = method.invoke(
                virtusizeApiTask,
                "https://staging.virtusize.jp/a/api/v3/sessions",
                streamString
            )

            val expectedUserSessionInfo = UserSessionInfo(
                accessToken = "test_access_token",
                bid = "test_bid",
                authToken = "",
                userSessionResponse = streamString
            )
            assertThat(returnValue).isEqualTo(expectedUserSessionInfo)
        }
    }



    @Test
    fun testParseStringToObjectByUserBodyProfileJsonParser_return_null() {
        virtusizeApiTask
            .setJsonParser(UserBodyProfileJsonParser())

        val parseStringToObjectMethod = VirtusizeApiTask::class.java.declaredMethods
            .find { it.name == "parseStringToObject" }

        parseStringToObjectMethod?.let { method ->
            method.isAccessible = true
            val returnValue = method.invoke(
                virtusizeApiTask,
                "https://staging.virtusize.jp/a/api/v3/user-body-measurements",
                """
                    {
                        "gender":"",
                        "age":null,
                        "height":null,
                        "weight":null,
                        "braSize":null,
                        "concernAreas":null,
                        "bodyData":null
                    }
                """.trimIndent()
            )

            assertThat(returnValue).isNull()
        }
    }
}