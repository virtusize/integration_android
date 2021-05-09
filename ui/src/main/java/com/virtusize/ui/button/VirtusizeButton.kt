package com.virtusize.ui.button

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.DrawableCompat
import com.virtusize.ui.R
import com.virtusize.ui.utils.dp


class VirtusizeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    var virtusizeButtonStyle = VirtusizeButtonStyle.NONE
        set(value) {
            field = value
            setView()
        }

    var virtusizeButtonSize = VirtusizeButtonSize.STANDARD
        set(value) {
            field = value
            setView()
        }

    var virtusizeButtonTextSize = VirtusizeButtonTextSize.DEFAULT
        set(value) {
            field = value
            setView()
        }

    private var virtusizeTextColor: Int = 0
    private var virtusizeBackgroundColor: Int = 0
    private var leftIconDrawable: Drawable? = null
    private var rightIconDrawable: Drawable? = null

    init {
        setView()

        isClickable = true
    }


    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        setElevation()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setElevation()
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        virtusizeTextColor = color
    }

    private fun setView() {
        if(virtusizeButtonStyle == VirtusizeButtonStyle.NONE) {
            return
        }

        isAllCaps = false
        minHeight = 0
        minWidth = 0
        minimumHeight = 0
        minimumWidth = 0

        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                if(isRoundButton()) {
                    var size = if (width > height) {
                        width
                    } else {
                        height
                    }
                    if(size < 40.dp.toInt()){
                        size = 40.dp.toInt()
                    }
                    minimumWidth = size
                    minimumHeight = size
                }
            }
        })

        setButtonStyle()
        setElevation()
        setTextStyle()
    }

    fun setVirtusizeBackgroundColor(@ColorInt color: Int) {
        virtusizeBackgroundColor = color
        setButtonStyle()
    }

    fun setLeftIcon(left: Drawable?, width: Int? = null, height: Int? = null) {
        leftIconDrawable = left
        leftIconDrawable?.setBounds(0, 0, width ?: leftIconDrawable!!.minimumWidth, height ?: leftIconDrawable!!.minimumHeight)
        setIcons()
    }

    fun setRightIcon(right: Drawable?, width: Int? = null, height: Int? = null) {
        rightIconDrawable = right
        rightIconDrawable?.setBounds(0, 0, width ?: rightIconDrawable!!.minimumWidth, height ?: rightIconDrawable!!.minimumHeight)
        setIcons()
    }

    private fun setIcons() {
        setCompoundDrawables(leftIconDrawable, null, rightIconDrawable, null)
        compoundDrawablePadding = resources.getDimension(R.dimen.virtusize_button_icon_padding).toInt()
    }

    private fun setButtonStyle() {
        when (virtusizeButtonStyle) {
            VirtusizeButtonStyle.DEFAULT -> {
                setBackgroundResource(R.drawable.virtusize_button_default_background)
            }
            VirtusizeButtonStyle.INVERTED -> {
                setBackgroundResource(R.drawable.virtusize_button_invertd_background)
            }
            VirtusizeButtonStyle.FLAT -> {
                setBackgroundResource(R.drawable.virtusize_button_flat_background)
                stateListAnimator = null
            }
            VirtusizeButtonStyle.ROUND -> {
                setBackgroundResource(R.drawable.virtusize_button_round_background)
            }
            VirtusizeButtonStyle.ROUND_INVERTED -> {
                setBackgroundResource(R.drawable.virtusize_button_round_inverted_background)
            }
        }
        if(virtusizeBackgroundColor != 0) {
            DrawableCompat.setTint(background, virtusizeBackgroundColor)
        }

        val horizontalPadding: Int
        val verticalPadding: Int
        when {
            isRoundButton() -> {
                horizontalPadding = resources.getDimension(R.dimen.virtusize_button_round_padding).toInt()
                verticalPadding = resources.getDimension(R.dimen.virtusize_button_round_padding).toInt()
            }
            virtusizeButtonSize == VirtusizeButtonSize.STANDARD -> {
                horizontalPadding = resources.getDimension(R.dimen.virtusize_button_standard_horizontal_padding).toInt()
                verticalPadding = resources.getDimension(R.dimen.virtusize_button_standard_vertical_padding).toInt()
            }
            else -> {
                horizontalPadding = resources.getDimension(R.dimen.virtusize_button_small_horizontal_padding).toInt()
                verticalPadding = resources.getDimension(R.dimen.virtusize_button_small_vertical_padding).toInt()
            }
        }
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
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

    private fun setTextStyle() {
        val styleId = if (isInvertedButton()) {
            if (virtusizeButtonSize == VirtusizeButtonSize.STANDARD) {
                R.style.TextAppearance_Virtusize_Button_Inverted
            } else {
                R.style.TextAppearance_Virtusize_Button_Inverted_Small
            }
        } else {
            if (virtusizeButtonSize == VirtusizeButtonSize.STANDARD) {
                R.style.TextAppearance_Virtusize_Button_Default
            } else {
                R.style.TextAppearance_Virtusize_Button_Default_Small
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(styleId)
        } else {
            setTextAppearance(context, styleId)
        }

        when(virtusizeButtonTextSize) {
            VirtusizeButtonTextSize.DEFAULT, VirtusizeButtonTextSize.NORMAL -> {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.vs_normal_text_size))
            }
            VirtusizeButtonTextSize.SMALLER -> {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.vs_small_text_size))
            }
            VirtusizeButtonTextSize.LARGE -> {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.vs_large_text_size))
            }
            VirtusizeButtonTextSize.LARGER -> {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.vs_xlarge_text_size))
            }
        }

        if(virtusizeTextColor != 0) {
            setTextColor(virtusizeTextColor)
        }
    }

    private fun isInvertedButton(): Boolean =
        virtusizeButtonStyle == VirtusizeButtonStyle.INVERTED || virtusizeButtonStyle == VirtusizeButtonStyle.ROUND_INVERTED

    private fun isRoundButton(): Boolean =
        virtusizeButtonStyle == VirtusizeButtonStyle.ROUND || virtusizeButtonStyle == VirtusizeButtonStyle.ROUND_INVERTED
}