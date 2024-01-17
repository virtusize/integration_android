package com.virtusize.android.network

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.android.core.BuildConfig
import com.virtusize.android.data.local.VirtusizeEnvironment
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeEvents
import com.virtusize.android.data.local.VirtusizeLanguage
import com.virtusize.android.data.local.VirtusizeOrder
import com.virtusize.android.data.local.VirtusizeOrderItem
import com.virtusize.android.data.local.getEventName
import com.virtusize.android.data.parsers.JsonUtils
import com.virtusize.android.fixtures.ProductFixtures
import com.virtusize.android.fixtures.TestFixtures
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class VirtusizeApiTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val defaultDisplay =
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    private val resolution = "${defaultDisplay.height}x${defaultDisplay.width}"
    private val versionCode = BuildConfig.VERSION_NAME

    @Before
    fun initVirtusizeApi() {
        VirtusizeApi.init(
            env = VirtusizeEnvironment.JAPAN,
            key = TestFixtures.API_KEY,
            userId = TestFixtures.USER_ID
        )
    }

    @Test
    fun productCheck_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.productCheck(TestFixtures.VIRTUSIZE_PRODUCT)

        val expectedUrl = "https://services.virtusize.com/stg/product/check" +
            "?apiKey=${TestFixtures.API_KEY}" +
            "&externalId=${TestFixtures.EXTERNAL_ID}" +
            "&version=1"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun virtusizeWebView_stagingEnv_shouldReturnExpectedUrl() {
        val actualUrl = VirtusizeApi.virtusizeWebViewURL()

        val expectedUrl = "https://static.api.virtusize.com/a/aoyama/staging/sdk-webview.html"

        assertThat(actualUrl).isEqualTo(expectedUrl)
    }

    @Test
    fun virtusizeWebView_japanEnv_shouldReturnExpectedUrl() {
        VirtusizeApi.init(
            VirtusizeEnvironment.JAPAN,
            TestFixtures.API_KEY,
            TestFixtures.USER_ID
        )
        val actualUrl = VirtusizeApi.virtusizeWebViewURL()
        val expectedUrl = "https://static.api.virtusize.jp/a/aoyama/latest/sdk-webview.html"

        assertThat(actualUrl).isEqualTo(expectedUrl)
    }

    @Test
    fun sendProductImageToBackend_shouldReturnExpectedApiRequest() {
        val actualApiRequest =
            VirtusizeApi.sendProductImageToBackend(TestFixtures.VIRTUSIZE_PRODUCT)

        val expectedUrl = "https://staging.virtusize.com/rest-api/v1/product-meta-data-hints"

        val expectedParams = mutableMapOf<String, Any>()
        TestFixtures.VIRTUSIZE_PRODUCT.productCheckData?.data?.storeId?.let {
            expectedParams["store_id"] = it.toString()
        }
        expectedParams["external_id"] = TestFixtures.VIRTUSIZE_PRODUCT.externalId
        expectedParams["image_url"] = TestFixtures.VIRTUSIZE_PRODUCT.imageUrl!!
        expectedParams["api_key"] = TestFixtures.API_KEY

        assertThat(actualApiRequest.url).isEqualTo(expectedUrl)
        assertThat(actualApiRequest.method).isEquivalentAccordingToCompareTo(HttpMethod.POST)
        assertThat(actualApiRequest.params).containsExactlyEntriesIn(expectedParams)
    }

    @Test
    fun sendUserSawProductEventToAPI_shouldReturnExpectedApiRequest() {
        val eventName = VirtusizeEvents.UserSawProduct.getEventName()
        val event = VirtusizeEvent(VirtusizeEvents.UserSawProduct.getEventName())
        val actualApiRequest = VirtusizeApi.sendEventToAPI(
            event,
            TestFixtures.PRODUCT_CHECK,
            TestFixtures.ORIENTATION,
            resolution,
            versionCode
        )

        val expectedParams = mutableMapOf<String, Any>(
            "name" to eventName,
            "apiKey" to TestFixtures.API_KEY,
            "type" to "user",
            "source" to "integration-android",
            "userCohort" to "direct",
            "widgetType" to "mobile",
            "browserOrientation" to TestFixtures.ORIENTATION,
            "browserResolution" to resolution,
            "integrationVersion" to versionCode.toString(),
            "snippetVersion" to versionCode.toString()
        )

        TestFixtures.PRODUCT_CHECK?.let { productCheck ->
            expectedParams["storeProductExternalId"] = productCheck.productId

            productCheck.data?.let { data ->
                expectedParams["storeId"] = data.storeId.toString()
                expectedParams["storeName"] = data.storeName
                expectedParams["storeProductType"] = data.productTypeName
            }
        }

        event.data?.optJSONObject("data")?.let {
            expectedParams.plus(JsonUtils.jsonObjectToMap(it))
        }

        assertThat(actualApiRequest.url).isEqualTo("https://events.staging.virtusize.com")
        assertThat(actualApiRequest.method).isEquivalentAccordingToCompareTo(HttpMethod.POST)
        assertThat(actualApiRequest.params).containsExactlyEntriesIn(expectedParams)
    }

    @Test
    fun sendOrder_shouldReturnExpectedApiRequest() {
        val order = VirtusizeOrder("888400111032")
        order.items = mutableListOf(
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
        val actualApiRequest = VirtusizeApi.sendOrder(order)

        val expectedParams = mutableMapOf(
            "apiKey" to TestFixtures.API_KEY,
            "externalOrderId" to "888400111032",
            "externalUserId" to TestFixtures.USER_ID,
            "items" to mutableListOf<MutableMap<String, Any>>(
                mutableMapOf(
                    "externalProductId" to "P001",
                    "size" to "L",
                    "sizeAlias" to "Large",
                    "variantId" to "P001_SIZEL_RED",
                    "imageUrl" to "http://images.example.com/products/P001/red/image1xl.jpg",
                    "color" to "Red",
                    "gender" to "W",
                    "unitPrice" to 5100.00,
                    "currency" to "JPY",
                    "quantity" to 1,
                    "url" to "http://example.com/products/P001"
                )
            )
        )

        assertThat(actualApiRequest.url).isEqualTo("https://staging.virtusize.com/a/api/v3/orders")
        assertThat(actualApiRequest.method).isEquivalentAccordingToCompareTo(HttpMethod.POST)
        assertThat(actualApiRequest.params).containsExactlyEntriesIn(expectedParams)
    }

    @Test
    fun retrieveStoreInfo_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.getStoreInfo()

        val expectedUrl = "https://staging.virtusize.com/a/api/v3/stores/api-key/test_apiKey" +
            "?format=json"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun getStoreProductInfo_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.getStoreProductInfo("16099122")

        val expectedUrl = "https://staging.virtusize.com/a/api/v3/store-products/16099122" +
            "?format=json"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun getProductTypes_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.getProductTypes()

        val expectedUrl = "https://staging.virtusize.com/a/api/v3/product-types"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun getI18n_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.getI18n(VirtusizeLanguage.KR)

        val expectedUrl = "https://i18n.virtusize.jp/bundle-payloads/aoyama/ko"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun getSessions_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.getSessions()

        val expectedUrl = "https://staging.virtusize.com/a/api/v3/sessions"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.POST)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun getUserProducts_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.getUserProducts()

        val expectedUrl = "https://staging.virtusize.com/a/api/v3/user-products"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET, mutableMapOf(), true)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun getUserBodyProfile_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.getUserBodyProfile()

        val expectedUrl = "https://staging.virtusize.com/a/api/v3/user-body-measurements"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET, mutableMapOf(), true)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun getSize_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.getSize(
            ProductFixtures.productTypes(),
            ProductFixtures.storeProduct(),
            TestFixtures.userBodyProfile
        )

        val expectedUrl = "https://size-recommendation.virtusize.jp/item"

        assertThat(actualApiRequest.url).isEqualTo(expectedUrl)
        assertThat(actualApiRequest.method).isEqualTo(HttpMethod.POST)
        assertThat(actualApiRequest.authorization).isEqualTo(false)
        assertThat(actualApiRequest.params["userGender"]).isEqualTo("female")
        assertThat(actualApiRequest.params["userHeight"]).isEqualTo(1630)
        assertThat(actualApiRequest.params["userWeight"]).isEqualTo(50)
        assertThat(actualApiRequest.params["bodyData"]).isEqualTo(
            mutableMapOf(
                "waistWidth" to mutableMapOf(
                    "value" to 225,
                    "predicted" to true
                ),
                "chest" to mutableMapOf(
                    "value" to 755,
                    "predicted" to true
                ),
                "bustWidth" to mutableMapOf(
                    "value" to 245,
                    "predicted" to true
                ),
                "thigh" to mutableMapOf(
                    "value" to 480,
                    "predicted" to true
                ),
                "shoulderWidth" to mutableMapOf(
                    "value" to 340,
                    "predicted" to true
                ),
                "hipHeight" to mutableMapOf(
                    "value" to 750,
                    "predicted" to true
                ),
                "kneeHeight" to mutableMapOf(
                    "value" to 395,
                    "predicted" to true
                ),
                "neck" to mutableMapOf(
                    "value" to 300,
                    "predicted" to true
                ),
                "waistHeight" to mutableMapOf(
                    "value" to 920,
                    "predicted" to true
                ),
                "hip" to mutableMapOf(
                    "value" to 830,
                    "predicted" to true
                ),
                "armpitHeight" to mutableMapOf(
                    "value" to 1130,
                    "predicted" to true
                ),
                "bicep" to mutableMapOf(
                    "value" to 220,
                    "predicted" to true
                ),
                "inseam" to mutableMapOf(
                    "value" to 700,
                    "predicted" to true
                ),
                "headHeight" to mutableMapOf(
                    "value" to 215,
                    "predicted" to true
                ),
                "hipWidth" to mutableMapOf(
                    "value" to 300,
                    "predicted" to true
                ),
                "sleeve" to mutableMapOf(
                    "value" to 720,
                    "predicted" to true
                ),
                "bust" to mutableMapOf(
                    "value" to 755,
                    "predicted" to true
                ),
                "waist" to mutableMapOf(
                    "value" to 630,
                    "predicted" to true
                ),
                "sleeveLength" to mutableMapOf(
                    "value" to 520,
                    "predicted" to true
                ),
                "rise" to mutableMapOf(
                    "value" to 215,
                    "predicted" to true
                ),
                "shoulder" to mutableMapOf(
                    "value" to 370,
                    "predicted" to true
                ),
                "shoulderHeight" to mutableMapOf(
                    "value" to 1240,
                    "predicted" to true
                )
            )
        )
        val items = actualApiRequest.params["items"] as Array<Map<String, Any>>
        for (item in items) {
            assertThat(item["extProductId"]).isEqualTo("694")
            assertThat(item["productType"]).isEqualTo("jacket")
            assertThat(item["itemSizesOrig"]).isEqualTo(mutableMapOf(
                "38" to mutableMapOf(
                    "bust" to 660,
                    "sleeve" to 845,
                    "height" to 760
                ),
                "36" to mutableMapOf(
                    "bust" to 645,
                    "sleeve" to 825,
                    "height" to 750
                )
            ))
            assertThat(item["additionalInfo"]).isEqualTo(mutableMapOf(
                "fit" to "regular",
                "sizes" to mutableMapOf(
                    "38" to mutableMapOf(
                        "bust" to 660,
                        "sleeve" to 845,
                        "height" to 760
                    ),
                    "36" to mutableMapOf(
                        "bust" to 645,
                        "sleeve" to 825,
                        "height" to 750
                    )
                ),
                "gender" to "female",
                "brand" to "Virtusize",
                "modelInfo" to mutableMapOf(
                    "waist" to 56,
                    "bust" to 78,
                    "size" to "38",
                    "hip" to 85,
                    "height" to 165
                )
            ))
        }
    }
}
