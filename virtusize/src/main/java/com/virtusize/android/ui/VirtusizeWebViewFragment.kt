package com.virtusize.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.virtusize.android.R
import com.virtusize.android.SharedPreferencesHelper
import com.virtusize.android.auth.VirtusizeAuth
import com.virtusize.android.auth.utils.VirtusizeURLCheck
import com.virtusize.android.data.local.StoreId
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.isUnitedArrows
import com.virtusize.android.data.parsers.VirtusizeEventJsonParser
import com.virtusize.android.databinding.FragmentVirtusizeWebviewBinding
import com.virtusize.android.network.VirtusizeAPIService
import com.virtusize.android.network.VirtusizeApi
import com.virtusize.android.util.Constants
import kotlinx.coroutines.launch
import org.json.JSONObject

class VirtusizeWebViewFragment : DialogFragment() {
    private var virtusizeWebAppUrl: String = VirtusizeApi.getVirtusizeWebViewURL()

    private var showSNSButtons: Boolean = false
    private var vsParamsFromSDKScript = ""
    private var backButtonClickEventFromSDKScript =
        "javascript:vsEventFromSDK({ name: 'sdk-back-button-tapped'})"

    private var virtusizeMessageHandler: VirtusizeMessageHandler? = null

    private val apiService: VirtusizeAPIService by lazy {
        VirtusizeAPIService.getInstance(requireContext(), virtusizeMessageHandler)
    }

    private lateinit var clientProduct: VirtusizeProduct

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private lateinit var binding: FragmentVirtusizeWebviewBinding

    private val virtusizeSNSAuthLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            VirtusizeAuth.handleVirtusizeSNSAuthResult(
                binding.webView,
                result.resultCode,
                result.data,
            )
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets style to dialog to show it as full screen
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentVirtusizeWebviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.VirtusizeDialogFragmentAnimation
        binding.webView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.virtusizeWhite))
        // Enable JavaScript in the web view
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.databaseEnabled = true
        binding.webView.settings.setSupportMultipleWindows(true)
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        // It's necessary for update WKWebView customUserAgent
        // to make Google SDK work inside WebView. See https://stackoverflow.com/a/73152331
        binding.webView.settings.userAgentString = "Mozilla/5.0 AppleWebKit/605.1.15 Mobile/15E148 Safari/604.1"
        // Add the Javascript interface to interface the web app with the web view
        binding.webView.addJavascriptInterface(WebAppInterface(), Constants.JS_BRIDGE_NAME)
        // Set up the web view client that adds JavaScript scripts for the interaction between the SDK and the web
        binding.webView.webViewClient =
            object : WebViewClient() {
                override fun onPageFinished(
                    view: WebView?,
                    url: String?,
                ) {
                    if (url != null && url.contains(virtusizeWebAppUrl)) {
                        binding.webView.evaluateJavascript(vsParamsFromSDKScript, null)
                        binding.webView.evaluateJavascript(
                            "javascript:window.virtusizeSNSEnabled = $showSNSButtons;",
                            null,
                        )
                        getBrowserIDFromCookies()?.let { bid ->
                            if (bid != sharedPreferencesHelper.getBrowserId()) {
                                sharedPreferencesHelper.storeBrowserId(bid)
                            }
                        }
                    }
                }

                override fun onLoadResource(
                    view: WebView?,
                    url: String?,
                ) {
                    super.onLoadResource(view, url)
                    // To prevent multiple views in the WebView when a user selects a different display language
                    if (url != null && url.contains("i18n")) {
                        binding.webView.removeAllViews()
                    }
                }
            }

        binding.webView.webChromeClient =
            object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView,
                    dialog: Boolean,
                    userGesture: Boolean,
                    resultMsg: Message,
                ): Boolean {
                    if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport) {
                        val popupWebView =
                            WebView(view.context).apply {
                                setBackgroundColor(Color.TRANSPARENT)
                                layoutParams =
                                    ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                    )
                                settings.javaScriptEnabled = true
                                settings.javaScriptCanOpenWindowsAutomatically = true
                                settings.setSupportMultipleWindows(true)
                                settings.domStorageEnabled = true
                                settings.userAgentString = System.getProperty("http.agent")
                                webViewClient =
                                    object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView,
                                            url: String,
                                        ): Boolean {
                                            if (VirtusizeURLCheck.isExternalLinkFromVirtusize(url)) {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                try {
                                                    startActivity(intent)
                                                } finally {
                                                    return true
                                                }
                                            }
                                            if (showSNSButtons) {
                                                return VirtusizeAuth.isSNSAuthUrl(
                                                    requireContext(),
                                                    virtusizeSNSAuthLauncher,
                                                    url,
                                                ).also { isSNSAuthUrl ->
                                                    if (isSNSAuthUrl) {
                                                        binding.webView.removeAllViews()
                                                    }
                                                }
                                            }
                                            return false
                                        }
                                    }
                                webChromeClient =
                                    object : WebChromeClient() {
                                        override fun onCloseWindow(window: WebView) {
                                            binding.webView.removeAllViews()
                                        }
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
                    else ->
                        binding.webView.evaluateJavascript(
                            backButtonClickEventFromSDKScript,
                            null,
                        )
                }
            }
            true
        }

        // Get the Virtusize params script passed in fragment arguments.
        // If the script is not passed, we dismiss this dialog fragment.
        vsParamsFromSDKScript = arguments?.getString(Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY) ?: run {
            dismiss()
            return
        }

        showSNSButtons = arguments?.getBoolean(Constants.VIRTUSIZE_SHOW_SNS_BUTTONS) ?: false

        clientProduct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(Constants.VIRTUSIZE_PRODUCT_KEY, VirtusizeProduct::class.java)
        } else {
            arguments?.getParcelable(Constants.VIRTUSIZE_PRODUCT_KEY)
        } ?: run {
            dismiss()
            return
        }

        val productStoreId = clientProduct.productCheckData?.data?.storeId
        when {
            productStoreId != null && StoreId(productStoreId).isUnitedArrows -> {
                virtusizeWebAppUrl = VirtusizeApi.getVirtusizeWebViewURLForSpecificClients()
                binding.webView.loadUrl(virtusizeWebAppUrl)
            }
            else ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val fetchedVersion =
                        apiService.fetchLatestAoyamaVersion().successData ?: run {
                            dismiss()
                            return@launch
                        }

                    val versionPattern = "\\d+\\.\\d+\\.\\d+".toRegex()
                    virtusizeWebAppUrl = virtusizeWebAppUrl.replace(regex = versionPattern, replacement = fetchedVersion)

                    binding.webView.loadUrl(virtusizeWebAppUrl)
                }
        }
    }

    override fun onStop() {
        super.onStop()
        // Cancel the window enter animation
        dialog?.window?.setWindowAnimations(R.style.VirtusizeDialogFragmentAnimation_Null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.stopLoading()
        binding.webView.destroy()
    }

    override fun onDismiss(dialog: DialogInterface) {
        val activity = requireActivity()
        if (activity is VirtusizeWebViewActivity) {
            activity.finish()
        }
        super.onDismiss(dialog)
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
            when (event) {
                is VirtusizeEvent.UserClosedWidget -> dismiss()
                is VirtusizeEvent.UssrClickedStart -> userAcceptedPrivacyPolicy()
                else -> Unit
            }
        }
    }

    private fun userAcceptedPrivacyPolicy() {
        binding.webView.post {
            binding.webView.evaluateJavascript(
                "localStorage.setItem('acceptedPrivacyPolicy','true');",
                null,
            )
        }
    }
}
