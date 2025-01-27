package com.virtusize.android.auth.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.virtusize.android.auth.data.SnsType
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.EXTRA_NAME_AUTH_URL
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.EXTRA_NAME_SNS_SCRIPT
import com.virtusize.android.auth.network.FacebookAPIService
import com.virtusize.android.auth.data.VirtusizeUser
import com.virtusize.android.auth.network.GoogleAPIService
import com.virtusize.android.auth.repositories.FacebookRepository
import com.virtusize.android.auth.repositories.GoogleRepository
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_ACCESS_TOKEN_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.NATIVE_APP_ID_KEY
import com.virtusize.android.data.parsers.JsonUtils
import com.virtusize.android.util.valueOf
import org.json.JSONObject
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import android.content.pm.ResolveInfo
import android.content.pm.PackageManager
import android.util.Log
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_CODE_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_ENV_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_REGION_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_TYPE_KEY
import com.virtusize.android.auth.utils.VirtusizeUriHelper
import com.virtusize.android.auth.utils.isVirtusizeSNSAuthURL
import java.net.URLDecoder

class VitrusizeAuthActivity : AppCompatActivity() {
    companion object {
        private const val QUERY_REDIRECT_URI_KEY = "redirect_uri"
        private const val QUERY_CHANNEL_URL_KEY = "channel_url"
    }

    private lateinit var viewModel: VirtusizeAuthViewModel
    private var customTabIsOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = VirtusizeAuthViewModel(
            FacebookRepository(FacebookAPIService.getInstance(context = this)),
            GoogleRepository(GoogleAPIService.getInstance(context = this))
        )

        val appId = intent.getStringExtra(NATIVE_APP_ID_KEY)
        var authUrl = intent.getStringExtra(EXTRA_NAME_AUTH_URL)
        when {
            authUrl != null -> {
                // Decode the UTF-8 URL
                authUrl = URLDecoder.decode(authUrl, "UTF-8")
                val originalRedirectUri =
                    authUrl.substringAfter("&$QUERY_REDIRECT_URI_KEY=").substringBefore("&")
                val region =
                    originalRedirectUri
                        .substringAfter("virtusize.", "")
                        .substringBefore("/", "com")
                val env = if (originalRedirectUri.isVirtusizeSNSAuthURL()) {
                    originalRedirectUri
                        .substringAfter("sns-proxy/", "")
                        .substringBefore("/", "staging")
                } else {
                    "production"
                }

                authUrl = authUrl.replace(
                    originalRedirectUri,
                    VirtusizeUriHelper.getRedirectUrl(region, env)
                )

                // remove `channel_url` parameters as it breaks Facebook login and its outdated
                if (authUrl.indexOf("$QUERY_CHANNEL_URL_KEY=") >= 0) {
                    val start = authUrl.indexOf("$QUERY_CHANNEL_URL_KEY=")
                    val end = authUrl.indexOf("&", start)
                    authUrl = authUrl.replaceRange(start, end, "")
                }

                // Append the application ID to the value of the `state` query parameter
                var uri = Uri.parse(authUrl)
                uri = VirtusizeUriHelper.updateStateWithValue(
                    uri,
                    NATIVE_APP_ID_KEY,
                    appId?.lowercase().toString())

                val snsType = SnsType.fromHost(uri)
                if (snsType != null) {
                    uri = VirtusizeUriHelper.updateStateWithValue(
                        uri,
                        SNS_TYPE_KEY,
                        snsType.value
                    )
                }

                uri = VirtusizeUriHelper.updateStateWithValue(
                    uri,
                    SNS_REGION_KEY,
                    region.lowercase())

                uri = VirtusizeUriHelper.updateStateWithValue(
                    uri,
                    SNS_ENV_KEY,
                    env.lowercase())


                if (deviceSupportsChromeCustomTabs(this)) {
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(this, uri)
                } else {
                    val launchUrlInBrowser = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(launchUrlInBrowser)
                }
            }
            else -> finish()
        }

        subscribeUI()
    }

    /**
     * Returns a list of packages that support Custom Tabs.
     *
     * Ref: https://developer.chrome.com/docs/android/custom-tabs/integration-guide/#how-can-i-check-whether-the-android-device-has-a-browser-that-supports-custom-tab
     *
     * @param context
     * @return true if the device supports chrome custom tabs
     */
    private fun deviceSupportsChromeCustomTabs(context: Context): Boolean {
        val pm: PackageManager = context.packageManager
        // Get default VIEW intent handler.
        val activityIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse("https://"))

        // Get all apps that can handle VIEW intents.
        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs: ArrayList<ResolveInfo> = ArrayList()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            // Check if this package also resolves the Custom Tabs service.
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info)
            }
        }
        return packagesSupportingCustomTabs.isNotEmpty()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val redirectURL = intent.data
        val accessToken = redirectURL?.getQueryParameter(SNS_ACCESS_TOKEN_KEY)
                        ?: redirectURL?.getQueryParameter(SNS_CODE_KEY)
        if (redirectURL == null || accessToken == null) {
            handleCustomTabToggleAndFinish()
            return
        }

        // Restore SNS params from `state` parameter as json
        val stateMap = VirtusizeUriHelper.getStateMap(redirectURL)
        val snsTypeValue = stateMap[SNS_TYPE_KEY] as? String
        val snsType = if (snsTypeValue != null) valueOf<SnsType>(snsTypeValue.uppercase()) else null

        // Restore original redirect URL
        val region = stateMap[SNS_REGION_KEY] as? String
        val env = stateMap[SNS_ENV_KEY] as? String
        val redirectUrl = VirtusizeUriHelper.getRedirectUrl(region, env)

        when (snsType) {
            SnsType.GOOGLE, SnsType.FACEBOOK -> {
                viewModel.getUserInfo(snsType, accessToken)
            }
            SnsType.LINE -> {
                val data = Intent()
                data.putExtra(
                    EXTRA_NAME_SNS_SCRIPT,
                    getSNSLoginScript(accessToken, redirectUrl)
                )
                setResult(Activity.RESULT_OK, data)
                finish()
            }
            null -> finish() // cancel the flow if the SNS Provider can't be resolved
        }
    }

    private fun handleCustomTabToggleAndFinish() {
        if (customTabIsOpened) {
            customTabIsOpened = false
            finish()
        } else {
            customTabIsOpened = true
        }
    }

    private fun subscribeUI() {
        viewModel.virtusizeUser.observe(this) { user ->
            if (user != null) {
                val data = Intent()
                data.putExtra(
                    EXTRA_NAME_SNS_SCRIPT,
                    getSNSAuthScript(user)
                )
                setResult(Activity.RESULT_OK, data)
            }
            finish()
        }
    }

    private fun getSNSAuthScript(user: VirtusizeUser): String {
        return """
            receiveSNSParamsFromSDK(
                {
                    accessId: '${user.id}',
                    snsType: '${user.snsType}',
                    displayName: '${user.name}',
                    email: '${user.email}'
                }
            )
        """.trimIndent()
    }

    private fun getSNSLoginScript(accessToken: String, redirectUri: String): String {
        return """
            sdkSnsLogin({
                code: '${accessToken}',
                snsType: 'line',
                redirectUri: '${redirectUri}'
            })
        """.trimIndent()
    }
}
