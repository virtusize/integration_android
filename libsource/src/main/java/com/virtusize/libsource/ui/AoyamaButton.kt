package com.virtusize.libsource.ui

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.virtusize.libsource.*
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.local.VirtusizeEvents
import com.virtusize.libsource.data.local.VirtusizeMessageHandler
import com.virtusize.libsource.data.local.VirtusizeProduct
import com.virtusize.libsource.data.local.aoyama.AoyamaParams
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.network.VirtusizeApi

class AoyamaButton(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    VirtusizeButtonSetupHandler {

    // TODO
    internal var aoyamaParams: AoyamaParams? = null

    // Receives Virtusize messages
    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler

    // The Aoyama view that opens when the button is clicked
    private val aoyamaDialogFragment = AoyamaView()

    init {
        visibility = View.INVISIBLE
        LayoutInflater.from(context).inflate(R.layout.button_aoyama, this, true)
    }

    /**
     * Sets up the button with the corresponding VirtusizeProduct
     * @param product the VirtusizeProduct that is set for this button
     * @see VirtusizeProduct
     */
    internal fun setup(params: AoyamaParams, messageHandler: VirtusizeMessageHandler) {
        aoyamaParams = params
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
        if (aoyamaParams?.virtusizeProduct != null) {
            aoyamaParams?.virtusizeProduct!!.productCheckData = productCheck
            productCheck.data?.let { productCheckResponseData ->
                if (productCheckResponseData.validProduct) {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        virtusizeMessageHandler.onEvent(this@AoyamaButton, VirtusizeEvents.UserOpenedWidget)
                        val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                        val previousFragment = (context as AppCompatActivity).supportFragmentManager.findFragmentByTag(Constants.AOYAMA_FRAG_TAG)
                        previousFragment?.let {fragment ->
                            fragmentTransaction.remove(fragment)
                        }
                        fragmentTransaction.addToBackStack(null)
                        val args = Bundle()
                        args.putString(Constants.URL_KEY, VirtusizeApi.aoyama())
                        aoyamaParams?.let {
                            args.putString(Constants.AOYAMA_PARAMS_SCRIPT_KEY, "javascript:vsParamsFromSDK(${it.getVsParamsString()})")
                        }
                        aoyamaDialogFragment.arguments = args
                        aoyamaDialogFragment.show(fragmentTransaction, Constants.AOYAMA_FRAG_TAG)
                    }
                }
            }
        } else {
            virtusizeMessageHandler.onError(this, VirtusizeError.InvalidProduct)
            throwError(VirtusizeError.InvalidProduct)
        }
    }
}