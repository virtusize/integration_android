package com.virtusize.libsource.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.virtusize.libsource.util.Constants
import com.virtusize.libsource.R
import com.virtusize.libsource.VirtusizeButtonSetupHandler
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.network.VirtusizeApi
import com.virtusize.libsource.data.local.throwError

class VirtusizeButton(context: Context, attrs: AttributeSet) :
    VirtusizeButtonView(context, attrs),
    VirtusizeButtonSetupHandler {

    // The parameter object to be passed to the Virtusize web app
    override var virtusizeParams: VirtusizeParams? = null

    // Receives Virtusize messages
    private lateinit var virtusizeMessageHandler: VirtusizeMessageHandler

    // The Virtusize view that opens when the button is clicked
    private val virtusizeDialogFragment = VirtusizeWebView()

    // The VirtusizeButtonStyle that clients can choose to use
    var buttonStyle: VirtusizeButtonStyle = VirtusizeButtonStyle.NONE
        set(value) {
            field = value
            updateButtonStyle(field)
        }

    init {
        visibility = View.INVISIBLE
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeButton, 0, 0)
        val buttonStyle = attrsArray.getInt(R.styleable.VirtusizeButton_virtusizeButtonStyle, VirtusizeButtonStyle.NONE.value)
        if(buttonStyle == VirtusizeButtonStyle.DEFAULT_STYLE.value) {
            this.buttonStyle = VirtusizeButtonStyle.DEFAULT_STYLE
        }
        attrsArray.recycle()
    }

    /**
     * Updates the Virtusize Button Style corresponding to [VirtusizeButtonStyle]
     * @param [VirtusizeButtonStyle]
     */
    private fun updateButtonStyle(virtusizeButtonStyle: VirtusizeButtonStyle?) {
        if(virtusizeButtonStyle == VirtusizeButtonStyle.DEFAULT_STYLE) {
            setVirtusizeDefaultStyle()
        }
        invalidate()
    }

    /**
     * Sets up the default Virtusize button style
     */
    private fun setVirtusizeDefaultStyle() {
        setBackgroundResource(R.drawable.button_background_black)

        setText(R.string.virtusize_button_text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(R.style.VirtusizeButtonTextAppearance)
        } else {
            setTextAppearance(context, R.style.VirtusizeButtonTextAppearance)
        }

        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_vs_icon_white)
        val drawableWidth = resources.getDimension(R.dimen.virtusize_button_logo_default_width).toInt()
        val drawableHeight = resources.getDimension(R.dimen.virtusize_button_logo_default_height).toInt()
        drawable?.setBounds(0, 0, drawableWidth, drawableHeight)
        setCompoundDrawables(drawable, null, null, null)
        compoundDrawablePadding = resources.getDimension(R.dimen.virtusize_button_text_marginStart).toInt()

        val horizontalPadding = resources.getDimension(R.dimen.virtusize_button_horizontal_padding).toInt()
        val verticalPadding = resources.getDimension(R.dimen.virtusize_button_vertical_padding).toInt()
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }

    /**
     * Sets up the button with the corresponding VirtusizeProduct
     * @param product the VirtusizeProduct that is set for this button
     * @see VirtusizeProduct
     */
    override fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
        virtusizeDialogFragment.setupMessageHandler(messageHandler, this)
    }

    /**
     * Dismisses/closes the Virtusize Window
     */
    override fun dismissVirtusizeView() {
        if (virtusizeDialogFragment.isVisible) {
            virtusizeDialogFragment.dismiss()
        }
    }

    /**
     * Sets up the product check data received from Virtusize API to VirtusizeProduct
     * @param productCheck ProductCheckResponse received from Virtusize API
     * @see ProductCheck
     * @throws VirtusizeErrorType.NullProduct error
     */
    override fun setupProductCheckResponseData(productCheck: ProductCheck) {
        if (virtusizeParams?.virtusizeProduct != null) {
            virtusizeParams?.virtusizeProduct!!.productCheckData = productCheck
            productCheck.data?.let { productCheckResponseData ->
                if (productCheckResponseData.validProduct) {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        virtusizeMessageHandler.onEvent(this, VirtusizeEvent(VirtusizeEvents.UserOpenedWidget.getEventName()))
                        val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                        val previousFragment = (context as AppCompatActivity).supportFragmentManager.findFragmentByTag(Constants.FRAG_TAG)
                        previousFragment?.let {fragment ->
                            fragmentTransaction.remove(fragment)
                        }
                        fragmentTransaction.addToBackStack(null)
                        val args = Bundle()
                        args.putString(Constants.URL_KEY, VirtusizeApi.virtusizeURL())
                        virtusizeParams?.let {
                            args.putString(Constants.VIRTUSIZE_PARAMS_SCRIPT_KEY, "javascript:vsParamsFromSDK(${it.vsParamsString()})")
                        }
                        virtusizeDialogFragment.arguments = args
                        virtusizeDialogFragment.show(fragmentTransaction, Constants.FRAG_TAG)
                    }
                }
            }
        } else {
            virtusizeMessageHandler.onError(this, VirtusizeErrorType.NullProduct.virtusizeError())
            throwError(VirtusizeErrorType.NullProduct)
        }
    }
}