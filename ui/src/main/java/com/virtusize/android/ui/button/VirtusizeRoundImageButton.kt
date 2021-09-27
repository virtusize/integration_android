package com.virtusize.android.ui.button

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver.*
import androidx.core.content.ContextCompat
import com.virtusize.ui.R
import com.virtusize.android.ui.utils.dp

open class VirtusizeRoundImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : androidx.appcompat.widget.AppCompatImageButton(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_BUTTON_SIZE = 44
    }

    var roundImageButtonStyle = VirtusizeRoundImageButtonStyle.DEFAULT
        set(value) {
            field = value
            setButtonStyle()
        }

    var virtusizeButtonSize = VirtusizeButtonSize.STANDARD
        set(value) {
            field = value
            setButtonSize()
        }

    init {
        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeRoundImageButton, 0, 0)

        val buttonStyle = attrsArray.getInt(R.styleable.VirtusizeRoundImageButton_roundImageButtonStyle, VirtusizeRoundImageButtonStyle.DEFAULT.ordinal)
        roundImageButtonStyle = VirtusizeRoundImageButtonStyle.values().firstOrNull { it.ordinal == buttonStyle } ?: VirtusizeRoundImageButtonStyle.DEFAULT

        val buttonSize = attrsArray.getInt(R.styleable.VirtusizeRoundImageButton_roundImageButtonSize, VirtusizeButtonSize.STANDARD.ordinal)
        virtusizeButtonSize = VirtusizeButtonSize.values().firstOrNull { it.ordinal == buttonSize } ?: VirtusizeButtonSize.STANDARD

        attrsArray.recycle()

        adjustViewBounds = true
        minimumWidth = DEFAULT_BUTTON_SIZE.dp.toInt()
        minimumHeight = DEFAULT_BUTTON_SIZE.dp.toInt()

        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                var size = if (width > height) {
                    width
                } else {
                    height
                }
                if(size < DEFAULT_BUTTON_SIZE.dp.toInt()) {
                    size = DEFAULT_BUTTON_SIZE.dp.toInt()
                }
                minimumWidth = size
                minimumHeight = size
            }
        })

        setButtonSize()
        setButtonStyle()
    }

    private fun setButtonSize() {
        val padding = if (virtusizeButtonSize == VirtusizeButtonSize.STANDARD) {
            resources.getDimension(R.dimen.virtusize_button_round_standard_padding).toInt()
        } else {
            resources.getDimension(R.dimen.virtusize_button_round_small_padding).toInt()
        }
        setPadding(padding, padding, padding, padding)
    }

    private fun setButtonStyle() {
        if(roundImageButtonStyle == VirtusizeRoundImageButtonStyle.DEFAULT) {
            setBackgroundResource(R.drawable.virtusize_button_round_background)
            setColorFilter(ContextCompat.getColor(context, R.color.vs_gray_900))
        } else if(roundImageButtonStyle == VirtusizeRoundImageButtonStyle.INVERTED) {
            setBackgroundResource(R.drawable.virtusize_button_invertd_background)
            setColorFilter(ContextCompat.getColor(context, R.color.vs_white))
        } else if(roundImageButtonStyle == VirtusizeRoundImageButtonStyle.COLOR) {
            setBackgroundResource(R.drawable.virtusize_button_round_background)
        }
    }
}