package com.virtusize.libsource.network

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.virtusize.libsource.data.JsonResponseSamples
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.JsonUtils
import com.virtusize.libsource.data.remote.parsers.ProductCheckJsonParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random


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
            key = API_KEY,
            browserID = BROWSER_ID,
            userId = USER_ID,
            language = LANGUAGE
        )
    }

    @Test
    fun productCheck_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.productCheck(VIRTUSIZE_PRODUCT)

        val expectedUrl = "https://staging.virtusize.com/integration/v3/product-data-check" +
                "?apiKey=$API_KEY" +
                "&externalId=$EXTERNAL_ID" +
                "&version=1"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    @Test
    fun fitIllustrator_shouldReturnExpectedUrl() {
        val randomNumberString = Random.nextInt(1519982555).toString()
        val actualUrl = VirtusizeApi.fitIllustrator(VIRTUSIZE_PRODUCT, randomNumberString)

        val expectedUrl = "https://staging.virtusize.com/a/fit-illustrator/v1/index.html" +
                "?detached=false" +
                "&bid=$BROWSER_ID" +
                "&addToCartEnabled=false" +
                "&storeId=2" +
                "&_=$randomNumberString" +
                "&spid=${PRODUCT_CHECK?.data?.productDataId}" +
                "&lang=$LANGUAGE" +
                "&android=true" +
                "&sdk=android" +
                "&userId=$USER_ID" +
                "&externalUserId=$USER_ID"

        assertThat(actualUrl).isEqualTo(expectedUrl)
    }

    @Test
    fun sendProductImageToBackend_shouldReturnExpectedApiRequest() {
        val actualApiRequest = VirtusizeApi.sendProductImageToBackend(VIRTUSIZE_PRODUCT)

        val expectedUrl = "https://staging.virtusize.com/rest-api/v1/product-meta-data-hints"

        val expectedParams = mutableMapOf<String, Any>()
        VIRTUSIZE_PRODUCT.productCheckData?.data?.storeId?.let {
            expectedParams["store_id"] = it.toString()
        }
        expectedParams["external_id"] = VIRTUSIZE_PRODUCT.externalId
        expectedParams["image_url"] = VIRTUSIZE_PRODUCT.imageUrl!!
        expectedParams["api_key"] = API_KEY

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
            PRODUCT_CHECK,
            ORIENTATION,
            resolution,
            versionCode
        )

        val expectedParams = mutableMapOf<String, Any>(
            "name" to eventName,
            "apiKey" to API_KEY,
            "type" to "user",
            "source" to "integration-android",
            "userCohort" to "direct",
            "widgetType" to "mobile",
            "browserOrientation" to ORIENTATION,
            "browserResolution" to resolution,
            "integrationVersion" to versionCode.toString(),
            "snippetVersion" to versionCode.toString()
        )

        PRODUCT_CHECK?.let { productCheck ->
            expectedParams["storeProductExternalId"] = productCheck.productId

            productCheck.data?.let { data ->
                expectedParams["storeId"] = data.storeId.toString()
                expectedParams["storeName"] = data.storeName
                expectedParams["storeProductType"] = data.productTypeName

                data.userData?.let { userData ->
                    expectedParams["wardrobeActive"] = userData.wardrobeActive
                    expectedParams["wardrobeHasM"] = userData.wardrobeHasM
                    expectedParams["wardrobeHasP"] = userData.wardrobeHasP
                    expectedParams["wardrobeHasR"] = userData.wardrobeHasR
                }

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
            "apiKey" to API_KEY,
            "externalOrderId" to "888400111032",
            "externalUserId" to USER_ID,
            "items" to mutableListOf<MutableMap<String, Any>>(mutableMapOf(
                "productId" to "P001",
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
        val actualApiRequest = VirtusizeApi.retrieveStoreInfo()

        val expectedUrl = "https://staging.virtusize.com/a/api/v3/stores/api-key/test_apiKey" +
                "?format=json"

        val expectedApiRequest = ApiRequest(expectedUrl, HttpMethod.GET)

        assertThat(actualApiRequest).isEqualTo(expectedApiRequest)
    }

    private companion object {
        const val API_KEY = "test_apiKey"
        const val BROWSER_ID = "browserID"
        const val USER_ID = "123"
        const val EXTERNAL_ID = "7110384"
        const val LANGUAGE = "en"
        const val ORIENTATION = "orientation"

        val PRODUCT_CHECK = ProductCheckJsonParser().parse(JsonResponseSamples.PRODUCT_DATA_CHECK)

        val VIRTUSIZE_PRODUCT = VirtusizeProduct(EXTERNAL_ID, "http://image.com/xxx.jpg", PRODUCT_CHECK)
    }
}