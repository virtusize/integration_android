package com.virtusize.libsource.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.virtusize.libsource.Constants
import com.virtusize.libsource.R
import kotlinx.android.synthetic.main.web_activity.*

class FitIllustratorDialog: DialogFragment() {

    private var url = "http://www.virtusize.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.web_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                web_view.loadUrl(
                    "javascript:(function() { " +
                            "var element = document.getElementsByClassName('global-close')[0];"
                            + "element.onclick = function() { ${Constants.JSBridgeName}.userClosedWidget(); };" +
                            "})()")
            }
        }
        web_view.addJavascriptInterface(JavaScriptInterface(), Constants.JSBridgeName)
        arguments?.getString(Constants.URL_KEY)?.let {
            url = it
        }
        web_view.loadUrl(url)
    }

    private inner class JavaScriptInterface {

        @JavascriptInterface
        fun userClosedWidget() {
            dismiss()
        }
    }
}