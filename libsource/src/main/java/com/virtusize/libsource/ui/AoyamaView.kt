package com.virtusize.libsource.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import com.virtusize.libsource.Constants
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import kotlinx.android.synthetic.main.web_activity.*

class AoyamaView: DialogFragment() {

    private val url = "https://static.api.virtusize.jp/a/aoyama/testing/sdk-integration/sdk-webview.html"

    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
    private lateinit var aoyamaButton: AoyamaButton

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
        web_view.settings.javaScriptEnabled = true
        web_view.settings.domStorageEnabled = true
        web_view.settings.databaseEnabled = true
        // Set up the web view client that adds a JavaScript script for the click listener to close the button
        web_view.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val vsParamsFromSDKScript = "javascript:vsParamsFromSDK({" +
                        "apiKey: '52140a6c9e0870294e4c5df4ebc39b47c8237bfa', " +
                        "externalProductId: '18575990709', " +
                        "env: 'staging', " +
                        "language: 'en'})"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    view?.evaluateJavascript(vsParamsFromSDKScript, null)
                } else {
                    view?.loadUrl(vsParamsFromSDKScript)
                }
            }
        }
        // Add the Javascript interface to interface the web app with the web view
        web_view.addJavascriptInterface(WebAppInterface(), Constants.JSBridgeName)
        web_view.loadUrl(url)
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
            Log.d(Constants.LOG_TAG, evenBody)
        }
    }
}