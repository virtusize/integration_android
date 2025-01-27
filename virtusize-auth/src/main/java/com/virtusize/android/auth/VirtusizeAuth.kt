package com.virtusize.android.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.Keep
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.NATIVE_APP_ID_KEY
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.EXTRA_NAME_AUTH_URL
import com.virtusize.android.auth.utils.VirtusizeAuthConstants.EXTRA_NAME_SNS_SCRIPT
import com.virtusize.android.auth.utils.isVirtusizeAuthAppURL
import com.virtusize.android.auth.utils.isVirtusizeSNSAuthURL
import com.virtusize.android.auth.views.VirtusizeAuthAppActivity
import com.virtusize.android.auth.views.VitrusizeAuthActivity

@Keep
object VirtusizeAuth {
    /**
     * Returns true if the url is a redirect url from the SNS auth flow.
     *
     * @param context The context from an activity or a fragment to start the authorization process.
     * @param activityResultLauncher The activity result launcher to launch the VitrusizeAuthActivity.
     * @param url The authorization url.
     */
    fun isSNSAuthUrl(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>?,
        url: String
    ): Boolean = when {
        url.isVirtusizeSNSAuthURL() -> {
            Intent(context, VitrusizeAuthActivity::class.java).apply {
                putExtra(EXTRA_NAME_AUTH_URL, url)
                putExtra(NATIVE_APP_ID_KEY, context.packageName)
            }.let { intent ->
                activityResultLauncher?.launch(intent)
            }
            true
        }
        url.isVirtusizeAuthAppURL() -> {
            Intent(context, VirtusizeAuthAppActivity::class.java).apply {
                putExtra(EXTRA_NAME_AUTH_URL, url)
            }.let { intent ->
                activityResultLauncher?.launch(intent)
            }
            true
        }
        else -> false
    }

    /**
     * Handles the result of the SNS auth flow.
     *
     * @param view The webview to handle the result.
     * @param resultCode The result code passed from the VirtusizeAuthActivity.
     * @param data The data passed from the VirtusizeAuthActivity.
     */
    fun handleVirtusizeSNSAuthResult(view: WebView, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(EXTRA_NAME_SNS_SCRIPT)?.let { snsScript ->
                view.evaluateJavascript(snsScript, null)
            }
        }
    }
}