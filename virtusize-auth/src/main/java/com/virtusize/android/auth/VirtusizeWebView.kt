package com.virtusize.android.auth

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
import android.webkit.ClientCertRequest
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.HttpAuthHandler
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.virtusize.android.auth.utils.VirtusizeURLCheck

@Keep
@SuppressLint("SetJavaScriptEnabled")
class VirtusizeWebView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : WebView(context, attrs, defStyleAttr) {
        private var webChromeClient: WebChromeClient? = null
        private var webViewClient: WebViewClient? = null

        private var launcher: ActivityResultLauncher<Intent>? = null

        override fun setWebChromeClient(client: WebChromeClient?) {
            webChromeClient = client
        }

        override fun setWebViewClient(client: WebViewClient) {
            webViewClient = client
        }

        init {
            isFocusable = true
            isFocusableInTouchMode = true

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
            // Required for Google SDK to work in WebView, see https://stackoverflow.com/a/73152331
            settings.userAgentString = "Mozilla/5.0 AppleWebKit/605.1.15 Mobile/15E148 Safari/604.1"

            super.setWebViewClient(
                object : WebViewClient() {
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        url: String?,
                    ): Boolean {
                        return webViewClient?.shouldOverrideUrlLoading(view, url) ?: false
                    }

                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        return webViewClient?.shouldOverrideUrlLoading(view, request) ?: false
                    }

                    override fun onPageStarted(
                        view: WebView?,
                        url: String?,
                        favicon: Bitmap?,
                    ) {
                        webViewClient?.onPageStarted(view, url, favicon)
                    }

                    override fun onPageFinished(
                        view: WebView?,
                        url: String?,
                    ) {
                        view?.evaluateJavascript("javascript:window.virtusizeSNSEnabled = true;", null)
                        webViewClient?.onPageFinished(view, url)
                    }

                    override fun onLoadResource(
                        view: WebView?,
                        url: String?,
                    ) {
                        webViewClient?.onLoadResource(view, url)
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onPageCommitVisible(
                        view: WebView?,
                        url: String?,
                    ) {
                        webViewClient?.onPageCommitVisible(view, url)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        url: String?,
                    ): WebResourceResponse? {
                        return webViewClient?.shouldInterceptRequest(view, url)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): WebResourceResponse? {
                        return webViewClient?.shouldInterceptRequest(view, request)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?,
                    ) {
                        webViewClient?.onReceivedError(view, errorCode, description, failingUrl)
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        webViewClient?.onReceivedError(view, request, error)
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?,
                    ) {
                        webViewClient?.onReceivedHttpError(view, request, errorResponse)
                    }

                    override fun onFormResubmission(
                        view: WebView?,
                        dontResend: Message?,
                        resend: Message?,
                    ) {
                        webViewClient?.onFormResubmission(view, dontResend, resend)
                    }

                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String?,
                        isReload: Boolean,
                    ) {
                        webViewClient?.doUpdateVisitedHistory(view, url, isReload)
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?,
                    ) {
                        webViewClient?.onReceivedSslError(view, handler, error)
                    }

                    override fun onReceivedClientCertRequest(
                        view: WebView?,
                        request: ClientCertRequest?,
                    ) {
                        webViewClient?.onReceivedClientCertRequest(view, request)
                    }

                    override fun onReceivedHttpAuthRequest(
                        view: WebView?,
                        handler: HttpAuthHandler?,
                        host: String?,
                        realm: String?,
                    ) {
                        webViewClient?.onReceivedHttpAuthRequest(view, handler, host, realm)
                    }

                    override fun shouldOverrideKeyEvent(
                        view: WebView?,
                        event: KeyEvent?,
                    ): Boolean {
                        return webViewClient?.shouldOverrideKeyEvent(view, event) ?: false
                    }

                    override fun onUnhandledKeyEvent(
                        view: WebView?,
                        event: KeyEvent?,
                    ) {
                        webViewClient?.onUnhandledKeyEvent(view, event)
                    }

                    override fun onScaleChanged(
                        view: WebView?,
                        oldScale: Float,
                        newScale: Float,
                    ) {
                        webViewClient?.onScaleChanged(view, oldScale, newScale)
                    }

                    override fun onReceivedLoginRequest(
                        view: WebView?,
                        realm: String?,
                        account: String?,
                        args: String?,
                    ) {
                        webViewClient?.onReceivedLoginRequest(view, realm, account, args)
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: RenderProcessGoneDetail?,
                    ): Boolean {
                        return webViewClient?.onRenderProcessGone(view, detail) ?: false
                    }

                    @RequiresApi(Build.VERSION_CODES.O_MR1)
                    override fun onSafeBrowsingHit(
                        view: WebView?,
                        request: WebResourceRequest?,
                        threatType: Int,
                        callback: SafeBrowsingResponse?,
                    ) {
                        webViewClient?.onSafeBrowsingHit(view, request, threatType, callback)
                    }
                },
            )

            super.setWebChromeClient(
                object : WebChromeClient() {
                    override fun onCreateWindow(
                        view: WebView,
                        dialog: Boolean,
                        userGesture: Boolean,
                        resultMsg: Message,
                    ): Boolean {
                        // Obtain the popup window link or link title
                        val message = view.handler.obtainMessage()
                        view.requestFocusNodeHref(message)
                        val url = message.data.getString("url")
                        val title = message.data.getString("title")
                        if (resultMsg.obj != null &&
                            resultMsg.obj is WebView.WebViewTransport &&
                            VirtusizeURLCheck.isLinkFromVirtusize(url, title)
                        ) {
                            val popupWebView = WebView(view.context)
                            popupWebView.settings.javaScriptEnabled = true
                            // For the 403 error with Google Sign-In
                            popupWebView.settings.userAgentString = System.getProperty("http.agent")
                            popupWebView.webViewClient =
                                object : WebViewClient() {
                                    @Deprecated("Deprecated in Java")
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView,
                                        url: String,
                                    ): Boolean {
                                        if (VirtusizeURLCheck.isExternalLinkFromVirtusize(url)) {
                                            runCatching {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                                return true
                                            }
                                        }
                                        return VirtusizeAuth.isSNSAuthUrl(context, launcher, url)
                                    }
                                }
                            popupWebView.webChromeClient =
                                object : WebChromeClient() {
                                    override fun onCloseWindow(window: WebView) {
                                        removeAllViews()
                                    }
                                }
                            val transport = resultMsg.obj as WebView.WebViewTransport
                            view.addView(popupWebView)
                            transport.webView = popupWebView
                            resultMsg.sendToTarget()
                            return true
                        }
                        return webChromeClient?.onCreateWindow(
                            view,
                            dialog,
                            userGesture,
                            resultMsg,
                        ) ?: false
                    }

                    override fun onProgressChanged(
                        view: WebView?,
                        newProgress: Int,
                    ) {
                        webChromeClient?.onProgressChanged(view, newProgress)
                    }

                    override fun onReceivedTitle(
                        view: WebView?,
                        title: String?,
                    ) {
                        webChromeClient?.onReceivedTitle(view, title)
                    }

                    override fun onReceivedIcon(
                        view: WebView?,
                        icon: Bitmap?,
                    ) {
                        webChromeClient?.onReceivedIcon(view, icon)
                    }

                    override fun onReceivedTouchIconUrl(
                        view: WebView?,
                        url: String?,
                        precomposed: Boolean,
                    ) {
                        webChromeClient?.onReceivedTouchIconUrl(view, url, precomposed)
                    }

                    override fun onShowCustomView(
                        view: View?,
                        callback: CustomViewCallback?,
                    ) {
                        webChromeClient?.onShowCustomView(view, callback)
                    }

                    override fun onHideCustomView() {
                        webChromeClient?.onHideCustomView()
                    }

                    override fun onRequestFocus(view: WebView?) {
                        webChromeClient?.onRequestFocus(view)
                    }

                    override fun onCloseWindow(window: WebView?) {
                        webChromeClient?.onCloseWindow(window)
                    }

                    override fun onJsAlert(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?,
                    ): Boolean {
                        return webChromeClient?.onJsAlert(view, url, message, result) ?: false
                    }

                    override fun onJsConfirm(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?,
                    ): Boolean {
                        return webChromeClient?.onJsConfirm(view, url, message, result) ?: false
                    }

                    override fun onJsPrompt(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        defaultValue: String?,
                        result: JsPromptResult?,
                    ): Boolean {
                        return webChromeClient?.onJsPrompt(view, url, message, defaultValue, result)
                            ?: false
                    }

                    override fun onJsBeforeUnload(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?,
                    ): Boolean {
                        return webChromeClient?.onJsBeforeUnload(view, url, message, result) ?: false
                    }

                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?,
                        callback: GeolocationPermissions.Callback?,
                    ) {
                        webChromeClient?.onGeolocationPermissionsShowPrompt(origin, callback)
                    }

                    override fun onGeolocationPermissionsHidePrompt() {
                        webChromeClient?.onGeolocationPermissionsHidePrompt()
                    }

                    override fun onPermissionRequest(request: PermissionRequest?) {
                        webChromeClient?.onPermissionRequest(request)
                    }

                    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                        webChromeClient?.onPermissionRequestCanceled(request)
                    }

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        return webChromeClient?.onConsoleMessage(consoleMessage) ?: false
                    }

                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?,
                    ): Boolean {
                        return webChromeClient?.onShowFileChooser(
                            webView,
                            filePathCallback,
                            fileChooserParams,
                        ) ?: false
                    }

                    override fun getDefaultVideoPoster(): Bitmap? {
                        return webChromeClient?.defaultVideoPoster
                    }

                    override fun getVideoLoadingProgressView(): View? {
                        return webChromeClient?.videoLoadingProgressView
                    }

                    override fun getVisitedHistory(callback: ValueCallback<Array<String>>?) {
                        webChromeClient?.getVisitedHistory(callback)
                    }
                },
            )
        }

        /**
         * Sets the activity result launcher for the Virtusize SNS auth flow.
         */
        fun setVirtusizeSNSAuthLauncher(launcher: ActivityResultLauncher<Intent>) {
            this.launcher = launcher
        }
    }
