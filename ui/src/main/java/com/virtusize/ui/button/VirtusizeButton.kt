package com.virtusize.ui.button

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.virtusize.ui.R
import com.virtusize.ui.utils.dp


class VirtusizeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    var virtusizeButtonStyle: VirtusizeButtonStyle = VirtusizeButtonStyle.NONE
        set(value) {
            field = value
            setView()
        }

    init {
        setView()

        isClickable = true
    }

    private fun setView() {
        if(virtusizeButtonStyle == VirtusizeButtonStyle.NONE) {
            return
        }

        setButtonStyle(virtusizeButtonStyle)

        setElevation()

        val styleId = if (virtusizeButtonStyle == VirtusizeButtonStyle.INVERTED) {
            R.style.TextAppearance_Virtusize_Button_Inverted
        } else {
            R.style.TextAppearance_Virtusize_Button_Default
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(styleId)
        } else {
            setTextAppearance(context, styleId)
        }
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        setElevation()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setElevation()
    }

    private fun setButtonStyle(style: VirtusizeButtonStyle) {
        if (style == VirtusizeButtonStyle.DEFAULT) {
            setBackgroundResource(R.drawable.virtusize_button_default_background)
        } else if (style == VirtusizeButtonStyle.INVERTED) {
            setBackgroundResource(R.drawable.virtusize_button_invertd_background)
        } else if (style == VirtusizeButtonStyle.FLAT) {
            setBackgroundResource(R.drawable.virtusize_button_flat_background)
            stateListAnimator = null
        }
    }

    private fun setElevation() {
        if(virtusizeButtonStyle == VirtusizeButtonStyle.NONE) {
            return
        }

        elevation = if (virtusizeButtonStyle != VirtusizeButtonStyle.FLAT && isEnabled && !isPressed) {
            4.dp
        } else {
            0.dp
        }
    }
}