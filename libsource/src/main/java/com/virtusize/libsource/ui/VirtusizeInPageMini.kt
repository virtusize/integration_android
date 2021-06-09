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
import com.virtusize.libsource.util.*
import com.virtusize.libsource.util.FontUtils
import com.virtusize.libsource.util.VirtusizeUtils
import kotlinx.android.synthetic.main.view_inpage_mini.view.*

class VirtusizeInPageMini(context: Context, attrs: AttributeSet) : VirtusizeInPageView(context, attrs) {

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

    // The VirtusizeViewStyle that clients can choose to use for this InPage Mini view
    var virtusizeViewStyle: VirtusizeViewStyle = VirtusizeViewStyle.NONE
        set(value) {
            field = value
            setStyle()
        }

    // The background color for this InPage Mini view
    var virtusizeBackgroundColor = 0
        private set

    // The configured context for localization
    private var configuredContext: ContextWrapper? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_inpage_mini, this, true)
        visibility = if (visibility == View.GONE) {
            View.GONE
        } else {
            View.INVISIBLE
        }
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeInPageMini, 0, 0)
        val buttonStyle = attrsArray.getInt(R.styleable.VirtusizeInPageMini_virtusizeInPageMiniStyle, VirtusizeViewStyle.NONE.value)
        virtusizeViewStyle = VirtusizeViewStyle.values().firstOrNull { it.value == buttonStyle } ?: VirtusizeViewStyle.NONE
        virtusizeBackgroundColor = attrsArray.getColor(R.styleable.VirtusizeInPageMini_inPageMiniBackgroundColor, 0)
        messageTextSize = attrsArray.getDimension(R.styleable.VirtusizeInPageMini_inPageMiniMessageTextSize, -1f)
        buttonTextSize = attrsArray.getDimension(R.styleable.VirtusizeInPageMini_inPageMiniButtonTextSize, -1f)
        attrsArray.recycle()
        setStyle()
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
     * @see VirtusizeView.setupProductCheckResponseData
     * @throws VirtusizeErrorType.NullProduct error
     */
    override fun setupProductCheckResponseData(productCheck: ProductCheck) {
        if (virtusizeParams?.virtusizeProduct != null) {
            virtusizeParams?.virtusizeProduct!!.productCheckData = productCheck
            productCheck.data?.let { productCheckResponseData ->
                if (productCheckResponseData.validProduct) {
                    visibility = View.VISIBLE
                    setupConfiguredLocalization()
                    setLoadingScreen(true)
                    setOnClickListener {
                        openVirtusizeWebView(context)
                    }
                    inpageMiniButton.setOnClickListener {
                        openVirtusizeWebView(context)
                    }
                }
            }
        } else {
            VirtusizeErrorType.NullProduct.throwError()
        }
    }

    /**
     * @see VirtusizeInPageView.setupRecommendationText
     */
    override fun setupRecommendationText(text: String) {
        inpageMiniText.text = text
        setLoadingScreen(false)
    }

    /**
     * @see VirtusizeInPageView.showErrorScreen
     */
    override fun showErrorScreen() {
        inpageMiniLoadingText.visibility = View.GONE
        inpageMiniText.visibility = View.VISIBLE
        inpageMiniText.text = configuredContext?.getText(R.string.inpage_short_error_text)
        inpageMiniText.setTextColor(ContextCompat.getColor(context, R.color.color_gray_700))
        inpageMiniImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error_hanger))
        setOnClickListener {}
    }

    /**
     * Sets up the background color of the InPage Mini
     * @param color a color int
     */
    fun setInPageMiniBackgroundColor(@ColorInt color: Int) {
        virtusizeBackgroundColor = color
        setStyle()
    }

    /**
     * Sets up the styles for the loading screen and the screen after finishing loading
     * @param loading pass true when it's loading, and pass false when finishing loading
     */
    private fun setLoadingScreen(loading: Boolean) {
        if(loading) {
            inpageMiniLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.virtusizeWhite))
            inpageMiniLoadingText.startAnimation()
        } else {
            inpageMiniLayout.setBackgroundColor(virtusizeBackgroundColor)
            inpageMiniLoadingText.stopAnimation()
        }
        FontUtils.setTypeFace(
            context,
            inpageMiniLoadingText,
            virtusizeParams?.language,
            if (loading) FontUtils.FontType.BOLD else FontUtils.FontType.REGULAR
        )
        inpageMiniImageView.visibility = if(loading) View.VISIBLE else View.GONE
        inpageMiniText.visibility = if(loading) View.GONE else View.VISIBLE
        inpageMiniLoadingText.visibility = if(loading) View.VISIBLE else View.GONE
        inpageMiniButton.visibility = if(loading) View.GONE else View.VISIBLE
    }

    /**
     * Sets the InPage Mini style corresponding to [VirtusizeViewStyle]
     */
    override fun setStyle() {
        if(virtusizeBackgroundColor != 0) {
            inpageMiniLayout.setBackgroundColor(virtusizeBackgroundColor)
            inpageMiniButton.setTextColor(virtusizeBackgroundColor)
            setButtonRightArrowColor(virtusizeBackgroundColor)
        } else if(virtusizeViewStyle == VirtusizeViewStyle.TEAL) {
            inpageMiniLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.virtusizeTeal))
            inpageMiniButton.setTextColor(ContextCompat.getColor(context, R.color.virtusizeTeal))
            setButtonRightArrowColor(ContextCompat.getColor(context, R.color.virtusizeTeal))
        }
    }

    /**
     * Sets up the color of the right arrow in the button
     */
    private fun setButtonRightArrowColor(color: Int) {
        var drawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_right_black)
        drawable = DrawableCompat.wrap(drawable!!)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        DrawableCompat.setTint(drawable, color)

        inpageMiniButton.setCompoundDrawables(null, null, drawable, null)
    }

    /**
     * Sets up the text fonts, localization, and UI dimensions based on the configured context
     */
    private fun setupConfiguredLocalization() {
        FontUtils.setTypeFaces(
            context,
            mutableListOf(
                inpageMiniText,
                inpageMiniButton
            ), virtusizeParams?.language, FontUtils.FontType.REGULAR
        )
        configuredContext = VirtusizeUtils.getConfiguredContext(context, virtusizeParams?.language)
        inpageMiniButton.text = configuredContext?.getText(R.string.virtusize_button_text)
        inpageMiniLoadingText.text = configuredContext?.getText(R.string.inpage_loading_text)
        setConfiguredDimensions()

        if(virtusizeParams?.language == VirtusizeLanguage.JP) {
            inpageMiniText.includeFontPadding = true
        }
    }

    /**
     * Sets up text sizes based on the configured context
     */
    private fun setConfiguredDimensions() {
        val additionalSize = if(virtusizeParams?.language == VirtusizeLanguage.EN) 2f.spToPx else 0f
        if(messageTextSize != -1f) {
            inpageMiniLoadingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize + additionalSize)
            inpageMiniText.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageTextSize + additionalSize)
        } else {
            configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_mini_message_textSize)
                ?.let {
                    inpageMiniLoadingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                    inpageMiniText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }
        }
        if(buttonTextSize != -1f) {
            val size = buttonTextSize + additionalSize
            inpageMiniButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            inpageMiniButton.rightDrawable(R.drawable.ic_arrow_right_black, 0.8f * size / 2, 0.8f * size)
        } else {
            configuredContext?.resources?.getDimension(R.dimen.virtusize_inpage_default_textSize)
                ?.let {
                    inpageMiniButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
                }
        }
    }
}