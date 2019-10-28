package com.virtusize.libsource.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.virtusize.libsource.Constants
import com.virtusize.libsource.VirtusizeButtonSetupHandler
import com.virtusize.libsource.data.VirtusizeApi
import com.virtusize.libsource.data.pojo.ProductCheckResponse
import com.virtusize.libsource.model.VirtusizeError
import com.virtusize.libsource.model.VirtusizeEvents
import com.virtusize.libsource.model.VirtusizeMessageHandler
import com.virtusize.libsource.model.VirtusizeProduct
import com.virtusize.libsource.throwError

/**
 * This class is the custom view FitIllustratorButton that is added in the client's layout file.
 */
class FitIllustratorButton(context: Context, attrs: AttributeSet): Button(context, attrs),
    VirtusizeButtonSetupHandler {

    /**
     * VirtusizeProduct associated with FitIllustratorButton instance
     */
    var virtusizeProduct: VirtusizeProduct? = null
    /**
     * Fit Illustrator view that opens when button is clicked
     */
    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
    private val fitIllustratorDialogFragment = FitIllustratorView()

    init {
        visibility = View.INVISIBLE
    }

    /**
     * This method is used to set up button with the corresponding VirtusizeProduct
     * @param product This is the VirtusizeProduct that is set for this button
     * @see VirtusizeProduct
     */
    internal fun setup(product: VirtusizeProduct, messageHandler: VirtusizeMessageHandler) {
        virtusizeProduct = product
        virtusizeMessageHandler = messageHandler
        fitIllustratorDialogFragment.setupMessageHandler(messageHandler, this)
    }

    /**
     * This method is used to dismiss/close the Fit Illustrator Window
     */
    fun dismissFitIllustratorView() {
        if (fitIllustratorDialogFragment.isVisible)
            fitIllustratorDialogFragment.dismiss()
    }

    /**
     * This method is used to set up product check data received from server to virtusizeProduct
     * @param productCheckResponse ProductCheckResponse received from Virtusize server
     * @see ProductCheckResponse
     * @throws VirtusizeError.InvalidProduct error
     */
    override fun setupProductCheckResponseData(productCheckResponse: ProductCheckResponse) {
        if (virtusizeProduct != null) {
            virtusizeProduct!!.productCheckData = productCheckResponse
            if (productCheckResponse.data.validProduct) {
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
        else {
            virtusizeMessageHandler.onError(this, VirtusizeError.InvalidProduct)
            throwError(VirtusizeError.InvalidProduct)
        }
    }
}