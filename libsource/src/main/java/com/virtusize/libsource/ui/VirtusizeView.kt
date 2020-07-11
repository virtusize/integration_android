package com.virtusize.libsource.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.*
import android.webkit.*
import androidx.fragment.app.DialogFragment
import com.virtusize.libsource.Constants
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import com.virtusize.libsource.data.parsers.VirtusizeEventJsonParser
import kotlinx.android.synthetic.main.web_activity.*
import org.json.JSONObject

class VirtusizeView: DialogFragment() {

    private var virtusizeBaseUrl = "https://static.api.virtusize.jp/a/aoyama/latest/sdk-integration/sdk-webview.html"
    private var vsParamsFromSDKScript = ""

    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
    private lateinit var virtusizeButton: VirtusizeButton

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.VirtusizeDialogFragmentAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets style to dialog to show it as full screen
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.web_activity, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Enable JavaScript in the web view
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        // Add the Javascript interface to interface the web app with the web view
        webView.addJavascriptInterface(WebAppInterface(), Constants.JSBridgeName)
        // Set up the web view client that adds JavaScript scripts for the interaction between the SDK and the web
        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if(url != null && url.contains("sdk-webview")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        view?.evaluateJavascript(vsParamsFromSDKScript, null)
                       } else {
                        view?.loadUrl(vsParamsFromSDKScript)
                    }
                }
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                if(url != null && url.contains("i18n")) {
                    webView.removeAllViews()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: Message): Boolean {
                if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport) {
                    val newWebView = WebView(view.context)
                    newWebView.settings.javaScriptEnabled = true
                    newWebView.settings.javaScriptCanOpenWindowsAutomatically = true
                    newWebView.settings.setSupportMultipleWindows(true)
                    newWebView.settings.userAgentString = System.getProperty("http.agent")
                    newWebView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            if(url.contains("survey")) {
                                webView.loadUrl(url)
                                webView.removeAllViews()
                            }
                            return false
                        }
                    }
                    newWebView.webChromeClient = object : WebChromeClient(){
                        override fun onCloseWindow(window: WebView) {
                            webView.removeAllViews()
                        }
                    }
                    val transport = resultMsg.obj as WebView.WebViewTransport
                    webView.addView(newWebView)
                    transport.webView = newWebView
                    resultMsg.sendToTarget()
                }
                return true
            }
        }

        webView.setOnKeyListener{ _, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP){
                if(webView.canGoBack()) {
                    webView.goBack()
                } else if(webView.childCount > 0) {
                    webView.removeAllViews()
                } else {
                    showConfirmDialog()
                }
            }
            true
        }

        // Get the Virtusize URL passed in fragment arguments
        arguments?.getString(Constants.URL_KEY)?.let {
            virtusizeBaseUrl = it
        }
        // Get the Virtusize params script passed in fragment arguments
        arguments?.getString(Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY)?.let {
            vsParamsFromSDKScript = it
        } ?: run {
            dismiss()
        }
        webView.loadUrl(virtusizeBaseUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.stopLoading()
        webView.destroy()
    }

    // TODO
    private fun showConfirmDialog() {
        dismiss()
    }

    /**
     * Sets up a Virtusize message handler
     */
    internal fun setupMessageHandler(messageHandler: VirtusizeMessageHandler, virtusizeButton: VirtusizeButton) {
        virtusizeMessageHandler = messageHandler
        this.virtusizeButton = virtusizeButton
    }

    private inner class WebAppInterface {

        @JavascriptInterface
        fun eventHandler(evenBody: String) {
            val event = VirtusizeEventJsonParser().parse(JSONObject(evenBody))
            event?.let { virtusizeMessageHandler.onEvent(virtusizeButton, it) }
            if (event?.name =="user-closed-widget") {
                dismiss()
            }
        }
    }
}