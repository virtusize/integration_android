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
        // Enable JavaScript in the web view
        web_view.settings.javaScriptEnabled = true
        web_view.settings.domStorageEnabled = true
        web_view.settings.databaseEnabled = true
        // Set up the web view client that adds a JavaScript script for the click listener to close the button
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
        // Add the Javascript interface to receive events from the web view
        web_view.addJavascriptInterface(JavaScriptInterface(), Constants.JSBridgeName)
        // Get the Fit Illustrator URL passed in fragment arguments
        arguments?.getString(Constants.URL_KEY)?.let {
            url = it
        }
        web_view.loadUrl(url)
    }

    /**
     * Sets up a Virtusize message handler
     */
    internal fun setupMessageHandler(messageHandler: VirtusizeMessageHandler, fitIllustratorButton: FitIllustratorButton) {
        virtusizeMessageHandler = messageHandler
        this.fitIllustratorButton = fitIllustratorButton
    }

    /**
     * The Javascript interface to receive events from the web view
     */
    private inner class JavaScriptInterface {

        /**
         * This method is called when a user clicks on the close button in the Fit Illustrator window
         */
        @JavascriptInterface
        fun userClosedWidget() {
            virtusizeMessageHandler.virtusizeControllerShouldClose(fitIllustratorButton)
            dismiss()
        }
    }
}