package com.virtusize.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import androidx.annotation.RequiresApi
import com.virtusize.android.ui.VirtusizeFitIllustratorFragment
import com.virtusize.android.util.isFitIllustratorURL
import com.virtusize.android.util.urlString

@SuppressLint("SetJavaScriptEnabled")
class VirtusizeFitIllustratorWebView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : WebView(context, attrs, defStyleAttr) {
        private var internalWebChromeClient: WebChromeClient? = null
        private var internalWebViewClient: WebViewClient? = null

        override fun setWebChromeClient(client: WebChromeClient?) {
            internalWebChromeClient = client
        }

        override fun setWebViewClient(client: WebViewClient) {
            internalWebViewClient = client
        }

        internal val isMultipleWindowsSupported: Boolean
            get() = settings.supportMultipleWindows()

        internal fun enableWindowsSettings() {
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
        }

        init {
            isFocusable = true
            isFocusableInTouchMode = true

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true

            super.setWebViewClient(
                object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        url: String?,
                    ): Boolean {
                        url?.let {
                            if (!isMultipleWindowsSupported && url.isFitIllustratorURL) {
                                VirtusizeFitIllustratorFragment.launch(context, url)
                                return true
                            }
                        }
                        return internalWebViewClient?.shouldOverrideUrlLoading(view, url) ?: false
                    }

                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        request?.let {
                            if (!isMultipleWindowsSupported && request.isFitIllustratorURL) {
                                VirtusizeFitIllustratorFragment.launch(context, request.urlString)
                                return true
                            }
                        }
                        return internalWebViewClient?.shouldOverrideUrlLoading(
                            view,
                            request,
                        ) ?: false
                    }

                    override fun onPageStarted(
                        view: WebView?,
                        url: String?,
                        favicon: Bitmap?,
                    ) {
                        internalWebViewClient?.onPageStarted(view, url, favicon)
                    }

                    override fun onPageFinished(
                        view: WebView?,
                        url: String?,
                    ) {
                        // This is to close any subviews when a user is successfully logged into Facebook
                        if (
                            isMultipleWindowsSupported &&
                            url?.contains("virtusize") == true &&
                            url.contains("#compare")
                        ) {
                            view?.removeAllViews()
                        }
                        internalWebViewClient?.onPageFinished(view, url)
                    }

