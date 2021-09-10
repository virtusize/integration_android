package com.virtusize.android

import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.fragment.app.DialogFragment
import com.virtusize.libsource.VirtusizeWebView
import com.virtusize.libsource.util.urlString

class WebViewFragment: DialogFragment() {

    companion object {
        private val TAG = WebViewFragment::class.simpleName
    }

    private lateinit var webView: VirtusizeWebView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.VirtusizeDialogFragmentAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                Log.d(TAG, "shouldOverrideUrlLoading ${url}")
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.let {
                    Log.d(TAG, "shouldOverrideUrlLoading ${request.urlString}")
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d(TAG, "onPageFinished ${view?.url}")
                super.onPageFinished(view, url)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                Log.d(TAG, "onCreateWindow ${view?.url}")
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
            }

            override fun onCloseWindow(window: WebView?) {
                Log.d(TAG, "onCloseWindow")
            }
        }

        webView.loadUrl("https://virtusize-jp-demo.s3-ap-northeast-1.amazonaws.com/sns-auth-test/index.html")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.stopLoading()
        webView.destroy()
    }
}