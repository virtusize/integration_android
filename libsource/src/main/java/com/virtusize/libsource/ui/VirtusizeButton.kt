package com.virtusize.libsource.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.util.VirtusizeUtils

class VirtusizeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VirtusizeView, AppCompatButton(context, attrs, defStyleAttr) {

    /**
     * @see VirtusizeView.virtusizeParams
     */
    override var virtusizeParams: VirtusizeParams? = null
        private set

    /**
     * @see VirtusizeView.virtusizeMessageHandler
     */
    override lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
        private set

    /**
     * @see VirtusizeView.virtusizeDialogFragment
     */
    override var virtusizeDialogFragment = VirtusizeWebViewFragment()
        private set

    // The VirtusizeViewStyle that clients can choose to use for this Button
    var virtusizeViewStyle: VirtusizeViewStyle = VirtusizeViewStyle.NONE
        set(value) {
            field = value
            setupButtonStyle()
        }

    init {
        visibility = if (visibility == View.GONE) {
            View.GONE
        } else {
            View.INVISIBLE
        }
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeButton, 0, 0)
        val buttonStyle = attrsArray.getInt(
            R.styleable.VirtusizeButton_virtusizeButtonStyle,
            VirtusizeViewStyle.NONE.value
        )
        virtusizeViewStyle = VirtusizeViewStyle.values().firstOrNull { it.value == buttonStyle }
            ?: VirtusizeViewStyle.NONE
        attrsArray.recycle()
        setupButtonStyle()
    }

    /**
     * @see VirtusizeView.setup
     */
    override fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        super.setup(params, messageHandler)
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
    }

    /**
     * Sets up the Virtusize Button Style corresponding to [VirtusizeViewStyle]
     */
    private fun setupButtonStyle() {
        if (virtusizeViewStyle == VirtusizeViewStyle.NONE) {
            return
        }

        includeFontPadding = false
        isAllCaps = false
        minHeight = 0
        minWidth = 0
        minimumWidth = 0
        minimumHeight = resources.getDimension(R.dimen.virtusize_button_corner_radius).toInt()

        if (virtusizeViewStyle == VirtusizeViewStyle.TEAL) {
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
        val drawableWidth =
            resources.getDimension(R.dimen.virtusize_button_logo_default_width).toInt()
        val drawableHeight =
            resources.getDimension(R.dimen.virtusize_button_logo_default_height).toInt()
        drawable?.setBounds(0, 0, drawableWidth, drawableHeight)
        setCompoundDrawables(drawable, null, null, null)
        compoundDrawablePadding =
            resources.getDimension(R.dimen.virtusize_button_text_marginStart).toInt()

        val horizontalPadding =
            resources.getDimension(R.dimen.virtusize_button_horizontal_padding).toInt()
        val verticalPadding =
            resources.getDimension(R.dimen.virtusize_button_vertical_padding).toInt()
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }

    /**
     * @see VirtusizeView.setupProductCheckResponseData
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
                        openVirtusizeWebView(context)
                    }
                }
            }
        } else {
            VirtusizeErrorType.NullProduct.throwError()
        }
    }

    /**
     * Sets up the button text style based on the language that clients set using the [VirtusizeBuilder] in the application
     */
    private fun setupButtonTextConfiguredLocalization() {
        val configuredContext =
            VirtusizeUtils.getConfiguredContext(context, virtusizeParams?.language)
        if (text.isNullOrEmpty()) {
            text = configuredContext?.getText(R.string.virtusize_button_text)
            configuredContext?.resources?.getDimension(R.dimen.virtusize_button_textSize)?.let {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
            }
        }
    }
}