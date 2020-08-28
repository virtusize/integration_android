package com.virtusize.libsource.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.data.local.throwError

class VirtusizeButton(context: Context, attrs: AttributeSet) : VirtusizeView, AppCompatButton(context, attrs) {

    override var virtusizeParams: VirtusizeParams? = null
        private set

    override lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
        private set

    override var virtusizeDialogFragment = VirtusizeWebView()
        private set

    // The VirtusizeViewStyle that clients can choose to use for this Button
    var virtusizeViewStyle: VirtusizeViewStyle = VirtusizeViewStyle.NONE
        set(value) {
            field = value
            setupButtonStyle()
        }

    init {
        visibility = View.INVISIBLE
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeButton, 0, 0)
        val buttonStyle = attrsArray.getInt(R.styleable.VirtusizeButton_virtusizeButtonStyle, VirtusizeViewStyle.NONE.value)
        virtusizeViewStyle = VirtusizeViewStyle.values().firstOrNull { it.value == buttonStyle } ?: VirtusizeViewStyle.NONE
        attrsArray.recycle()
        setupButtonStyle()
    }

    override fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        super.setup(params, messageHandler)
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
    }

    /**
     * Sets up the Virtusize Button Style corresponding to [VirtusizeViewStyle]
     */
    private fun setupButtonStyle() {
        includeFontPadding = false
        isAllCaps = false

        if(virtusizeViewStyle == VirtusizeViewStyle.TEAL) {
            setBackgroundResource(R.drawable.button_background_teal)
        } else {
            setBackgroundResource(R.drawable.button_background_black)
        }

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
                    setupButtonTextConfiguredLocalization()
                    setOnClickListener {
                        clickVirtusizeView(context)
                    }
                }
            }
        } else {
            virtusizeMessageHandler.onError(this, VirtusizeErrorType.NullProduct.virtusizeError())
            throwError(VirtusizeErrorType.NullProduct)
        }
    }

    private fun setupButtonTextConfiguredLocalization() {
        val configuredContext = getConfiguredContext(context)
        text = configuredContext?.getText(R.string.virtusize_button_text)
        configuredContext?.resources?.getDimension(R.dimen.virtusize_button_textSize)
    }
}