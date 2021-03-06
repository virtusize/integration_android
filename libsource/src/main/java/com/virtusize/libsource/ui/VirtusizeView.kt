package com.virtusize.libsource.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.util.Constants

/**
 * An interface for the Virtusize specific views such as VirtusizeButton and VirtusizeInPageView
 */
interface VirtusizeView {
    // The parameter object to be passed to the Virtusize web app
    val virtusizeParams: VirtusizeParams?
    // Receives Virtusize messages
    val virtusizeMessageHandler: VirtusizeMessageHandler
    // The Virtusize view that opens when the view is clicked
    val virtusizeDialogFragment: VirtusizeWebViewFragment

    /**
     * Sets up the VirtusizeView with the corresponding VirtusizeParams
     * @param params the VirtusizeParams that is set for this VirtusizeView
     * @param messageHandler pass VirtusizeMessageHandler to listen to any Virtusize-related messages
     * @see VirtusizeParams
     */
    fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        virtusizeDialogFragment.setupMessageHandler(messageHandler, this)
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
        virtusizeMessageHandler.onEvent(VirtusizeEvent(VirtusizeEvents.UserOpenedWidget.getEventName()))
        val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        val previousFragment = context.supportFragmentManager.findFragmentByTag(Constants.FRAG_TAG)
        previousFragment?.let {fragment ->
            fragmentTransaction.remove(fragment)
        }
        fragmentTransaction.addToBackStack(null)
        val args = Bundle()
        args.putString(Constants.URL_KEY, VirtusizeApi.virtusizeWebViewURL())
        virtusizeParams?.let {
            args.putString(Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY, "javascript:vsParamsFromSDK(${it.vsParamsString()})")
        }
        virtusizeDialogFragment.arguments = args
        virtusizeDialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
    }
}