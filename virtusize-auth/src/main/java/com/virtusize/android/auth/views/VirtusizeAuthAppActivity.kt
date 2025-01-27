package com.virtusize.android.auth.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.EXTRA_NAME_AUTH_URL
import androidx.activity.result.contract.ActivityResultContracts
import com.virtusize.android.auth.R
import com.virtusize.android.auth.VirtusizeAuth
import com.virtusize.android.auth.VirtusizeWebView
import kotlin.time.Duration.Companion.seconds

class VirtusizeAuthAppActivity : AppCompatActivity() {

    private lateinit var webView: VirtusizeWebView
    private val virtusizeSNSAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        VirtusizeAuth.handleVirtusizeSNSAuthResult(webView, result.resultCode, result.data)
        Handler(Looper.getMainLooper())
            .postDelayed(
                {
                    finish()
                },
                // Delay dismissing the activity to ensure the SNS authentication is completed
                3.seconds.inWholeMilliseconds
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_virtusize_auth_app)
        val authUrl = intent.getStringExtra(EXTRA_NAME_AUTH_URL)
        if (authUrl == null) {
            finish()
            return
        }
        webView = findViewById(R.id.web_view)
        webView.setVirtusizeSNSAuthLauncher(virtusizeSNSAuthLauncher)
        webView.loadUrl(authUrl)
    }
}