                    override fun onLoadResource(
                        view: WebView?,
                        url: String?,
                    ) {
                        internalWebViewClient?.onLoadResource(view, url)
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onPageCommitVisible(
                        view: WebView?,
                        url: String?,
                    ) {
                        internalWebViewClient?.onPageCommitVisible(view, url)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        url: String?,
                    ): WebResourceResponse? =
                        internalWebViewClient?.shouldInterceptRequest(view, url)

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): WebResourceResponse? =
                        internalWebViewClient?.shouldInterceptRequest(view, request)

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?,
                    ) {
                        internalWebViewClient?.onReceivedError(
                            view,
                            errorCode,
                            description,
                            failingUrl,
                        )
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        internalWebViewClient?.onReceivedError(view, request, error)
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?,
                    ) {
                        internalWebViewClient?.onReceivedHttpError(view, request, errorResponse)
                    }

                    override fun onFormResubmission(
                        view: WebView?,
                        dontResend: Message?,
                        resend: Message?,
                    ) {
                        internalWebViewClient?.onFormResubmission(view, dontResend, resend)
                    }

                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String?,
                        isReload: Boolean,
                    ) {
                        internalWebViewClient?.doUpdateVisitedHistory(view, url, isReload)
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?,
                    ) {
                        internalWebViewClient?.onReceivedSslError(view, handler, error)
                    }

                    override fun onReceivedClientCertRequest(
                        view: WebView?,
                        request: ClientCertRequest?,
                    ) {
                        internalWebViewClient?.onReceivedClientCertRequest(view, request)
                    }

                    override fun onReceivedHttpAuthRequest(
                        view: WebView?,
                        handler: HttpAuthHandler?,
                        host: String?,
                        realm: String?,
                    ) {
                        internalWebViewClient?.onReceivedHttpAuthRequest(view, handler, host, realm)
                    }

                    override fun shouldOverrideKeyEvent(
                        view: WebView?,
                        event: KeyEvent?,
                    ): Boolean = internalWebViewClient?.shouldOverrideKeyEvent(view, event) ?: false

                    override fun onUnhandledKeyEvent(
                        view: WebView?,
                        event: KeyEvent?,
                    ) {
                        internalWebViewClient?.onUnhandledKeyEvent(view, event)
                    }

                    override fun onScaleChanged(
                        view: WebView?,
                        oldScale: Float,
                        newScale: Float,
                    ) {
                        internalWebViewClient?.onScaleChanged(view, oldScale, newScale)
                    }

                    override fun onReceivedLoginRequest(
                        view: WebView?,
                        realm: String?,
                        account: String?,
                        args: String?,
                    ) {
                        internalWebViewClient?.onReceivedLoginRequest(view, realm, account, args)
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: RenderProcessGoneDetail?,
                    ): Boolean = internalWebViewClient?.onRenderProcessGone(view, detail) ?: false

                    @RequiresApi(Build.VERSION_CODES.O_MR1)
                    override fun onSafeBrowsingHit(
                        view: WebView?,
                        request: WebResourceRequest?,
                        threatType: Int,
                        callback: SafeBrowsingResponse?,
                    ) {
                        internalWebViewClient?.onSafeBrowsingHit(
                            view,
                            request,
                            threatType,
                            callback,
                        )
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
                        if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport) {
                            val popupWebView = VirtusizeFitIllustratorWebView(view.context)
                            popupWebView.enableWindowsSettings()
                            popupWebView.layoutParams =
                                LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                            popupWebView.webViewClient =
                                object : WebViewClient() {
                                    override fun onPageFinished(
                                        view: WebView?,
                                        url: String?,
                                    ) {
                                        // This is to scroll the webview to top when the fit illustrator is open
                                        url?.let {
                                            scrollTo(0, 0)
                                        }
                                    }
                                }

                            popupWebView.webChromeClient =
                                object : WebChromeClient() {
                                    override fun onCloseWindow(window: WebView) {
                                        removeAllViews()
                                    }
                                }
                            popupWebView.setOnKeyListener { v, keyCode, event ->
                                if (keyCode == KeyEvent.KEYCODE_BACK &&
                                    event.action == MotionEvent.ACTION_UP &&
                                    popupWebView.canGoBack()
                                ) {
                                    popupWebView.goBack()
                                    return@setOnKeyListener true
                                }
                                false
                            }
                            addView(popupWebView)
                            val transport = resultMsg.obj as WebView.WebViewTransport
                            transport.webView = popupWebView
                            resultMsg.sendToTarget()
                            return true
                        }
                        return internalWebChromeClient?.onCreateWindow(
                            view,
                            dialog,
                            userGesture,
                            resultMsg,
                        )
                            ?: false
                    }

                    override fun onProgressChanged(
                        view: WebView?,
                        newProgress: Int,
                    ) {
                        internalWebChromeClient?.onProgressChanged(view, newProgress)
                    }

                    override fun onReceivedTitle(
                        view: WebView?,
                        title: String?,
                    ) {
                        internalWebChromeClient?.onReceivedTitle(view, title)
                    }

                    override fun onReceivedIcon(
                        view: WebView?,
                        icon: Bitmap?,
                    ) {
                        internalWebChromeClient?.onReceivedIcon(view, icon)
                    }

                    override fun onReceivedTouchIconUrl(
                        view: WebView?,
                        url: String?,
                        precomposed: Boolean,
                    ) {
                        internalWebChromeClient?.onReceivedTouchIconUrl(view, url, precomposed)
                    }

                    override fun onShowCustomView(
                        view: View?,
                        callback: CustomViewCallback?,
                    ) {
                        internalWebChromeClient?.onShowCustomView(view, callback)
                    }

                    override fun onHideCustomView() {
                        internalWebChromeClient?.onHideCustomView()
                    }

                    override fun onRequestFocus(view: WebView?) {
                        internalWebChromeClient?.onRequestFocus(view)
                    }

                    override fun onCloseWindow(window: WebView?) {
                        window?.removeAllViews()
                        internalWebChromeClient?.onCloseWindow(window)
                    }

                    override fun onJsAlert(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?,
                    ): Boolean =
                        internalWebChromeClient?.onJsAlert(view, url, message, result) ?: false

                    override fun onJsConfirm(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?,
                    ): Boolean =
                        internalWebChromeClient?.onJsConfirm(view, url, message, result) ?: false

                    override fun onJsPrompt(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        defaultValue: String?,
                        result: JsPromptResult?,
                    ): Boolean =
                        internalWebChromeClient?.onJsPrompt(
                            view,
                            url,
                            message,
                            defaultValue,
                            result,
                        )
                            ?: false

                    override fun onJsBeforeUnload(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: JsResult?,
                    ): Boolean =
                        internalWebChromeClient?.onJsBeforeUnload(
                            view,
                            url,
                            message,
                            result,
                        ) ?: false

                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?,
                        callback: GeolocationPermissions.Callback?,
                    ) {
                        internalWebChromeClient?.onGeolocationPermissionsShowPrompt(
                            origin,
                            callback,
                        )
                    }

                    override fun onGeolocationPermissionsHidePrompt() {
                        internalWebChromeClient?.onGeolocationPermissionsHidePrompt()
                    }

                    override fun onPermissionRequest(request: PermissionRequest?) {
                        internalWebChromeClient?.onPermissionRequest(request)
                    }

                    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                        internalWebChromeClient?.onPermissionRequestCanceled(request)
                    }

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean =
                        internalWebChromeClient?.onConsoleMessage(consoleMessage) ?: false

                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?,
                    ): Boolean =
                        internalWebChromeClient?.onShowFileChooser(
                            webView,
                            filePathCallback,
                            fileChooserParams,
                        ) ?: false

                    override fun getDefaultVideoPoster(): Bitmap? =
                        internalWebChromeClient?.defaultVideoPoster

                    override fun getVideoLoadingProgressView(): View? =
                        internalWebChromeClient?.videoLoadingProgressView

                    override fun getVisitedHistory(callback: ValueCallback<Array<String>>?) {
                        internalWebChromeClient?.getVisitedHistory(callback)
                    }
                },
            )
        }
    }
