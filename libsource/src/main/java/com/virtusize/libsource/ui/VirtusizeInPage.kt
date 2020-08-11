package com.virtusize.libsource.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import com.virtusize.libsource.data.local.VirtusizeParams
import com.virtusize.libsource.data.remote.ProductCheck

class VirtusizeInPage(context: Context, attrs: AttributeSet) : VirtusizeRelativeLayoutView(context, attrs) {
    override var virtusizeParams: VirtusizeParams? = null

    // Receives Virtusize messages
    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler

    // The Virtusize view that opens when the InPage view is clicked
    private val virtusizeDialogFragment = VirtusizeWebView()

    init {
        visibility = View.INVISIBLE
    }

    override fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
        virtusizeDialogFragment.setupMessageHandler(messageHandler, this)
    }

    override fun setupProductCheckResponseData(productCheck: ProductCheck) {
        // TODO
    }

    override fun dismissVirtusizeView() {
        if (virtusizeDialogFragment.isVisible) {
            virtusizeDialogFragment.dismiss()
        }
    }
}