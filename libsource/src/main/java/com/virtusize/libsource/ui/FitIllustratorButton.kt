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

class FitIllustratorButton(context: Context, attrs: AttributeSet): LinearLayout(context, attrs),
    VirtusizeButtonSetupHandler {

    var virtusizeProduct: VirtusizeProduct? = null
    private val dialogFragment = FitIllustratorDialog()

    init {
        View.inflate(context, R.layout.fit_illustrator_button, this)
        fit_button.visibility = View.INVISIBLE
    }

    fun setup(product: VirtusizeProduct) {
        virtusizeProduct = product
    }

    fun applyStyle(buttonStyle: VirtusizeButtonStyle) {
        when(buttonStyle) {
            VirtusizeButtonStyle.DEFAULT_STYLE -> {
                fit_button.setBackgroundColor(resources.getColor(R.color.virtusizeBlack))
                fit_button.setTextColor(resources.getColor(R.color.vritusizeWhite))
                fit_button.setText(R.string.fit_button_text)
            }
        }
    }

    fun dismiss() {
        if (dialogFragment.isVisible)
            dialogFragment.dismiss()
    }

    override fun setupProduct(productData: ProductCheckResponse) {
        if (virtusizeProduct != null) {
            virtusizeProduct!!.data = productData
            if (productData.data?.validProduct != null && productData.data.validProduct) {
                fit_button.visibility = View.VISIBLE
                fit_button.setOnClickListener {
                    val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                    val previousFragment = (context as AppCompatActivity).supportFragmentManager.findFragmentByTag(Constants.FRAG_TAG)
                    if (previousFragment != null) {
                        fragmentTransaction.remove(previousFragment)
                    }
                    fragmentTransaction.addToBackStack(null)
                    val args = Bundle()
		            args.putString(Constants.URL_KEY, VirtusizeApi.fitIllustrator(virtusizeProduct!!))
                    dialogFragment.arguments = args
                    dialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
                }
            }
        }
        else {
            throwError(VirtusizeError.InvalidProduct)
        }
    }
}