package com.virtusize.libsource.ui

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.virtusize.libsource.Constants
import com.virtusize.libsource.VirtusizeButtonSetupHandler
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.local.VirtusizeEvents
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.throwError

/**
 * This class is the custom Fit Illustrator Button that is added in the client's layout file
 */
class FitIllustratorButton(context: Context, attrs: AttributeSet): AppCompatButton(context, attrs),
    VirtusizeButtonSetupHandler {

    // VirtusizeProduct associated with the FitIllustratorButton instance
    var virtusizeProduct: VirtusizeProduct? = null

    // Receives Virtusize messages
    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler

    // The Fit Illustrator view that opens when the button is clicked
    private val fitIllustratorDialogFragment = FitIllustratorView()

    init {
        visibility = View.INVISIBLE
    }

    /**
     * Sets up the button with the corresponding VirtusizeProduct
     * @param product the VirtusizeProduct that is set for this button
     * @see VirtusizeProduct
     */
    internal fun setup(product: VirtusizeProduct, messageHandler: VirtusizeMessageHandler) {
        virtusizeProduct = product
        virtusizeMessageHandler = messageHandler
        fitIllustratorDialogFragment.setupMessageHandler(messageHandler, this)
    }

    /**
     * Dismisses/closes the Fit Illustrator Window
     */
    fun dismissFitIllustratorView() {
        if (fitIllustratorDialogFragment.isVisible)
            fitIllustratorDialogFragment.dismiss()
    }

    /**
     * Sets up the product check data received from Virtusize API to VirtusizeProduct
     * @param productCheck ProductCheckResponse received from Virtusize API
     * @see ProductCheck
     * @throws VirtusizeError.InvalidProduct error
     */
    override fun setupProductCheckResponseData(productCheck: ProductCheck) {
        if (virtusizeProduct != null) {
            virtusizeProduct!!.productCheckData = productCheck
            productCheck.data?.let { productCheckResponseData ->
                if (productCheckResponseData.validProduct) {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        virtusizeMessageHandler.onEvent(this, VirtusizeEvents.UserOpenedWidget)
                        val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                        val previousFragment = (context as AppCompatActivity).supportFragmentManager.findFragmentByTag(Constants.FRAG_TAG)
                        previousFragment?.let {fragment ->
                            fragmentTransaction.remove(fragment)
                        }
                        fragmentTransaction.addToBackStack(null)
                        val args = Bundle()
                        args.putString(Constants.URL_KEY, VirtusizeApi.fitIllustrator(virtusizeProduct!!))
                        fitIllustratorDialogFragment.arguments = args
                        fitIllustratorDialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
                    }
                }
            }

        }
        else {
            virtusizeMessageHandler.onError(this, VirtusizeError.InvalidProduct)
            throwError(VirtusizeError.InvalidProduct)
        }
    }
}