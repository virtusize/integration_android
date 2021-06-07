package com.virtusize.libsource

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.webkit.*

@SuppressLint("SetJavaScriptEnabled")
class VirtusizeWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = VirtusizeWebView::class.simpleName
    }

    private var _webChromeClient: WebChromeClient? = null

    override fun setWebChromeClient(client: WebChromeClient?) {
        _webChromeClient = client
    }

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true

        super.setWebChromeClient(object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                dialog: Boolean,
                userGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val href = view.handler.obtainMessage()
                view.requestFocusNodeHref(href)
                val url = href.data.getString("url")
                val title = href.data.getString("title")
                Log.d(TAG, "onCreateWindow ${href.data}")
                if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport && isVirtusizeLink(url, title)) {
                    Log.d(TAG, "Add the popup to the current web view")
                    val popupWebView = WebView(view.context)
                    popupWebView.settings.javaScriptEnabled = true
                    popupWebView.settings.javaScriptCanOpenWindowsAutomatically = true
                    popupWebView.settings.setSupportMultipleWindows(true)
                    // For the 403 error with Google Sign-In
                    popupWebView.settings.userAgentString = System.getProperty("http.agent")
                    popupWebView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            try {
                                view.context?.startActivity(intent)
                            } finally {
                                return true
                            }
                        }
                    }
                    popupWebView.webChromeClient = object : WebChromeClient(){
                        override fun onCloseWindow(window: WebView) {
                            window.removeAllViews()
                        }
                    }
                    val transport = resultMsg.obj as WebView.WebViewTransport
                    view.addView(popupWebView)
                    transport.webView = popupWebView
                    resultMsg.sendToTarget()
                    return true
                }
                return _webChromeClient?.onCreateWindow(view, dialog, userGesture, resultMsg) ?: false
            }
        })
    }

    private fun isVirtusizeLink(url: String?, title: String?): Boolean {
        return (url?.contains("virtusize") == true && url.contains("privacy")) ||
                (url?.contains("surveymonkey") == true && url.contains("survey")) ||
                title?.contains("Facebook") == true ||
                title?.contains("Google") == true
    }
}