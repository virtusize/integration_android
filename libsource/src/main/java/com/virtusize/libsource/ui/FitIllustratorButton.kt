package com.virtusize.libsource.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.virtusize.libsource.Constants
import com.virtusize.libsource.R
import com.virtusize.libsource.VirtusizeButtonSetupHandler
import com.virtusize.libsource.data.VirtusizeApi
import com.virtusize.libsource.data.pojo.ProductCheckResponse
import com.virtusize.libsource.model.VirtusizeButtonStyle
import com.virtusize.libsource.model.VirtusizeError
import com.virtusize.libsource.model.VirtusizeProduct
import com.virtusize.libsource.throwError
import kotlinx.android.synthetic.main.fit_illustrator_button.view.*

/**
 * This class is the custom view FitIllustratorButton that is added in the client's layout file.
 */
class FitIllustratorButton(context: Context, attrs: AttributeSet): LinearLayout(context, attrs),
    VirtusizeButtonSetupHandler {

    /**
     * VirtusizeProduct associated with FitIllustratorButton instance
     */
    var virtusizeProduct: VirtusizeProduct? = null
    /**
     * Fit Illustrator view that opens when button is clicked
     */
    private val fitIllustratorDialogFragment = FitIllustratorView()

    init {
        View.inflate(context, R.layout.fit_illustrator_button, this)
        fit_button.visibility = View.INVISIBLE
    }

    /**
     * This method is used to set up button with the corresponding VirtusizeProduct
     * @param product This is the VirtusizeProduct that is set for this button
     * @see VirtusizeProduct
     */
    internal fun setup(product: VirtusizeProduct) {
        virtusizeProduct = product
    }

    /**
     * This method is used to apply custom style to FitIllustratorButton
     * @param buttonStyle The type of styles available, are in VirtusizeButtonStyle
     * @see VirtusizeButtonStyle
     */
    fun applyStyle(buttonStyle: VirtusizeButtonStyle) {
        when(buttonStyle) {
            VirtusizeButtonStyle.DEFAULT_STYLE -> {
                fit_button.setBackgroundColor(resources.getColor(R.color.virtusizeBlack))
                fit_button.setTextColor(resources.getColor(R.color.vritusizeWhite))
                fit_button.setText(R.string.fit_button_text)
            }
        }
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
                fit_button.visibility = View.VISIBLE
                fit_button.setOnClickListener {
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
            throwError(VirtusizeError.InvalidProduct)
        }
    }
}