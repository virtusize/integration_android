package com.virtusize.sampleappkotlin

import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.virtusize.android.auth.VirtusizeAuth
import com.virtusize.android.auth.VirtusizeWebView
import com.virtusize.android.util.urlString

class WebViewFragment: DialogFragment() {

    companion object {
        private val TAG = WebViewFragment::class.simpleName
    }

    private lateinit var webView: VirtusizeWebView

    // 1. Register for getting the result of the activity
    private val virtusizeSNSAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        // 3. Handle the SNS auth result by passing the webview and the result to the `VirtusizeAuth.handleVirtusizeSNSAuthResult` function
        VirtusizeAuth.handleVirtusizeSNSAuthResult(webView, result.resultCode, result.data)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = com.virtusize.android.R.style.VirtusizeDialogFragmentAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.virtusize.android.R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.webview_vs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        // 2. Pass the activity launcher to the VirtusizeWebView
        webView.setVirtusizeSNSAuthLauncher(virtusizeSNSAuthLauncher)

        object : WebViewClient() {
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
        }.also { webView.webViewClient = it }

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

        webView.loadUrl("https://demo.virtusize.com/")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.stopLoading()
        webView.destroy()
    }
}