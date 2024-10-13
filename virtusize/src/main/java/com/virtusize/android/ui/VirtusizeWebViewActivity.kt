package com.virtusize.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.android.R
import com.virtusize.android.data.local.VirtusizeWebViewInMemoryCache
import com.virtusize.android.util.Constants

internal class VirtusizeWebViewActivity : AppCompatActivity(R.layout.activity_virtusize_webview) {
    private val virtusizeDialogFragment: VirtusizeWebViewFragment by lazy {
        VirtusizeWebViewFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_virtusize_webview)
        if (savedInstanceState == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val previousFragment = supportFragmentManager.findFragmentByTag(Constants.FRAG_TAG)
            previousFragment?.let { fragment ->
                fragmentTransaction.remove(fragment)
            }
            fragmentTransaction.addToBackStack(null)
            val extras = intent.extras
            virtusizeDialogFragment.arguments = extras
            VirtusizeWebViewInMemoryCache.getMessageHandler()?.let { messageHandler ->
                virtusizeDialogFragment.setupMessageHandler(messageHandler)
            }
            virtusizeDialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
        }
    }
}
