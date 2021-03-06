package com.virtusize.libsource

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi

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
    private var _webViewClient: WebViewClient? = null

    override fun setWebChromeClient(client: WebChromeClient?) {
        _webChromeClient = client
    }

    override fun setWebViewClient(client: WebViewClient) {
        _webViewClient = client
    }

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true

        super.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return _webViewClient?.shouldOverrideUrlLoading(view, url) ?: false
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return _webViewClient?.shouldOverrideUrlLoading(view, request) ?: false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                _webViewClient?.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                view?.evaluateJavascript("javascript:window.virtusizeSNSEnabled = true;", null)
                _webViewClient?.onPageFinished(view, url)
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                _webViewClient?.onLoadResource(view, url)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                _webViewClient?.onPageCommitVisible(view, url)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                url: String?
            ): WebResourceResponse? {
                return _webViewClient?.shouldInterceptRequest(view, url)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return _webViewClient?.shouldInterceptRequest(view, request)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                _webViewClient?.onReceivedError(view, errorCode, description, failingUrl)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                _webViewClient?.onReceivedError(view, request, error)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                _webViewClient?.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onFormResubmission(
                view: WebView?,
                dontResend: Message?,
                resend: Message?
            ) {
                _webViewClient?.onFormResubmission(view, dontResend, resend)
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                _webViewClient?.doUpdateVisitedHistory(view, url, isReload)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                _webViewClient?.onReceivedSslError(view, handler, error)
            }

            override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
                _webViewClient?.onReceivedClientCertRequest(view, request)
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler?,
                host: String?,
                realm: String?
            ) {
                _webViewClient?.onReceivedHttpAuthRequest(view, handler, host, realm)
            }

            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                return _webViewClient?.shouldOverrideKeyEvent(view, event) ?: false
            }

            override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
                _webViewClient?.onUnhandledKeyEvent(view, event)
            }

            override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
                _webViewClient?.onScaleChanged(view, oldScale, newScale)
            }

            override fun onReceivedLoginRequest(
                view: WebView?,
                realm: String?,
                account: String?,
                args: String?
            ) {
                _webViewClient?.onReceivedLoginRequest(view, realm, account, args)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                return _webViewClient?.onRenderProcessGone(view, detail) ?: false
            }

            @RequiresApi(Build.VERSION_CODES.O_MR1)
            override fun onSafeBrowsingHit(
                view: WebView?,
                request: WebResourceRequest?,
                threatType: Int,
                callback: SafeBrowsingResponse?
            ) {
                _webViewClient?.onSafeBrowsingHit(view, request, threatType, callback)
            }
        })

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
                if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport && isLinkFromVirtusize(url, title)) {
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