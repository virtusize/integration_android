package com.virtusize.libsource.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.fragment.app.DialogFragment
import com.virtusize.libsource.Constants
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import kotlinx.android.synthetic.main.web_activity.*

class AoyamaView: DialogFragment() {

    private var aoyamaBaseUrl = "https://static.api.virtusize.jp/a/aoyama/testing/sdk-integration/sdk-webview.html"
    private var vsParamsFromSDKScript = "javascript:vsParamsFromSDK({" +
            "apiKey: '52140a6c9e0870294e4c5df4ebc39b47c8237bfa', " +
            "externalProductId: '18575990709', " +
            "env: 'staging', " +
            "language: 'jp'})"

    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
    private lateinit var aoyamaButton: AoyamaButton

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.AoyamaDialogFragmentAnimation
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

        webView.setOnKeyListener{ v, keyCode, event ->
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

        // Get the Aoyama URL passed in fragment arguments
        arguments?.getString(Constants.URL_KEY)?.let {
            aoyamaBaseUrl = it
        }
        // Get the Aoyama params script passed in fragment arguments
        (arguments?.getString(Constants.AOYAMA_PARAMS_SCRIPT_KEY))?.let {
            vsParamsFromSDKScript = it
        }
        webView.loadUrl(aoyamaBaseUrl)
    }

    // TODO
    private fun showConfirmDialog() {
        dismiss()
    }

    /**
     * Sets up a Virtusize message handler
     */
    internal fun setupMessageHandler(messageHandler: VirtusizeMessageHandler, aoyamaButton: AoyamaButton) {
        virtusizeMessageHandler = messageHandler
        this.aoyamaButton = aoyamaButton
    }

    private inner class WebAppInterface {

        @JavascriptInterface
        fun eventHandler(evenBody: String) {
            // TODO: convert evenBody to the object VirtusizeEvent
            Log.d(Constants.LOG_TAG, "event: $evenBody")
            if (evenBody.contains("user-closed-widget")) {
                dismiss()
            }
        }
    }
}