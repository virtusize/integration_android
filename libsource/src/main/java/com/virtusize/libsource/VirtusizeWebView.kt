package com.virtusize.libsource

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
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
                // Obtain the popup window link or link title
                val message = view.handler.obtainMessage()
                view.requestFocusNodeHref(message)
                val url = message.data.getString("url")
                val title = message.data.getString("title")
                Log.d(TAG, "onCreateWindow ${message.data}")
                if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport && isLinkFromVirtusize(url, title)) {
                    Log.d(TAG, "Add the popup to the current web view")
                    val popupWebView = WebView(view.context)
                    popupWebView.settings.javaScriptEnabled = true
                    popupWebView.settings.javaScriptCanOpenWindowsAutomatically = true
                    popupWebView.settings.setSupportMultipleWindows(true)
                    // For the 403 error with Google Sign-In
                    popupWebView.settings.userAgentString = System.getProperty("http.agent")
                    popupWebView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            if (isExternalLinkFromVirtusize(url)) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try {
                                    context.startActivity(intent)
                                } finally {
                                    return true
                                }
                            }
                            return false
                        }
                    }
                    popupWebView.webChromeClient = object : WebChromeClient() {
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

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                _webChromeClient?.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                _webChromeClient?.onReceivedTitle(view, title)
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                _webChromeClient?.onReceivedIcon(view, icon)
            }

            override fun onReceivedTouchIconUrl(
                view: WebView?,
                url: String?,
                precomposed: Boolean
            ) {
                _webChromeClient?.onReceivedTouchIconUrl(view, url, precomposed)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                _webChromeClient?.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                _webChromeClient?.onHideCustomView()
            }

            override fun onRequestFocus(view: WebView?) {
                _webChromeClient?.onRequestFocus(view)
            }

            override fun onCloseWindow(window: WebView?) {
                _webChromeClient?.onCloseWindow(window)
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                return _webChromeClient?.onJsAlert(view, url, message, result) ?: false
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                return _webChromeClient?.onJsConfirm(view, url, message, result) ?: false
            }

            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                return _webChromeClient?.onJsPrompt(view, url, message, defaultValue, result)
                    ?: false
            }

            override fun onJsBeforeUnload(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                return _webChromeClient?.onJsBeforeUnload(view, url, message, result) ?: false
            }

            override fun onExceededDatabaseQuota(
                url: String?,
                databaseIdentifier: String?,
                quota: Long,
                estimatedDatabaseSize: Long,
                totalQuota: Long,
                quotaUpdater: WebStorage.QuotaUpdater?
            ) {
                _webChromeClient?.onExceededDatabaseQuota(
                    url,
                    databaseIdentifier,
                    quota,
                    estimatedDatabaseSize,
                    totalQuota,
                    quotaUpdater
                )
            }

            override fun onReachedMaxAppCacheSize(
                requiredStorage: Long,
                quota: Long,
                quotaUpdater: WebStorage.QuotaUpdater?
            ) {
                _webChromeClient?.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                _webChromeClient?.onGeolocationPermissionsShowPrompt(origin, callback)
            }

            override fun onGeolocationPermissionsHidePrompt() {
                _webChromeClient?.onGeolocationPermissionsHidePrompt()
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                _webChromeClient?.onPermissionRequest(request)
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                _webChromeClient?.onPermissionRequestCanceled(request)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                return _webChromeClient?.onConsoleMessage(consoleMessage) ?: false
            }

            override fun onJsTimeout(): Boolean {
                return _webChromeClient?.onJsTimeout() ?: true
            }

            override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
                _webChromeClient?.onConsoleMessage(message, lineNumber, sourceID)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                return _webChromeClient?.onShowFileChooser(
                    webView,
                    filePathCallback,
                    fileChooserParams
                ) ?: false
            }

            override fun onShowCustomView(
                view: View?,
                requestedOrientation: Int,
                callback: CustomViewCallback?
            ) {
                _webChromeClient?.onShowCustomView(view, requestedOrientation, callback)
            }

            override fun getDefaultVideoPoster(): Bitmap? {
                return _webChromeClient?.defaultVideoPoster
            }

            override fun getVideoLoadingProgressView(): View? {
                return _webChromeClient?.videoLoadingProgressView
            }

            override fun getVisitedHistory(callback: ValueCallback<Array<String>>?) {
                _webChromeClient?.getVisitedHistory(callback)
            }
        })
    }

    /**
     * Checks if the URL is a Virtusize external link to be opened with a browser app
     */
    private fun isExternalLinkFromVirtusize(url: String?): Boolean {
        return (url?.contains("virtusize") == true && url.contains("privacy")) ||
                url?.contains("surveymonkey") == true
    }

    /**
     * Checks if the URL or the link title is from Virtusize
     */
    private fun isLinkFromVirtusize(url: String?, title: String?): Boolean {
        return isExternalLinkFromVirtusize(url) ||
                /* Facebook Auth link title */ title?.contains("Facebook") == true ||
                /* Google Auth link title */ title?.contains("Google") == true
    }
}