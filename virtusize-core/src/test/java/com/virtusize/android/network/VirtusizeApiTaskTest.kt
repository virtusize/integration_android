package com.virtusize.android.network

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.data.parsers.ProductCheckJsonParser
import com.virtusize.android.data.parsers.ProductTypeJsonParser
import com.virtusize.android.data.parsers.UserBodyProfileJsonParser
import com.virtusize.android.data.parsers.UserProductJsonParser
import com.virtusize.android.data.parsers.UserSessionInfoJsonParser
import com.virtusize.android.data.remote.Data
import com.virtusize.android.data.remote.Measurement
import com.virtusize.android.data.remote.ProductCheck
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.UserBodyProfile
import com.virtusize.android.data.remote.UserSessionInfo
import com.virtusize.android.fixtures.ProductFixtures
import com.virtusize.android.fixtures.TestFixtures
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
internal class VirtusizeApiTaskTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var virtusizeApiTask: VirtusizeApiTask

    @Before
    fun setup() {
        virtusizeApiTask =
            VirtusizeApiTask(
                urlConnection = null,
                sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context),
                messageHandler = null,
            )
    }

    @Test
    fun `test user-body-measurements endpoint with UserBodyProfileJsonParser should return expected UserBodyProfile`() {
        virtusizeApiTask.setJsonParser(UserBodyProfileJsonParser())

        val returnValue =
            virtusizeApiTask.parseInputStreamStringToObject(
                inputStreamString = TestFixtures.USER_BODY_JSONObject.toString(),
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
                Measurement("shoulderHeight", 1240),
            ),
        )
    }

    @Test
    fun testParseInputStreamStringToUserBodyProfile_return_null() {
        virtusizeApiTask.setJsonParser(UserBodyProfileJsonParser())

        val returnValue =
            virtusizeApiTask.parseInputStreamStringToObject(
                inputStreamString =
                    "{\"gender\":\"\",\"age\":null,\"height\":null,\"weight\":null,\"braSize\":null," +
                        "\"concernAreas\":null,\"bodyData\":null}",
            )

        assertThat(returnValue).isNull()
    }

    @Test
    fun `test user-body-measurements endpoint with UserBodyProfileJsonParser returning null`() {
        virtusizeApiTask.setJsonParser(UserBodyProfileJsonParser())

        val returnValue =
            virtusizeApiTask.parseInputStreamStringToObject(
                apiRequestUrl = "https://staging.virtusize.jp/a/api/v3/user-body-measurements",
                inputStreamString =
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
                    """.trimIndent(),
            )

        assertThat(returnValue).isNull()
    }

    @Test
    fun `test product-check endpoint with ProductCheckJsonParser should return expected ProductCheck`() {
        virtusizeApiTask.setJsonParser(ProductCheckJsonParser())

        val pdcJsonString =
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
            """.trimIndent()
        val returnValue =
            virtusizeApiTask.parseErrorStreamStringToObject(errorStreamString = pdcJsonString)

        val expectedProductCheck =
            ProductCheck(
                Data(
                    validProduct = false,
                    fetchMetaData = false,
                    shouldSeePhTooltip = false,
                    productDataId = 0,
                    productTypeName = "",
                    storeName = "virtusize",
                    storeId = 2,
                    productTypeId = 0,
                ),
                productId = "123",
                name = "backend-checked-product",
                JSONObject(pdcJsonString).toString(),
            )

        assertThat(returnValue).isEqualTo(expectedProductCheck)
    }

    @Test
    fun `test user-product endpoint error with UserProductJsonParser`() {
        virtusizeApiTask.setJsonParser(UserProductJsonParser())

        val returnValue =
            virtusizeApiTask.parseErrorStreamStringToObject(
                errorStreamString = "{\"detail\":\"No wardrobe found\"}",
            )

        assertThat(returnValue).isEqualTo("{\"detail\":\"No wardrobe found\"}")
    }

    @Test
    fun `test product-types endpoint with ProductTypeJsonParser should return expected list Of ProductType`() {
        virtusizeApiTask.setJsonParser(ProductTypeJsonParser())

        val returnValue =
            virtusizeApiTask.parseInputStreamStringToObject(
                apiRequestUrl = "https://staging.virtusize.jp/a/api/v3/product-types",
                inputStreamString = ProductFixtures.PRODUCT_TYPE_JSON_ARRAY.toString(),
            )
        val productTypes = returnValue as List<ProductType>

        assertThat(productTypes.size).isEqualTo(4)
    }

    @Test
    fun `test sessions endpoint with UserSessionInfoJsonParser should return expected UserSessionInfo`() {
        virtusizeApiTask.setJsonParser(UserSessionInfoJsonParser())

        val streamString =
            """
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
                """
                .trimIndent()
                .replace(regex = "\\s+|[\\n]+".toRegex(), replacement = "")

        val returnValue =
            virtusizeApiTask.parseInputStreamStringToObject(
                apiRequestUrl = "https://staging.virtusize.jp/a/api/v3/sessions",
                inputStreamString = streamString,
            )

        val expectedUserSessionInfo =
            UserSessionInfo(
                accessToken = "test_access_token",
                bid = "test_bid",
                authToken = "",
                userSessionResponse = streamString,
            )

        assertThat(returnValue).isEqualTo(expectedUserSessionInfo)
    }

    @Test
    fun `test latest version URL with LatestAoyamaVersionJsonParser`() {
        val returnValue =
            virtusizeApiTask.parseInputStreamStringToObject(
                apiRequestUrl = "https://static.api.virtusize.com/a/aoyama/latest.txt",
                inputStreamString = "1.0.0\n",
            )

        assertThat(returnValue).isEqualTo("1.0.0")
    }
}
