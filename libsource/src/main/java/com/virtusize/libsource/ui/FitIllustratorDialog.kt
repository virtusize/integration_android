package com.virtusize.libsource.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import com.virtusize.libsource.Constants
import com.virtusize.libsource.R
import com.virtusize.libsource.model.VirtusizeError
import com.virtusize.libsource.throwError
import kotlinx.android.synthetic.main.web_activity.*

class FitIllustratorDialog: DialogFragment() {

    var url = "http://www.virtusize.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle);
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
                            + "element.onclick = function() { vs.userClosedWidget(); };" +
                            "})()");
            }
        }
        web_view.addJavascriptInterface(JavaScriptInterface(), "vs")
        arguments?.getString(Constants.URL_KEY)?.let {
            url = it
        }
        web_view.loadUrl(url)
        splash_logo.visibility = View.GONE
    }

    private inner class JavaScriptInterface {

        @JavascriptInterface
        fun userClosedWidget() {
            dismiss()
        }
    }
}