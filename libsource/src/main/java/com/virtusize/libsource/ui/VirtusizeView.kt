package com.virtusize.libsource.ui

import android.content.Context
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.util.VirtusizeUtils

/**
 * An interface for the Virtusize specific views such as VirtusizeButton and VirtusizeInPageView
 */
interface VirtusizeView {
    // The product which is bounded with this Virtusize
    var clientProduct: VirtusizeProduct
    // The parameter object to be passed to the Virtusize web app
    var virtusizeParams: VirtusizeParams
    // Receives Virtusize messages
    var virtusizeMessageHandler: VirtusizeMessageHandler
    // The Virtusize view that opens when the view is clicked
    var virtusizeDialogFragment: VirtusizeWebViewFragment

    /**
     * Sets up the VirtusizeView with the corresponding VirtusizeParams
     * @param product // TODO
     * @param params the VirtusizeParams that is set for this VirtusizeView
     * @param messageHandler pass VirtusizeMessageHandler to listen to any Virtusize-related messages
     * @see VirtusizeParams
     */
    fun initialSetup(product: VirtusizeProduct, params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        clientProduct = product
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
        virtusizeDialogFragment = VirtusizeWebViewFragment()
        virtusizeDialogFragment.setupMessageHandler(virtusizeMessageHandler)
    }

    /**
     * Sets up the product check data received from Virtusize API to VirtusizeProduct
     * @param productCheck ProductCheckResponse received from Virtusize API
     * @see ProductCheck
     */
    fun setupProductCheckResponseData(productCheck: ProductCheck)

    /**
     * Dismisses/closes the Virtusize Window
     */
    fun dismissVirtusizeView() {
        if (virtusizeDialogFragment.isVisible) {
            virtusizeDialogFragment.dismiss()
        }
    }

    /**
     * A clickable function to open the Virtusize WebView
     */
    fun openVirtusizeWebView(context: Context) {
        VirtusizeUtils.openVirtusizeWebView(context, virtusizeParams, virtusizeDialogFragment)
    }
}