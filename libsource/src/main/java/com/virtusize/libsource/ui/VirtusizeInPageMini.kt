package com.virtusize.libsource.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.virtusize.libsource.R
import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck
import com.virtusize.libsource.util.VirtusizeUtils
import kotlinx.android.synthetic.main.view_inpage_mini.view.*
import java.util.*

class VirtusizeInPageMini(context: Context, attrs: AttributeSet) : VirtusizeRelativeLayoutView(context, attrs) {

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
            setupInPageMiniStyle()
        }

    // The background color for this InPage Mini view
    var virtusizeBackgroundColor = 0
        set(value) {
            field = value
            setupInPageMiniStyle()
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_inpage_mini, this, true)
        visibility = View.INVISIBLE
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeInPageMini, 0, 0)
        val buttonStyle = attrsArray.getInt(R.styleable.VirtusizeInPageMini_virtusizeInPageMiniStyle, VirtusizeViewStyle.NONE.value)
        virtusizeViewStyle = VirtusizeViewStyle.values().firstOrNull { it.value == buttonStyle } ?: VirtusizeViewStyle.NONE
        virtusizeBackgroundColor = attrsArray.getColor(R.styleable.VirtusizeButton_virtusizeButtonStyle, 0)
        attrsArray.recycle()
        setupInPageMiniStyle()
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
                    setupMessageTextConfiguredLocalization()
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

    /**
     * Sets up the InPage Mini Style corresponding to [VirtusizeViewStyle]
     */
    private fun setupInPageMiniStyle() {
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
        drawable.setBounds( 0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        DrawableCompat.setTint(drawable, color)

        inpage_mini_button.setCompoundDrawables(null, null, drawable, null)
    }

    private fun setupMessageTextConfiguredLocalization() {
        var configuredContext = VirtusizeUtils.configureLocale(context, Locale.getDefault())
        when(virtusizeParams?.language) {
            VirtusizeLanguage.EN -> {
                configuredContext = VirtusizeUtils.configureLocale(context, Locale.ENGLISH)
                inpage_mini_text.typeface = Typeface.create("proxima_nova_regular", Typeface.NORMAL)
            }
            VirtusizeLanguage.JP -> {
                configuredContext = VirtusizeUtils.configureLocale(context, Locale.JAPAN)
                inpage_mini_text.typeface = Typeface.create("noto_sans_cjk_jp_regular", Typeface.NORMAL)
            }
            VirtusizeLanguage.KR -> {
                configuredContext = VirtusizeUtils.configureLocale(context, Locale.KOREA)
                inpage_mini_text.typeface = Typeface.create("noto_sans_cjk_kr_regular", Typeface.NORMAL)
            }
        }
        inpage_mini_button.text = configuredContext?.getText(R.string.virtusize_button_text)
    }

    override fun setupRecommendationText(text: String) {
        inpage_mini_text.text = text
    }
}