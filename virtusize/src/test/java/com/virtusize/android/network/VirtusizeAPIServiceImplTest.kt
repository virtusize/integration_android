package com.virtusize.android.network

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.MainCoroutineRule
import com.virtusize.android.TestUtils
import com.virtusize.android.VirtusizeBuilder
import com.virtusize.android.data.local.VirtusizeEnvironment
import com.virtusize.android.data.local.VirtusizeErrorType
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.message
import com.virtusize.android.data.parsers.I18nLocalizationJsonParser
import com.virtusize.android.data.remote.BrandSizing
import com.virtusize.android.data.remote.Measurement
import com.virtusize.android.data.remote.ProductSize
import com.virtusize.android.data.remote.ProductType
import com.virtusize.android.data.remote.StoreProductAdditionalInfo
import com.virtusize.android.data.remote.Weight
import com.virtusize.android.fixtures.ProductFixtures
import com.virtusize.android.fixtures.TestFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
@ExperimentalCoroutinesApi
class VirtusizeAPIServiceImplTest {
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val testDispatcher = TestCoroutineDispatcher()

    private var virtusizeAPIService = VirtusizeAPIServiceImpl(context, null)

    private var mockURL: URL = URL("https://www.mockurl.com/")

    @Before
    fun setup() {
        VirtusizeBuilder().init(context)
            .setApiKey(TestFixtures.API_KEY)
            .setUserId(TestFixtures.USER_ID)
            .setEnv(VirtusizeEnvironment.STAGING)
            .build()

        virtusizeAPIService.setCoroutineDispatcher(testDispatcher)
    }

