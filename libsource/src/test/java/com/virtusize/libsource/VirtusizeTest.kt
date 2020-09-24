package com.virtusize.libsource

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.*
import com.virtusize.libsource.fixtures.ProductFixtures
import com.virtusize.libsource.fixtures.TestFixtures
import com.virtusize.libsource.network.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class VirtusizeTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var virtusize: Virtusize
    private var actualError: VirtusizeError? = null

    private var mockURL: URL = mock(URL::class.java)
    private var mockApiRequest: ApiRequest = mock(ApiRequest::class.java)

    @Before
    fun setup() {
        virtusize = VirtusizeBuilder().init(context)
            .setApiKey(TestFixtures.API_KEY)
            .setUserId(TestFixtures.USER_ID)
            .setEnv(VirtusizeEnvironment.STAGING)
            .build()

        virtusize.setCoroutineDispatcher(testDispatcher)

        actualError = null
    }

    @Test
    fun testProductDataCheck_isValidProduct_hasExpectedData() = runBlocking {
        var actualProductCheck: ProductCheck? = null

        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, TestFixtures.PRODUCT_DATA_CHECK.toString().byteInputStream())
        ))
        virtusize.productDataCheck(object : ValidProductCheckHandler {
            override fun onValidProductCheckCompleted(productCheck: ProductCheck) {
                actualProductCheck = productCheck
            }
        }, object : ErrorResponseHandler {
            override fun onError(error: VirtusizeError) {}
        }, mockApiRequest)

        assertThat(actualProductCheck?.name).isEqualTo("backend-checked-product")
        assertThat(actualProductCheck?.data?.productTypeName).isEqualTo("pants")
        assertThat(actualProductCheck?.data?.storeName).isEqualTo("virtusize")
        assertThat(actualProductCheck?.data?.storeId).isEqualTo(2)
        assertThat(actualProductCheck?.data?.validProduct).isTrue()
        assertThat(actualProductCheck?.data?.fetchMetaData).isFalse()
        assertThat(actualProductCheck?.data?.productDataId).isEqualTo(7110384)
        assertThat(actualProductCheck?.data?.productTypeId).isEqualTo(5)
        assertThat(actualProductCheck?.data?.shouldSeePhTooltip).isTrue()
        assertThat(actualProductCheck?.productId).isEqualTo("694")
    }

    @Test
    fun testProductDataCheck_isInvalidProduct() = runBlocking {
        var actualProductCheck: ProductCheck? = null

        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, TestFixtures.INVALID_PRODUCT_DATA_CHECK.toString().byteInputStream())
        ))
        virtusize.productDataCheck(object : ValidProductCheckHandler {
            override fun onValidProductCheckCompleted(productCheck: ProductCheck) {
                actualProductCheck = productCheck
            }
        }, object : ErrorResponseHandler {
            override fun onError(error: VirtusizeError) {}
        }, mockApiRequest)

        assertThat(actualProductCheck?.name).isEqualTo("backend-checked-product")
        assertThat(actualProductCheck?.data?.validProduct).isFalse()
        assertThat(actualProductCheck?.data?.productDataId).isEqualTo(0)
    }

    @Test
    fun testProductDataCheck_provideWrongAPIKey_hasNetworkError() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(HttpURLConnection.HTTP_FORBIDDEN, "{ \"Code\": \"ForbiddenError\", \"Message\": \"ForbiddenError: \" }".byteInputStream())
        ))
        virtusize.productDataCheck(object : ValidProductCheckHandler {
            override fun onValidProductCheckCompleted(productCheck: ProductCheck) {}
        }, object : ErrorResponseHandler {
            override fun onError(error: VirtusizeError) {
                actualError = error
            }
        }, mockApiRequest)

        assertThat(actualError?.code).isEqualTo(HttpURLConnection.HTTP_FORBIDDEN)
        assertThat(actualError?.message).isEqualTo(VirtusizeErrorType.ApiKeyNullOrInvalid.message())
        assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.ApiKeyNullOrInvalid)
    }

    @Test
    fun testSendProductImageToBackend_whenSuccessful_hasExpectedProductMetaDataHints() = runBlocking {
        var actualProductMetaDataHints: ProductMetaDataHints? = null

        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, TestFixtures.PRODUCT_META_DATA_HINTS.toString().byteInputStream())
        ))
        virtusize.sendProductImageToBackend(TestFixtures.VIRTUSIZE_PRODUCT, object: SuccessResponseHandler {
            override fun onSuccess(data: Any?) {
                actualProductMetaDataHints = data as ProductMetaDataHints
            }
        }, object: ErrorResponseHandler{
            override fun onError(error: VirtusizeError) {}
        })

        assertThat(actualProductMetaDataHints?.apiKey).isEqualTo(TestFixtures.API_KEY)
        assertThat(actualProductMetaDataHints?.imageUrl).isEqualTo("http://www.test.com/goods/31/12/11/71/1234_COL_COL02_570.jpg")
        assertThat(actualProductMetaDataHints?.cloudinaryPublicId).isEqualTo("test_cloudinaryPublicId")
        assertThat(actualProductMetaDataHints?.externalProductId).isEqualTo("694")
    }

    @Test
    fun testSendProductImageToBackend_whenFailed_hasExpectedErrorInfo() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(500, INTERNAL_SERVER_ERROR_RESPONSE.byteInputStream(), null)
        )
        )
        virtusize.sendProductImageToBackend(TestFixtures.VIRTUSIZE_PRODUCT, object: SuccessResponseHandler {
            override fun onSuccess(data: Any?) {}
        }, object: ErrorResponseHandler{
            override fun onError(error: VirtusizeError) {
                actualError = error
            }
        })

        assertThat(actualError?.code).isEqualTo(500)
        assertThat(actualError?.message).contains(INTERNAL_SERVER_ERROR_RESPONSE)
        assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.NetworkError)
    }

    @Test
    fun testSendUserSawProductEvent_whenSuccessful_onSuccessShouldBeCalled() = runBlocking {
        var isSuccessful = false

        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(201,
                TestFixtures.USER_SAW_PRODUCT_EVENT_RESPONSE.toString().byteInputStream())
        ))
        virtusize.sendEventToApi(
            VirtusizeEvent("user-saw-product", null),
            null, object: SuccessResponseHandler {
                override fun onSuccess(data: Any?) {
                    isSuccessful = true
                }
            }, object : ErrorResponseHandler {
                    override fun onError(error: VirtusizeError) {}
            }
        )

        assertThat(isSuccessful).isTrue()
    }

    @Test
    fun testRetrieveStoreInfo_getsFullStoreInfoResponse_hasExpectedStore() = runBlocking {
        var actualStore: Store? = null

        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200,
                TestFixtures.STORE_WITH_FULL_INFO.toString().byteInputStream())
        ))
        virtusize.getStoreInfo(
            object: SuccessResponseHandler {
                override fun onSuccess(data: Any?) {
                    actualStore = data as Store
                }
            }, object : ErrorResponseHandler {
                override fun onError(error: VirtusizeError) {}
            }
        )

        assertThat(actualStore?.id).isEqualTo(2)
        assertThat(actualStore?.surveyLink).isEqualTo("https://www.survey.com/s/xxxxxx")
        assertThat(actualStore?.name).isEqualTo("Virtusize")
        assertThat(actualStore?.shortName).isEqualTo("virtusize")
        assertThat(actualStore?.lengthUnitId).isEqualTo(2)
        assertThat(actualStore?.apiKey).isEqualTo(TestFixtures.API_KEY)
        assertThat(actualStore?.created).isEqualTo("2011-01-01T00:00:00Z")
        assertThat(actualStore?.updated).isEqualTo("2020-04-20T02:33:58Z")
        assertThat(actualStore?.disabled).isEqualTo("2018-05-29 04:32:45")
        assertThat(actualStore?.typeMapperEnabled).isEqualTo(false)
        assertThat(actualStore?.region).isEqualTo("KR")
    }

    @Test
    fun testRetrieveStoreInfo_getsSomeNullValuesForStoreInfo_hasExpectedStore() = runBlocking {
        var actualStore: Store? = null

        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200,
                TestFixtures.STORE_WITH_NULL_VALUES.toString().byteInputStream())
        ))
        virtusize.getStoreInfo(
            object: SuccessResponseHandler {
                override fun onSuccess(data: Any?) {
                    actualStore = data as Store
                }
            }, object : ErrorResponseHandler {
                override fun onError(error: VirtusizeError) {}
            }
        )

        assertThat(actualStore?.id).isEqualTo(2)
        assertThat(actualStore?.surveyLink).isEqualTo("https://www.survey.com/s/xxxxxx")
        assertThat(actualStore?.name).isEqualTo("Virtusize")
        assertThat(actualStore?.shortName).isEqualTo("virtusize")
        assertThat(actualStore?.lengthUnitId).isEqualTo(2)
        assertThat(actualStore?.apiKey).isEqualTo(TestFixtures.API_KEY)
        assertThat(actualStore?.created).isEqualTo("2011-01-01T00:00:00Z")
        assertThat(actualStore?.updated).isEqualTo("2020-04-20T02:33:58Z")
        assertThat(actualStore?.disabled).isEqualTo("")
        assertThat(actualStore?.typeMapperEnabled).isEqualTo(false)
        assertThat(actualStore?.region).isEqualTo("JP")
    }

    @Test
    fun testSendOrder_whenSuccessful_onSuccessShouldBeCalled() = runBlocking {
        var isSuccessful = false

        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(204, "".byteInputStream())
        ))
        virtusize.sendOrder(
            TestFixtures.VIRTUSIZE_ORDER,
            object: SuccessResponseHandler {
                override fun onSuccess(data: Any?) {
                    isSuccessful = true
                }
            }, object : ErrorResponseHandler {
                override fun onError(error: VirtusizeError) {}
            }
        )

        assertThat(isSuccessful).isTrue()
    }

    @Test
    fun testGetStoreProductInfo_whenSuccessful_onSuccessShouldReturnExpectedStoreProduct() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, ProductFixtures.STORE_PRODUCT_INFO_JSON_DATA.toString().byteInputStream())
        ))

        val storeProductResponse = virtusize.getStoreProductResponse(TestFixtures.PRODUCT_ID)
        assertThat(storeProductResponse is VirtusizeApiResponse.Success<Product?>)
        val actualProduct = (storeProductResponse as VirtusizeApiResponse.Success<Product?>).data
        assertThat(actualProduct?.id).isEqualTo(TestFixtures.PRODUCT_ID)
        assertThat(actualProduct?.sizes?.size).isEqualTo(2)
        assertThat(actualProduct?.externalId).isEqualTo(TestFixtures.EXTERNAL_ID)
        assertThat(actualProduct?.productType).isEqualTo(8)
        assertThat(actualProduct?.name).isEqualTo(TestFixtures.PRODUCT_NAME)
        assertThat(actualProduct?.storeId).isEqualTo(TestFixtures.STORE_ID)
        assertThat(actualProduct?.storeProductMeta?.id).isEqualTo(1)
        assertThat(actualProduct?.storeProductMeta?.id).isEqualTo(1)
        val expectedAdditionalInfo = StoreProductAdditionalInfo(
            "Virtusize",
            "female",
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
            ),
            mutableMapOf(
                "hip" to 85,
                "size" to "38",
                "waist" to 56,
                "bust" to 78,
                "height" to 165
            ),
            "regular",
            "fashionable",
            BrandSizing("large", false)
        )
        assertThat(actualProduct?.storeProductMeta?.additionalInfo).isEqualTo(expectedAdditionalInfo)
    }

    @Test
    fun testGetProductTypes_whenSuccessful_onSuccessShouldReturnExpectedProductTypeList() = runBlocking {
        var actualProductTypeList: List<ProductType>? = null

        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, ProductFixtures.PRODUCT_TYPE_JSON_ARRAY.toString().byteInputStream())
        ))

        virtusize.getProductTypes(
            onSuccess = {
                actualProductTypeList = it
            }
        )

        assertThat(actualProductTypeList?.size).isEqualTo(2)
        assertThat(actualProductTypeList?.get(0)).isEqualTo(
            ProductType(
                1,
                "dress",
                mutableSetOf(
                    Weight("bust", 1f),
                    Weight("waist", 1f),
                    Weight("height", 0.25f)
                )
            )
        )
        assertThat(actualProductTypeList?.get(1)).isEqualTo(
            ProductType(
                18,
                "bag",
                mutableSetOf(
                    Weight("depth", 1f),
                    Weight("width", 2f),
                    Weight("height", 1f)
                )
            )
        )
    }

    @Test
    fun testGetUserProducts_whenSuccessful_shouldReturnExpectedUserProduct() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, ProductFixtures.USER_PRODUCT_JSON_ARRAY.toString().byteInputStream())
        ))

        val actualApiResponse = virtusize.getUserProductsApiResponse()
        assertThat(actualApiResponse is VirtusizeApiResponse.Success<List<Product>?>)
        val actualUserProductList = (actualApiResponse as? VirtusizeApiResponse.Success<List<Product>?>)?.data
        assertThat(actualUserProductList?.size).isEqualTo(2)
        assertThat(actualUserProductList?.get(0)?.id).isEqualTo(123456)
        assertThat(actualUserProductList?.get(0)?.sizes?.size).isEqualTo(1)
        assertThat(actualUserProductList?.get(0)?.sizes?.get(0)?.name).isEqualTo("S")
        assertThat(actualUserProductList?.get(1)?.id).isEqualTo(654321)
        assertThat(actualUserProductList?.get(1)?.sizes?.get(0)?.name).isEqualTo("")
        assertThat(actualUserProductList?.get(1)?.sizes?.get(0)?.measurements).isEqualTo(
            mutableSetOf(
                Measurement("height", 820),
                Measurement("bust", 520),
                Measurement("sleeve", 930)
            ))
        assertThat(actualUserProductList?.get(1)?.productType).isEqualTo(2)
        assertThat(actualUserProductList?.get(1)?.name).isEqualTo("test2")
        assertThat(actualUserProductList?.get(1)?.cloudinaryPublicId).isEqualTo("")
        assertThat(actualUserProductList?.get(1)?.isFavorite).isEqualTo(true)
    }

    @Test
    fun testGetUserProducts_emptyWardrobe_shouldReturnEmptyUserProductList() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, ProductFixtures.EMPTY_PRODUCT_JSON_ARRAY.toString().byteInputStream())
        ))

        val actualApiResponse = virtusize.getUserProductsApiResponse()
        assertThat(actualApiResponse is VirtusizeApiResponse.Success<List<Product>?>)
        val actualUserProductList = (actualApiResponse as? VirtusizeApiResponse.Success<List<Product>?>)?.data
        assertThat(actualUserProductList?.size).isEqualTo(0)
    }

    @Test
    fun testGetUserProducts_wardrobeNotExisted_shouldReturn404Error() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(404, ProductFixtures.WARDROBE_NOT_FOUND_ERROR_JSONObject.toString().byteInputStream())
        ))

        val actualApiResponse = virtusize.getUserProductsApiResponse()
        assertThat(actualApiResponse is VirtusizeApiResponse.Error)
        val actualError = (actualApiResponse as? VirtusizeApiResponse.Error)?.error

        assertThat(actualError?.code).isEqualTo(HttpURLConnection.HTTP_NOT_FOUND)
        assertThat(actualError?.message).contains("{\"detail\":\"No wardrobe found\"}")
        assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.NetworkError)
    }

    @Test
    fun testGetUserBodyProfile_getsValidUserBodyProfile_shouldHaveExpectedUserBodyProfile() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, TestFixtures.USER_BODY_JSONObject.toString().byteInputStream())
        ))

        val actualApiResponse = virtusize.getUserBodyProfileResponse()
        assertThat(actualApiResponse is VirtusizeApiResponse.Success<UserBodyProfile?>)
        val actualUserBodyProfile = (actualApiResponse as? VirtusizeApiResponse.Success<UserBodyProfile?>)?.data

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

    @Test
    fun testGetUserBodyProfile_getsEmptyUserBodyProfile_shouldExpectNull() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(200, TestFixtures.NULL_USER_BODY_PROFILE.toString().byteInputStream())
        ))

        val actualApiResponse = virtusize.getUserBodyProfileResponse()
        assertThat(actualApiResponse is VirtusizeApiResponse.Success<UserBodyProfile?>)
        val actualUserBodyProfile = (actualApiResponse as? VirtusizeApiResponse.Success<UserBodyProfile?>)?.data

        assertThat(actualUserBodyProfile).isNull()
    }

    @Test
    fun testGetUserBodyProfile_wardrobeNotExisted_shouldReturn404Error() = runBlocking {
        virtusize.setHTTPURLConnection(MockHttpURLConnection(
            mockURL,
            MockedResponse(404, ProductFixtures.WARDROBE_NOT_FOUND_ERROR_JSONObject.toString().byteInputStream())
        ))

        val actualApiResponse = virtusize.getUserBodyProfileResponse()
        assertThat(actualApiResponse is VirtusizeApiResponse.Error)
        val actualError = (actualApiResponse as? VirtusizeApiResponse.Error)?.error

        assertThat(actualError?.code).isEqualTo(HttpURLConnection.HTTP_NOT_FOUND)
        assertThat(actualError?.message).contains("{\"detail\":\"No wardrobe found\"}")
        assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.NetworkError)
    }

    companion object {
        private const val INTERNAL_SERVER_ERROR_RESPONSE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n<title>500 Internal Server Error</title>\n" +
        "<h1>Internal Server Error</h1>\n" +
        "<p>The server encountered an internal error and was unable to complete your request.  " +
        "Either the server is overloaded or there is an error in the application.</p>"
    }
}