package com.virtusize.libsource.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import com.virtusize.libsource.data.local.VirtusizeParams
import com.virtusize.libsource.data.remote.ProductCheck

class VirtusizeInPage(context: Context, attrs: AttributeSet) : VirtusizeRelativeLayoutView(context, attrs) {

    override var virtusizeParams: VirtusizeParams? = null
        private set

    override lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
        private set

    override var virtusizeDialogFragment = VirtusizeWebView()
        private set

    init {
        visibility = View.INVISIBLE
    }

    override fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        super.setup(params, messageHandler)
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
    }

    override fun setupProductCheckResponseData(productCheck: ProductCheck) {
        // TODO
    }

    override fun setupRecommendationText(text: String) {
        TODO("Not yet implemented")
    }

    override fun dismissVirtusizeView() {
        if (virtusizeDialogFragment.isVisible) {
            virtusizeDialogFragment.dismiss()
        }
    }
}