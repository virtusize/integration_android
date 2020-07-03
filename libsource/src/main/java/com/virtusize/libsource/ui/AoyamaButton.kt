package com.virtusize.libsource.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.libsource.Constants
import com.virtusize.libsource.R
import com.virtusize.libsource.VirtusizeButtonSetupHandler
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.local.VirtusizeEvents
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.throwError

class AoyamaButton(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    VirtusizeButtonSetupHandler {

    // VirtusizeProduct associated with the AoyamaButton instance
    var virtusizeProduct: VirtusizeProduct? = null

    // Receives Virtusize messages
    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler

    // The Aoyama view that opens when the button is clicked
    private val aoyamaDialogFragment = AoyamaView()


    init {
        visibility = View.INVISIBLE

        LayoutInflater.from(context).inflate(R.layout.button_aoyama, this, true)

        setOnClickListener {
            val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            val previousFragment = context.supportFragmentManager.findFragmentByTag(Constants.AOYAMA_FRAG_TAG)
            previousFragment?.let {fragment ->
                fragmentTransaction.remove(fragment)
            }
            fragmentTransaction.addToBackStack(null)
            aoyamaDialogFragment.show(fragmentTransaction, Constants.AOYAMA_FRAG_TAG)
        }
    }

    /**
     * Sets up the button with the corresponding VirtusizeProduct
     * @param product the VirtusizeProduct that is set for this button
     * @see VirtusizeProduct
     */
    internal fun setup(product: VirtusizeProduct, messageHandler: VirtusizeMessageHandler) {
        virtusizeProduct = product
        virtusizeMessageHandler = messageHandler
        aoyamaDialogFragment.setupMessageHandler(messageHandler, this)
    }

    /**
     * Dismisses/closes the Aoyama Window
     */
    fun dismissAoyamaView() {
        if (aoyamaDialogFragment.isVisible)
            aoyamaDialogFragment.dismiss()
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
                        val previousFragment = (context as AppCompatActivity).supportFragmentManager.findFragmentByTag(Constants.AOYAMA_FRAG_TAG)
                        previousFragment?.let {fragment ->
                            fragmentTransaction.remove(fragment)
                        }
                        fragmentTransaction.addToBackStack(null)
                        aoyamaDialogFragment.show(fragmentTransaction, Constants.AOYAMA_FRAG_TAG)
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