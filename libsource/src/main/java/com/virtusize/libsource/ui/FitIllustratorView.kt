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
import com.virtusize.libsource.model.VirtusizeMessageHandler
import kotlinx.android.synthetic.main.web_activity.*

/**
 * This class represents the Fit Illustrator Window
 */
class FitIllustratorView: DialogFragment() {

    private var url = "http://www.virtusize.com"

    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
    private lateinit var fitIllustratorButton: FitIllustratorButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets style to dialog to show it as full screen
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.web_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Enable javascript in web view
        web_view.settings.javaScriptEnabled = true
        // Add web view client that adds script for click listener to close button in javascript widget
        web_view.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl(
                    "javascript:(function() { " +
                            "var element = document.getElementsByClassName('global-close')[0];"
                            + "element.onclick = function() { ${Constants.JSBridgeName}.userClosedWidget(); };" +
                            "})()")
            }
        }
        // Add Javascript interface to receive events from web view
        web_view.addJavascriptInterface(JavaScriptInterface(), Constants.JSBridgeName)
        // Get Fit Illustrator URL passed in fragment arguments
        arguments?.getString(Constants.URL_KEY)?.let {
            url = it
        }
        web_view.loadUrl(url)
    }

    /**
     * This method sets up message handler
     */
    internal fun setupMessageHandler(messageHandler: VirtusizeMessageHandler, fitIllustratorButton: FitIllustratorButton) {
        virtusizeMessageHandler = messageHandler
        this.fitIllustratorButton = fitIllustratorButton
    }

    /**
     * Javascript interface to receive events from web view
     */
    private inner class JavaScriptInterface {

        /**
         * This method is called when usr clicks on close button in fit illustrator view
         */
        @JavascriptInterface
        fun userClosedWidget() {
            virtusizeMessageHandler.virtusizeControllerShouldClose(fitIllustratorButton)
            dismiss()
        }
    }
}