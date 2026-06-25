package com.virtusize.android.auth.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.android.auth.data.SnsType
import com.virtusize.android.auth.data.VirtusizeUser
import com.virtusize.android.auth.network.FacebookAPIService
import com.virtusize.android.auth.network.GoogleAPIService
import com.virtusize.android.auth.repositories.FacebookRepository
import com.virtusize.android.auth.repositories.GoogleRepository
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.EXTRA_NAME_AUTH_URL
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.EXTRA_NAME_SNS_SCRIPT
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.NATIVE_APP_ID_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_ACCESS_TOKEN_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_CODE_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_ENV_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_REGION_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.SNS_TYPE_KEY
import com.virtusize.android.auth.utils.VirtusizeUriHelper
import com.virtusize.android.auth.utils.isVirtusizeSNSAuthURL
import com.virtusize.android.util.valueOf
import java.net.URLDecoder

internal class VitrusizeAuthActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "VsAuth"
        private const val QUERY_REDIRECT_URI_KEY = "redirect_uri"
        private const val QUERY_CHANNEL_URL_KEY = "channel_url"
    }

    private lateinit var viewModel: VirtusizeAuthViewModel
    private var webView: WebView? = null
    private var redirectHandled = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =
            VirtusizeAuthViewModel(
                FacebookRepository(FacebookAPIService.getInstance(context = this)),
                GoogleRepository(GoogleAPIService.getInstance(context = this)),
            )

        val appId = intent.getStringExtra(NATIVE_APP_ID_KEY)?.replace("_", "-")
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
                val env =
                    if (authUrl.isVirtusizeSNSAuthURL()) {
                        originalRedirectUri
                            .substringAfter("sns-proxy/", "")
                            .substringBefore("/", "staging")
                    } else {
                        "production"
                    }

                authUrl =
                    authUrl.replace(
                        originalRedirectUri,
                        VirtusizeUriHelper.getRedirectUrl(region, env),
                    )

                // remove `channel_url` parameters as it breaks Facebook login and its outdated
                if (authUrl.indexOf("$QUERY_CHANNEL_URL_KEY=") >= 0) {
                    val start = authUrl.indexOf("$QUERY_CHANNEL_URL_KEY=")
                    val end = authUrl.indexOf("&", start)
                    authUrl = authUrl.replaceRange(start, end, "")
                }

                // Append the application ID to the value of the `state` query parameter
                var uri = Uri.parse(authUrl)
                uri =
                    VirtusizeUriHelper.updateStateWithValue(
                        uri,
                        NATIVE_APP_ID_KEY,
                        appId?.lowercase().toString(),
                    )

                val snsType = SnsType.fromHost(uri)
                if (snsType != null) {
                    uri =
                        VirtusizeUriHelper.updateStateWithValue(
                            uri,
                            SNS_TYPE_KEY,
                            snsType.value,
                        )
                }

                uri =
                    VirtusizeUriHelper.updateStateWithValue(
                        uri,
                        SNS_REGION_KEY,
                        region.lowercase(),
                    )

                uri =
                    VirtusizeUriHelper.updateStateWithValue(
                        uri,
                        SNS_ENV_KEY,
                        env.lowercase(),
                    )

                // Render the SNS sign-in flow in an embedded WebView instead of a Chrome Custom
                // Tab. The Custom Tab showed a blank screen after the user picked a Google account
                // (Chrome blocks the automatic redirect back to the app's custom scheme without a
                // user gesture). A WebView using the device's default user agent is accepted by
                // Google and lets us intercept the final redirect ourselves. See the working
                // iceberg-native SDK which uses the same embedded-WebView approach.
                Log.d(TAG, "onCreate loading auth url: $uri")
                setContentView(createAuthWebView().also { webView = it })
                webView?.loadUrl(uri.toString())
            }
            else -> finish()
        }

        subscribeUI()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createAuthWebView(): WebView =
        WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            // Use the device's default browser user agent (not the SDK Safari UA) so providers
            // such as Google do not reject the embedded browser.
            settings.userAgentString = System.getProperty("http.agent")
            webViewClient =
                object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        val url = request.url
                        val scheme = url.scheme?.lowercase()
                        Log.d(TAG, "shouldOverrideUrlLoading scheme=$scheme url=$url")
                        // The SNS proxy redirects to the app's custom scheme
                        // (e.g. com.company.app.virtusize://sns-auth?...) once the provider
                        // sign-in completes. Consume that redirect and deliver the result.
                        if (scheme != null && scheme != "http" && scheme != "https") {
                            handleRedirect(url)
                            return true
                        }
                        return false
                    }

                    override fun onPageFinished(
                        view: WebView?,
                        url: String?,
                    ) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "onPageFinished url=$url")
                        // Defensive: some SNS proxy pages deliver the result to the opener via
                        // postMessage and never navigate to the app's custom scheme. When loaded
                        // directly (no opener) that leaves a blank page, so also try to consume the
                        // token straight from the loaded URL.
                        url?.let { handleRedirect(Uri.parse(it)) }
                    }
                }
        }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Fallback for the manifest deep-link path (e.g. when the flow falls back to an external
        // browser): consume a redirect URI we were resumed with. No-op on the initial launch.
        handleRedirect(intent.data)
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    /**
     * Handles the SNS redirect URI carrying the access token (or code) and the `state` params.
     *
     * @return true if [redirectURL] was a valid SNS redirect that we consumed.
     */
    private fun handleRedirect(redirectURL: Uri?): Boolean {
        val accessToken =
            redirectURL?.getQueryParameter(SNS_ACCESS_TOKEN_KEY)
                ?: redirectURL?.getQueryParameter(SNS_CODE_KEY)
        if (redirectURL == null || accessToken == null) {
            return false
        }
        // Both shouldOverrideUrlLoading and onPageFinished may surface the same redirect; only
        // process it once so we don't fire the user-info request (and finish) twice.
        if (redirectHandled) {
            return true
        }
        redirectHandled = true

        // Restore SNS params from `state` parameter as json
        val stateMap = VirtusizeUriHelper.getStateMap(redirectURL)
        val snsTypeValue = stateMap[SNS_TYPE_KEY] as? String
        val snsType = if (snsTypeValue != null) valueOf<SnsType>(snsTypeValue.uppercase()) else null

        // Restore original redirect URL
        val region = stateMap[SNS_REGION_KEY] as? String
        val env = stateMap[SNS_ENV_KEY] as? String
        val redirectUrl = VirtusizeUriHelper.getRedirectUrl(region, env)
        Log.d(TAG, "handleRedirect token=present snsType=$snsType region=$region env=$env")

        when (snsType) {
            SnsType.GOOGLE, SnsType.FACEBOOK -> {
                viewModel.getUserInfo(snsType, accessToken)
            }
            SnsType.LINE -> {
                val data = Intent()
                data.putExtra(
                    EXTRA_NAME_SNS_SCRIPT,
                    getSNSLoginScript(accessToken, redirectUrl),
                )
                setResult(Activity.RESULT_OK, data)
                finish()
            }
            null -> finish() // cancel the flow if the SNS Provider can't be resolved
        }
        return true
    }

    private fun subscribeUI() {
        viewModel.virtusizeUser.observe(this) { user ->
            Log.d(TAG, "getUserInfo result user=${if (user != null) "ok(${user.email})" else "null"}; finishing")
            if (user != null) {
                val data = Intent()
                data.putExtra(
                    EXTRA_NAME_SNS_SCRIPT,
                    getSNSAuthScript(user),
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

    private fun getSNSLoginScript(
        accessToken: String,
        redirectUri: String,
    ): String {
        return """
            sdkSnsLogin({
                code: '$accessToken',
                snsType: 'line',
                redirectUri: '$redirectUri'
            })
            """.trimIndent()
    }
}
