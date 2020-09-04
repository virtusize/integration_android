package com.virtusize.libsource.ui

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.util.FontUtils
import kotlinx.android.synthetic.main.view_inpage_mini.view.*

class VirtusizeInPageMini(context: Context, attrs: AttributeSet) : VirtusizeInPageView(context, attrs) {

    override var virtusizeParams: VirtusizeParams? = null
        private set

    override lateinit var virtusizeMessageHandler: VirtusizeMessageHandler
        private set

    override var virtusizeDialogFragment = VirtusizeWebView()
        private set

    // The VirtusizeViewStyle that clients can choose to use for this InPage Mini view
    var virtusizeViewStyle: VirtusizeViewStyle = VirtusizeViewStyle.NONE
        set(value) {
            field = value
            setupStyle()
        }

    // The background color for this InPage Mini view
    var virtusizeBackgroundColor = 0
        private set

    private var configuredContext: ContextWrapper? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_inpage_mini, this, true)
        visibility = View.INVISIBLE
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeInPageMini, 0, 0)
        val buttonStyle = attrsArray.getInt(R.styleable.VirtusizeInPageMini_virtusizeInPageMiniStyle, VirtusizeViewStyle.NONE.value)
        virtusizeViewStyle = VirtusizeViewStyle.values().firstOrNull { it.value == buttonStyle } ?: VirtusizeViewStyle.NONE
        virtusizeBackgroundColor = attrsArray.getColor(R.styleable.VirtusizeInPageMini_inPageMiniBackgroundColor, 0)
        attrsArray.recycle()
        setupStyle()
    }

    override fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler) {
        super.setup(params, messageHandler)
        virtusizeParams = params
        virtusizeMessageHandler = messageHandler
    }

    override fun setupProductCheckResponseData(productCheck: ProductCheck) {
        if (virtusizeParams?.virtusizeProduct != null) {
            virtusizeParams?.virtusizeProduct!!.productCheckData = productCheck
            productCheck.data?.let { productCheckResponseData ->
                if (productCheckResponseData.validProduct) {
                    visibility = View.VISIBLE
                    setupTextsConfiguredLocalization()
                    setLoadingScreen(true)
                    setOnClickListener {
                        clickVirtusizeView(context)
                    }
                    inpage_mini_button.setOnClickListener {
                        clickVirtusizeView(context)
                    }
                }
            }
        } else {
            virtusizeMessageHandler.onError(this, VirtusizeErrorType.NullProduct.virtusizeError())
            throwError(VirtusizeErrorType.NullProduct)
        }
    }

    private fun setLoadingScreen(loading: Boolean) {
        if(loading) {
            inpage_mini_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.virtusizeWhite))
        } else {
            inpage_mini_layout.setBackgroundColor(virtusizeBackgroundColor)
        }
        FontUtils.setTypeFace(
            context,
            inpage_mini_loading_text,
            virtusizeParams?.language,
            if (loading) FontUtils.FontType.BOLD else FontUtils.FontType.REGULAR
        )
        inpage_mini_image_view.visibility = if(loading) View.VISIBLE else View.GONE
        inpage_mini_text.visibility = if(loading) View.GONE else View.VISIBLE
        inpage_mini_loading_text.visibility = if(loading) View.VISIBLE else View.GONE
        inpage_mini_loading_text.startAnimation()
        inpage_mini_button.visibility = if(loading) View.GONE else View.VISIBLE
    }

    fun setInPageMiniBackgroundColor(@ColorInt color: Int) {
        virtusizeBackgroundColor = color
        setupStyle()
    }

    /**
     * Sets up the InPage Mini Style corresponding to [VirtusizeViewStyle]
     */
    private fun setupStyle() {
        if(virtusizeBackgroundColor != 0) {
            inpage_mini_layout.setBackgroundColor(virtusizeBackgroundColor)
            inpage_mini_button.setTextColor(virtusizeBackgroundColor)
            setButtonRightArrowColor(virtusizeBackgroundColor)
        } else if(virtusizeViewStyle == VirtusizeViewStyle.TEAL) {
            inpage_mini_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.virtusizeTeal))
            inpage_mini_button.setTextColor(ContextCompat.getColor(context, R.color.virtusizeTeal))
            setButtonRightArrowColor(ContextCompat.getColor(context, R.color.virtusizeTeal))
        }
    }

    private fun setButtonRightArrowColor(color: Int) {
        var drawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_right_black)
        drawable = DrawableCompat.wrap(drawable!!)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        DrawableCompat.setTint(drawable, color)

        inpage_mini_button.setCompoundDrawables(null, null, drawable, null)
    }

    private fun setupTextsConfiguredLocalization() {
        FontUtils.setTypeFaces(
            context,
            mutableListOf(
                inpage_mini_text,
                inpage_mini_button
            ), virtusizeParams?.language, FontUtils.FontType.REGULAR
        )
        configuredContext = getConfiguredContext(context)
        inpage_mini_button.text = configuredContext?.getText(R.string.virtusize_button_text)
        inpage_mini_loading_text.text = configuredContext?.getText(R.string.inpage_mini_loading_text)
        setConfiguredDimensions()
    }

    private fun setConfiguredDimensions() {
        configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_mini_message_textSize)?.let {
            inpage_mini_loading_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
            inpage_mini_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
        }
        configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_default_textSize)?.let {
            inpage_mini_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
        }
    }

    override fun setupRecommendationText(text: String) {
        inpage_mini_text.text = text
        setLoadingScreen(false)
    }

    override fun showErrorScreen() {
        inpage_mini_loading_text.visibility = View.GONE
        inpage_mini_text.visibility = View.VISIBLE
        inpage_mini_text.text = configuredContext?.getText(R.string.inpage_short_error_text)
        inpage_mini_text.setTextColor(ContextCompat.getColor(context, R.color.color_gray_700))
        inpage_mini_image_view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error_hanger))
        setOnClickListener {}
    }
}