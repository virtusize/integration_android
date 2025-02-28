package com.virtusize.sampleappkotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.android.auth.VirtusizeAuth
import com.virtusize.android.auth.VirtusizeWebView
import com.virtusize.android.auth.utils.VirtusizeURLCheck
import com.virtusize.android.auth.utils.withBranch

class WebAppActivity : AppCompatActivity() {
    // Method 1: Use the VirtusizeWebView
    private lateinit var webView: VirtusizeWebView
    // Method 2: Use WebView
    // private lateinit var webView: WebView

    private val virtusizeSNSAuthLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            VirtusizeAuth.handleVirtusizeSNSAuthResult(webView, result.resultCode, result.data)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_app)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webView = findViewById(R.id.web_view)

        // Method 1: Use the VirtusizeWebView
        webView.setVirtusizeSNSAuthLauncher(virtusizeSNSAuthLauncher)
        // Method 2: Use WebView
        // webViewImplementation()
        val uri =
            Uri.parse("https://demo.virtusize.com")
                .withBranch("staging")
        webView.loadUrl(uri.toString())
    }

    /*
     * This is the method 2 to enable Virtusize SNS login
     */
    private fun webViewImplementation() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.setSupportMultipleWindows(true)

        webView.webViewClient =
            object : WebViewClient() {
                override fun onPageFinished(
                    view: WebView?,
                    url: String?,
                ) {
                    // Enable SNS buttons in Virtusize
                    webView.evaluateJavascript("javascript:window.virtusizeSNSEnabled = true;", null)
                }
            }

        webView.webChromeClient =
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
                    if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport &&
                        VirtusizeURLCheck.isLinkFromVirtusize(
                            url,
                            title,
                        )
                    ) {
                        val popupWebView = WebView(view.context)
                        popupWebView.settings.javaScriptEnabled = true
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
                                            startActivity(intent)
                                            return true
                                        }
                                    }
                                    return VirtusizeAuth.isSNSAuthUrl(baseContext, virtusizeSNSAuthLauncher, url)
                                }
                            }
                        popupWebView.webChromeClient =
                            object : WebChromeClient() {
                                override fun onCloseWindow(window: WebView) {
                                    webView.removeAllViews()
                                }
                            }
                        val transport = resultMsg.obj as WebView.WebViewTransport
                        view.addView(popupWebView)
                        transport.webView = popupWebView
                        resultMsg.sendToTarget()
                        return true
                    }
                    return super.onCreateWindow(view, dialog, userGesture, resultMsg)
                }
            }
    }

    // Handle the up button click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate back to the parent activity
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