    @Test
    fun testProductCheck_isValidProduct_hasExpectedData() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        TestFixtures.PRODUCT_CHECK_DATA.toString().byteInputStream(),
                    ),
                ),
            )

            val actualProductCheck =
                virtusizeAPIService.productCheck(TestFixtures.VIRTUSIZE_PRODUCT).successData

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
    fun testProductCheck_isInvalidProduct() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        TestFixtures.INVALID_PRODUCT_CHECK_JSON_DATA.toString().byteInputStream(),
                    ),
                ),
            )
            val actualProductCheck =
                virtusizeAPIService.productCheck(TestFixtures.VIRTUSIZE_PRODUCT).successData

            assertThat(actualProductCheck?.name).isEqualTo("backend-checked-product")
            assertThat(actualProductCheck?.data?.validProduct).isFalse()
            assertThat(actualProductCheck?.data?.productDataId).isEqualTo(0)
        }

    @Test
    fun testProductCheck_provideWrongAPIKey_hasNetworkError() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        HttpURLConnection.HTTP_FORBIDDEN,
                        "{ \"Code\": \"ForbiddenError\", \"Message\": \"ForbiddenError: \" }"
                            .byteInputStream(),
                    ),
                ),
            )

            val actualError =
                virtusizeAPIService.productCheck(TestFixtures.VIRTUSIZE_PRODUCT).failureData

            assertThat(actualError?.code).isEqualTo(HttpURLConnection.HTTP_FORBIDDEN)
            assertThat(
                actualError?.message,
            ).isEqualTo(VirtusizeErrorType.ApiKeyNullOrInvalid.message())
            assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.ApiKeyNullOrInvalid)
        }

    @Test
    fun testSendProductImageToBackend_whenSuccessful_hasExpectedProductMetaDataHints() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        TestFixtures.PRODUCT_META_DATA_HINTS.toString().byteInputStream(),
                    ),
                ),
            )

            val actualProductMetaDataHints =
                virtusizeAPIService.sendProductImageToBackend(
                    TestFixtures.VIRTUSIZE_PRODUCT,
                ).successData

            assertThat(actualProductMetaDataHints?.apiKey).isEqualTo(TestFixtures.API_KEY)
            assertThat(
                actualProductMetaDataHints?.imageUrl,
            ).isEqualTo(
                "http://www.test.com/goods/31/12/11/71/1234_COL_COL02_570.jpg",
            )
            assertThat(
                actualProductMetaDataHints?.cloudinaryPublicId,
            ).isEqualTo(
                "test_cloudinaryPublicId",
            )
            assertThat(actualProductMetaDataHints?.externalProductId).isEqualTo("694")
        }

    @Test
    fun testSendProductImageToBackend_whenFailed_hasExpectedErrorInfo() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        500,
                        INTERNAL_SERVER_ERROR_RESPONSE.byteInputStream(),
                        null,
                    ),
                ),
            )
            val actualError =
                virtusizeAPIService.sendProductImageToBackend(
                    TestFixtures.VIRTUSIZE_PRODUCT,
                ).failureData

            assertThat(actualError?.code).isEqualTo(500)
            assertThat(actualError?.message).contains(INTERNAL_SERVER_ERROR_RESPONSE)
            assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.APIError)
        }

    @Test
    fun testSendUserSawProductEvent_whenSuccessful_onSuccessShouldBeCalled() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        201,
                        TestFixtures.USER_SAW_PRODUCT_EVENT_RESPONSE.toString().byteInputStream(),
                    ),
                ),
            )

            val sendEventResponse =
                virtusizeAPIService.sendEvent(VirtusizeEvent.UserSawProduct(data = null))

            assertThat(sendEventResponse.isSuccessful).isTrue()
        }

    @Test
    fun testRetrieveStoreInfo_getsFullStoreInfoResponse_hasExpectedStore() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        TestFixtures.STORE_WITH_FULL_INFO.toString().byteInputStream(),
                    ),
                ),
            )

            val actualStore = virtusizeAPIService.getStoreInfo().successData

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
    fun testRetrieveStoreInfo_getsSomeNullValuesForStoreInfo_hasExpectedStore() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        TestFixtures.STORE_WITH_NULL_VALUES.toString().byteInputStream(),
                    ),
                ),
            )

            val actualStore = virtusizeAPIService.getStoreInfo().successData

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
    fun testSendOrder_whenSuccessful_onSuccessShouldBeCalled() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(204, "".byteInputStream()),
                ),
            )

            val sendOrderResponse =
                virtusizeAPIService.sendOrder(
                    "",
                    TestFixtures.VIRTUSIZE_ORDER,
                )

            assertThat(sendOrderResponse.isSuccessful).isTrue()
        }

    @Test
    fun testGetStoreProductInfo_whenSuccessful_onSuccessShouldReturnExpectedStoreProduct() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        ProductFixtures.STORE_PRODUCT_INFO_JSON_DATA.toString().byteInputStream(),
                    ),
                ),
            )

            val actualProduct =
                virtusizeAPIService.getStoreProduct(TestFixtures.PRODUCT_ID).successData

            assertThat(actualProduct?.id).isEqualTo(TestFixtures.PRODUCT_ID)
            assertThat(actualProduct?.sizes?.size).isEqualTo(2)
            assertThat(actualProduct?.externalId).isEqualTo(TestFixtures.EXTERNAL_ID)
            assertThat(actualProduct?.productType).isEqualTo(8)
            assertThat(actualProduct?.name).isEqualTo(TestFixtures.PRODUCT_NAME)
            assertThat(actualProduct?.storeId).isEqualTo(TestFixtures.STORE_ID)
            assertThat(actualProduct?.storeProductMeta?.id).isEqualTo(1)
            assertThat(actualProduct?.storeProductMeta?.id).isEqualTo(1)
            val expectedAdditionalInfo =
                StoreProductAdditionalInfo(
                    "Virtusize",
                    "female",
                    mutableSetOf(
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
                    mutableMapOf(
                        "hip" to 85,
                        "size" to "38",
                        "waist" to 56,
                        "bust" to 78,
                        "height" to 165,
                    ),
                    "regular",
                    "fashionable",
                    BrandSizing("large", false),
                )
            assertThat(actualProduct?.storeProductMeta?.additionalInfo).isEqualTo(
                expectedAdditionalInfo,
            )
        }

    @Test
    fun testGetProductTypesResponse_whenSuccessful_onSuccessShouldReturnExpectedProductTypeList() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        ProductFixtures.PRODUCT_TYPE_JSON_ARRAY.toString().byteInputStream(),
                    ),
                ),
            )

            val actualProductTypeList = virtusizeAPIService.getProductTypes().successData

            assertThat(actualProductTypeList?.size).isEqualTo(4)
            assertThat(actualProductTypeList?.get(0)).isEqualTo(
                ProductType(
                    1,
                    "dress",
                    mutableSetOf(
                        Weight("bust", 1f),
                        Weight("waist", 1f),
                        Weight("height", 0.25f),
                    ),
                    mutableListOf(1, 16),
                ),
            )
            assertThat(actualProductTypeList?.get(3)).isEqualTo(
                ProductType(
                    18,
                    "bag",
                    mutableSetOf(
                        Weight("depth", 1f),
                        Weight("width", 2f),
                        Weight("height", 1f),
                    ),
                    mutableListOf(18, 19, 25, 26),
                ),
            )
        }

    @Test
    fun testGetDeleteUserResponse() =
        runBlocking {
            val expectedDeleteUserJsonResponse =
                "{" +
                    "\"wardrobe(s) deleted from DB\":1," +
                    "\"user product(s) deleted from DB\":1,\"duration\":0.04" +
                    "}"
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(200, expectedDeleteUserJsonResponse.byteInputStream()),
                ),
            )

            val isSuccess = virtusizeAPIService.deleteUser().isSuccessful
            assertThat(isSuccess).isTrue()
        }

    @Test
    fun testGetUserProductsResponse_userHasItemsInTheWardrobe_shouldReturnExpectedUserProducts() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        ProductFixtures.USER_PRODUCT_JSON_ARRAY.toString().byteInputStream(),
                    ),
                ),
            )

            val actualUserProductList = virtusizeAPIService.getUserProducts().successData

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
                    Measurement("sleeve", 930),
                ),
            )
            assertThat(actualUserProductList?.get(1)?.productType).isEqualTo(2)
            assertThat(actualUserProductList?.get(1)?.name).isEqualTo("test2")
            assertThat(actualUserProductList?.get(1)?.cloudinaryPublicId).isEqualTo("")
            assertThat(actualUserProductList?.get(1)?.isFavorite).isEqualTo(true)
        }

    @Test
    fun testGetUserProductsResponse_userHasEmptyWardrobe_shouldReturnEmptyUserProductList() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        ProductFixtures.EMPTY_PRODUCT_JSON_ARRAY.toString().byteInputStream(),
                    ),
                ),
            )

            val actualUserProductList = virtusizeAPIService.getUserProducts().successData

            assertThat(actualUserProductList?.size).isEqualTo(0)
        }

    @Test
    fun testGetUserProductsResponse_wardrobeDoesNotExist_shouldReturn404Error() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        404,
                        ProductFixtures.WARDROBE_NOT_FOUND_ERROR_JSONObject.toString().byteInputStream(),
                    ),
                ),
            )

            val actualError = virtusizeAPIService.getUserProducts().failureData

            assertThat(actualError?.code).isEqualTo(HttpURLConnection.HTTP_NOT_FOUND)
            assertThat(actualError?.message).contains("{\"detail\":\"No wardrobe found\"}")
            assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.APIError)
        }

    @Test
    fun testGetUserBodyProfileResponse_userHasValidBodyProfile_shouldReturnExpectedBodyProfile() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        TestFixtures.USER_BODY_JSONObject.toString().byteInputStream(),
                    ),
                ),
            )

            val actualUserBodyProfile = virtusizeAPIService.getUserBodyProfile().successData

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
            assertThat(actualUserBodyProfile?.footwearData).isEqualTo(
                mapOf(
                    "toeShape" to "greek",
                    "size" to "30.5",
                    "type" to "sneakers",
                    "brand" to "Virtusize",
                    "footWidth" to "regular",
                ),
            )
        }

    @Test
    fun testGetUserBodyProfileResponse_wardrobeDoesNotExist_shouldReturn404Error() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        404,
                        ProductFixtures.WARDROBE_NOT_FOUND_ERROR_JSONObject.toString().byteInputStream(),
                    ),
                ),
            )

            val actualError = virtusizeAPIService.getUserBodyProfile().failureData
            assertThat(actualError?.code).isEqualTo(HttpURLConnection.HTTP_NOT_FOUND)
            assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.APIError)
        }

    @Test
    fun testI18nResponse_whenSuccessful_shouldReturnExpectedI18Localization() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(
                        200,
                        TestUtils.readFileFromAssets("/i18n_en.json").toString().byteInputStream(),
                    ),
                ),
            )

            val actualI18nAsJson =
                virtusizeAPIService.getI18n(
                    VirtusizeLanguage.EN,
                ).successData

            assertThat(actualI18nAsJson).isNotNull()

            val i18nParser = I18nLocalizationJsonParser(context, VirtusizeLanguage.EN)
            val actualI18nLocalization = i18nParser.parse(actualI18nAsJson!!)

            assertThat(
                actualI18nLocalization.bodyDataEmptyText,
            ).isEqualTo(
                "Find your right size",
            )
            assertThat(
                actualI18nLocalization.defaultAccessoryText,
            ).isEqualTo(
                "See what fits inside",
            )
        }

    @Test
    fun testBodyRecommendedSize_whenSuccessful_shouldReturnExpectedBodyProfileRecommendedSize() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    mockURL,
                    MockedResponse(200, "{\"sizeName\": \"large\"}".byteInputStream()),
                ),
            )

            val actualBodyProfileRecommendedSize =
                virtusizeAPIService.getBodyProfileRecommendedItemSize(
                    ProductFixtures.productTypes(),
                    ProductFixtures.storeProduct(),
                    TestFixtures.userBodyProfile,
                ).successData

            assertThat(actualBodyProfileRecommendedSize?.get(0)?.sizeName).isAnyOf("large", null)
        }

    @Test
    fun testBodyRecommendedSize_whenStoreProductIsAnAccessory_shouldReturn400Error() =
        runBlocking {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    URL("https://size-recommendation.virtusize.jp/item"),
                    MockedResponse(
                        400,
                        "{\"Code\": \"BadRequestError\", \"Message\": \"BadRequestError: \"}"
                            .byteInputStream(),
                    ),
                ),
            )

            val actualApiResponse =
                virtusizeAPIService.getBodyProfileRecommendedItemSize(
                    ProductFixtures.productTypes(),
                    ProductFixtures.storeProduct(18),
                    TestFixtures.userBodyProfile,
                )

            val actualError = (actualApiResponse as? VirtusizeApiResponse.Error)?.error

            assertThat(actualError?.code).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST)
            assertThat(
                actualError?.message,
            ).contains(
                "/item" +
                    " - {\"Code\": \"BadRequestError\", \"Message\": \"BadRequestError: \"}",
            )
            assertThat(actualError?.type).isEqualTo(VirtusizeErrorType.APIError)
        }

    @Test
    fun `test fetchLatestAoyamaVersion should return expected value`() =
        runTest {
            virtusizeAPIService.setHTTPURLConnection(
                MockHttpsURLConnection(
                    URL("https://static.api.virtusize.com/a/aoyama/latest.txt"),
                    MockedResponse(
                        200,
                        VirtusizeApi.DEFAULT_AOYAMA_VERSION.byteInputStream(),
                    ),
                ),
            )
            val actualApiResponse = virtusizeAPIService.fetchLatestAoyamaVersion()
            assertThat(actualApiResponse.successData).isEqualTo(VirtusizeApi.DEFAULT_AOYAMA_VERSION)
        }

    companion object {
        private const val INTERNAL_SERVER_ERROR_RESPONSE =
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n" +
                "<title>500 Internal Server Error</title>\n" +
                "<h1>Internal Server Error</h1>\n" +
                "<p>The server encountered an internal error" +
                " and was unable to complete your request." +
                "Either the server is overloaded or there is an error in the application.</p>"
    }
}
