package com.virtusize.android.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import com.virtusize.android.R
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.auth.VirtusizeAuth
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.parsers.VirtusizeEventJsonParser
import com.virtusize.android.databinding.WebActivityBinding
import com.virtusize.android.util.Constants
import org.json.JSONObject

class VirtusizeWebViewFragment : DialogFragment() {

    private var virtusizeWebAppUrl =
        "https://static.api.virtusize.jp/a/aoyama/latest/sdk-webview.html"
    private var vsParamsFromSDKScript = ""
    private var backButtonClickEventFromSDKScript =
        "javascript:vsEventFromSDK({ name: 'sdk-back-button-tapped'})"

    private var virtusizeMessageHandler: VirtusizeMessageHandler? = null
    private lateinit var clientProduct: VirtusizeProduct

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private lateinit var binding: WebActivityBinding

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.VirtusizeDialogFragmentAnimation
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets style to dialog to show it as full screen
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = WebActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Enable JavaScript in the web view
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.databaseEnabled = true
        binding.webView.settings.setSupportMultipleWindows(true)
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        // Add the Javascript interface to interface the web app with the web view
        binding.webView.addJavascriptInterface(WebAppInterface(), Constants.JS_BRIDGE_NAME)
        // Set up the web view client that adds JavaScript scripts for the interaction between the SDK and the web
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null && url.contains(virtusizeWebAppUrl)) {
                    binding.webView.evaluateJavascript(vsParamsFromSDKScript, null)
                    binding.webView.evaluateJavascript(
                        "javascript:window.virtusizeSNSEnabled = true;",
                        null
                    )
                    getBrowserIDFromCookies()?.let { bid ->
                        if (bid != sharedPreferencesHelper.getBrowserId()) {
                            sharedPreferencesHelper.storeBrowserId(bid)
                        }
                    }
                }
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                // To prevent multiple views in the WebView when a user selects a different display language
                if (url != null && url.contains("i18n")) {
                    binding.webView.removeAllViews()
                }
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                dialog: Boolean,
                userGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport) {
                    val popupWebView = WebView(view.context)
                    popupWebView.settings.javaScriptEnabled = true
                    popupWebView.settings.javaScriptCanOpenWindowsAutomatically = true
                    popupWebView.settings.setSupportMultipleWindows(true)
                    popupWebView.settings.userAgentString = System.getProperty("http.agent")
                    popupWebView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            if (isExternalLink(url)) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try {
                                    startActivity(intent)
                                } finally {
                                    return true
                                }
                            }
                            return VirtusizeAuth.isSNSAuthUrl(this@VirtusizeWebViewFragment, url)
                        }
                    }
                    popupWebView.webChromeClient = object : WebChromeClient() {
                        override fun onCloseWindow(window: WebView) {
                            binding.webView.removeAllViews()
                        }
                    }
                    val transport = resultMsg.obj as WebView.WebViewTransport
                    binding.webView.addView(popupWebView)
                    transport.webView = popupWebView
                    resultMsg.sendToTarget()
                }
                return true
            }
        }

        binding.webView.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP) {
                when {
                    binding.webView.canGoBack() -> binding.webView.goBack()
                    binding.webView.childCount > 0 -> {
                        binding.webView.removeAllViews()
                        binding.webView.reload()
                    }
                    else -> binding.webView.evaluateJavascript(
                        backButtonClickEventFromSDKScript, null
                    )
                }
            }
            true
        }

        // Get the Virtusize URL passed in fragment arguments
        arguments?.getString(Constants.URL_KEY)?.let {
            virtusizeWebAppUrl = it
        } ?: run {
            dismiss()
        }

        // Get the Virtusize params script passed in fragment arguments.
        // If the script is not passed, we dismiss this dialog fragment.
        arguments?.getString(Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY)?.let {
            vsParamsFromSDKScript = it
        } ?: run {
            dismiss()
        }

        arguments?.getParcelable<VirtusizeProduct>(Constants.VIRTUSIZE_PRODUCT_KEY)?.let {
            clientProduct = it
        } ?: run {
            dismiss()
        }

        binding.webView.loadUrl(virtusizeWebAppUrl)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        VirtusizeAuth.handleVirtusizeAuthResult(binding.webView, requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.stopLoading()
        binding.webView.destroy()
    }

    /**
     * Sets up a Virtusize message handler
     */
    internal fun setupMessageHandler(messageHandler: VirtusizeMessageHandler) {
        virtusizeMessageHandler = messageHandler
    }

    /**
     * Returns virtusize.bid from the web view cookies
     */
    private fun getBrowserIDFromCookies(): String? {
        var bidValue: String? = null
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(virtusizeWebAppUrl)
        if (cookies != null) {
            val cookiesArray = cookies.split(";".toRegex()).toTypedArray()
            for (cookie in cookiesArray) {
                if (cookie.contains("virtusize.bid")) {
                    bidValue = cookie.split("=".toRegex()).toTypedArray()[1]
                }
            }
        }
        return bidValue
    }

    /**
     * Checks if the URL is an external link to be opened with a browser app
     */
    private fun isExternalLink(url: String): Boolean {
        return url.contains("privacy") || url.contains("survey")
    }

    /**
     * The JavaScript interface to interact the web app with the web view
     */
    private inner class WebAppInterface {

        /**
         * Receives any event information from the Virtusize web app
         * @param eventInfo The String value of the event info
         */
        @JavascriptInterface
        fun eventHandler(eventInfo: String) {
            val event = VirtusizeEventJsonParser().parse(JSONObject(eventInfo))
            event?.let { virtusizeMessageHandler?.onEvent(clientProduct, it) }
            if (event?.name == "user-closed-widget") {
                dismiss()
            }
            if (event?.name == "user-clicked-start") {
                userAcceptedPrivacyPolicy()
            }
        }
    }

    private fun userAcceptedPrivacyPolicy() {
        binding.webView.post {
            binding.webView.evaluateJavascript(
                "localStorage.setItem('acceptedPrivacyPolicy','true');",
                null
            )
        }
    }
}
