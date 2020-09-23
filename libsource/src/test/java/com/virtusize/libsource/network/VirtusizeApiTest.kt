package com.virtusize.libsource.network

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.fixtures.TestFixtures
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.parsers.JsonUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class VirtusizeApiTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val defaultDisplay = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    private val resolution = "${defaultDisplay.height}x${defaultDisplay.width}"
    private val versionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode

    @Before
    fun initVirtusizeApi(){
        VirtusizeApi.init(
            env = VirtusizeEnvironment.STAGING,
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
    fun virtusize_shouldReturnExpectedUrl() {
        val actualUrl = VirtusizeApi.virtusizeURL()

        val expectedUrl = "https://static.api.virtusize.jp/a/aoyama/latest/sdk-webview.html"

        assertThat(actualUrl).isEqualTo(expectedUrl)
    }

    @Test
    fun sendProductImageToBackend_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.sendProductImageToBackend(TestFixtures.VIRTUSIZE_PRODUCT)

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

        assertThat(actualApiRequest.url).isEqualTo("https://staging.virtusize.com/a/api/v3/events")
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
            "items" to mutableListOf<MutableMap<String, Any>>(mutableMapOf(
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
            ))
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
}