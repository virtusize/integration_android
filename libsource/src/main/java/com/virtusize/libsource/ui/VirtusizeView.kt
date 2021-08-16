package com.virtusize.libsource.ui

import android.content.Context
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.util.VirtusizeUtils

/**
 * An interface for the Virtusize specific views such as VirtusizeButton and VirtusizeInPageView
 */
interface VirtusizeView {
    // The product which is bounded with this Virtusize
    var clientProduct: VirtusizeProduct?
    // The parameter object to be passed to the Virtusize web app
    var virtusizeParams: VirtusizeParams
    // Receives Virtusize messages
    var virtusizeMessageHandler: VirtusizeMessageHandler
    // The Virtusize view that opens when the view is clicked
    var virtusizeDialogFragment: VirtusizeWebViewFragment

    /**
     * Initial setup for this VirtusizeView
     * @param product the [VirtusizeProduct] set by a client
     * @param params the [VirtusizeParams] that is set for this VirtusizeView
     * @param messageHandler pass [VirtusizeMessageHandler] to listen to any Virtusize-related messages
     */
    fun initialSetup(
        product: VirtusizeProduct,
        params: VirtusizeParams,
        messageHandler: VirtusizeMessageHandler
    ) {
        clientProduct = product
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
        virtusizeDialogFragment = VirtusizeWebViewFragment()
    }

    /**
     * Sets the product with the product data check response to this Virtusize view
     * @param productWithPDC the [VirtusizeProduct] with the product data check response received from Virtusize API
     */
    fun setProductWithProductDataCheck(productWithPDC: VirtusizeProduct) {
        if (clientProduct == null) {
            virtusizeMessageHandler.onError(VirtusizeErrorType.NullProduct.virtusizeError())
            VirtusizeErrorType.NullProduct.throwError()
        }
    }

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
    fun openVirtusizeWebView(context: Context, product: VirtusizeProduct) {
        VirtusizeUtils.openVirtusizeWebView(
            context,
            virtusizeParams,
            virtusizeDialogFragment,
            product,
            virtusizeMessageHandler
        )
    }
}