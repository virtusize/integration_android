package com.virtusize.android.ui.button

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.virtusize.android.ui.R
import com.virtusize.android.ui.utils.dp

class VirtusizeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    var virtusizeButtonStyle = VirtusizeButtonStyle.NONE
        set(value) {
            field = value
            setButtonStyle()
        }

    var virtusizeButtonLevel = VirtusizeButtonLevel.NONE
        set(value) {
            field = value
            setButtonLevel()
        }

    var virtusizeButtonBordersWithTransparency = VirtusizeButtonBordersWithTransparency.NONE
        set(value) {
            field = value
            setButtonBordersWithTransparency()
        }

    var virtusizeButtonFontWeight = VirtusizeButtonFontWeight.BOLD
        set(value) {
            field = value
            setButtonFontWeight()
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
        setTextColor(currentTextColor)

        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.VirtusizeButton, 0, 0)

        val buttonStyle = attrsArray.getInt(R.styleable.VirtusizeButton_uiButtonStyle, VirtusizeButtonStyle.NONE.ordinal)
        virtusizeButtonStyle = VirtusizeButtonStyle.values().firstOrNull { it.ordinal == buttonStyle } ?: VirtusizeButtonStyle.NONE

        val buttonLevel = attrsArray.getInt(R.styleable.VirtusizeButton_uiButtonLevel, VirtusizeButtonLevel.NONE.ordinal)
        virtusizeButtonLevel = VirtusizeButtonLevel.values().firstOrNull { it.ordinal == buttonLevel } ?: VirtusizeButtonLevel.NONE

        val buttonBorderWithTransparency = attrsArray.getInt(R.styleable.VirtusizeButton_virtusizeButtonBorderAndTransparency, VirtusizeButtonBordersWithTransparency.NONE.ordinal)
        virtusizeButtonBordersWithTransparency = VirtusizeButtonBordersWithTransparency.values().firstOrNull { it.ordinal == buttonBorderWithTransparency } ?: VirtusizeButtonBordersWithTransparency.NONE

        val buttonFontWeight = attrsArray.getInt(R.styleable.VirtusizeButton_buttonFontWeight, VirtusizeButtonFontWeight.BOLD.ordinal)
        virtusizeButtonFontWeight = VirtusizeButtonFontWeight.values().firstOrNull { it.ordinal == buttonFontWeight } ?: VirtusizeButtonFontWeight.BOLD

        val buttonSize = attrsArray.getInt(R.styleable.VirtusizeButton_virtusizeButtonSize, VirtusizeButtonSize.STANDARD.ordinal)
        virtusizeButtonSize = VirtusizeButtonSize.values().firstOrNull { it.ordinal == buttonSize } ?: VirtusizeButtonSize.STANDARD

        virtusizeBackgroundColor = attrsArray.getColor(R.styleable.VirtusizeButton_virtusizeBackgroundColor, 0)

        val buttonTextSize = attrsArray.getInt(R.styleable.VirtusizeButton_virtusizeButtonTextSize, VirtusizeButtonTextSize.DEFAULT.ordinal)
        virtusizeButtonTextSize = VirtusizeButtonTextSize.values().firstOrNull { it.ordinal == buttonTextSize } ?: VirtusizeButtonTextSize.DEFAULT

        attrsArray.recycle()

        isClickable = true
        isAllCaps = false

        minHeight = 0
        minWidth = 0
        minimumHeight = 0
        minimumWidth = 0

        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (isRoundButton()) {
                    var size = if (width > height) {
                        width
                    } else {
                        height
                    }
                    if (size < 40.dp.toInt()) {
                        size = 40.dp.toInt()
                    }
                    minimumWidth = size
                    minimumHeight = size
                }
            }
        })

        setView()
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
        if (virtusizeButtonStyle == VirtusizeButtonStyle.NONE) {
            return
        }

        setButtonStyle()
        setIcons()
        setElevation()
        setTextStyle()
        setButtonFontWeight()
    }

    fun setVirtusizeBackgroundColor(@ColorInt color: Int) {
        virtusizeBackgroundColor = color
        setButtonStyle()
    }

    fun setLeftIcon(left: Drawable?, width: Int? = null, height: Int? = null) {
        leftIconDrawable = left
        leftIconDrawable?.setBounds(0, 0, width ?: leftIconDrawable!!.minimumWidth, height ?: leftIconDrawable!!.minimumHeight)
        if (isInvertedButton() && leftIconDrawable != null) {
            DrawableCompat.setTint(leftIconDrawable!!, ContextCompat.getColor(context, R.color.vs_white))
        }
        setIcons()
    }

    fun setRightIcon(right: Drawable?, width: Int? = null, height: Int? = null) {
        rightIconDrawable = right
        rightIconDrawable?.setBounds(0, 0, width ?: rightIconDrawable!!.minimumWidth, height ?: rightIconDrawable!!.minimumHeight)
        if (isInvertedButton() && rightIconDrawable != null) {
            DrawableCompat.setTint(rightIconDrawable!!, ContextCompat.getColor(context, R.color.vs_white))
        }
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
            VirtusizeButtonStyle.SQUARE -> {
                setBackgroundResource(R.drawable.virtusize_button_square_background)
            }

            else -> {}
        }
        if (virtusizeBackgroundColor != 0) {
            DrawableCompat.setTint(background, virtusizeBackgroundColor)
        }

        val horizontalPadding: Int
        val verticalPadding: Int
        when {
            isRoundButton() -> {
                horizontalPadding = resources.getDimension(R.dimen.virtusize_button_round_standard_padding).toInt()
                verticalPadding = resources.getDimension(R.dimen.virtusize_button_round_standard_padding).toInt()
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

    private fun setButtonLevel() {
        when (virtusizeButtonLevel) {

            VirtusizeButtonLevel.FLUID -> {
                // Take all the width available in the screen
                val layoutParams = LayoutParams(
                    MATCH_PARENT,
                    WRAP_CONTENT
                )
                this.layoutParams = layoutParams
            }

            VirtusizeButtonLevel.BLOCK -> {
                // Set Fixed Size or take width from user
                val layoutParams = LayoutParams(
                    resources.getDimension(R.dimen.virtusize_button_block_width).toInt(),
                    WRAP_CONTENT
                )
                this.layoutParams = layoutParams
            }
            else -> {}
        }
    }

    private fun setButtonFontWeight() {
        when (virtusizeButtonFontWeight) {

            VirtusizeButtonFontWeight.BOLD -> {
                setTypeface(null, Typeface.BOLD)
            }

            VirtusizeButtonFontWeight.REGULAR -> {
                setTypeface(null, Typeface.NORMAL)
            }

            else -> {}
        }
    }

    private fun setButtonBordersWithTransparency() {
        when (virtusizeButtonBordersWithTransparency) {

            VirtusizeButtonBordersWithTransparency.NO_BORDER -> {
                setBackgroundResource(R.drawable.virtusize_button_no_border_background)
            }

            VirtusizeButtonBordersWithTransparency.TRANSPARENT -> {
                setBackgroundResource(R.drawable.virtusize_button_transparent_background)
            }

            VirtusizeButtonBordersWithTransparency.NO_BORDER_AND_TRANSPARENT -> {
                setBackgroundColor(Color.TRANSPARENT)
            }

            else -> {}
        }
    }

    private fun setElevation() {
        if (virtusizeButtonStyle == VirtusizeButtonStyle.NONE) {
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

        if (virtusizeButtonStyle == VirtusizeButtonStyle.DEFAULT && virtusizeTextColor != 0) {
            setTextColor(virtusizeTextColor)
        }

        when (virtusizeButtonTextSize) {
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
    }

    private fun isInvertedButton(): Boolean =
        virtusizeButtonStyle == VirtusizeButtonStyle.INVERTED || virtusizeButtonStyle == VirtusizeButtonStyle.ROUND_INVERTED

    private fun isRoundButton(): Boolean =
        virtusizeButtonStyle == VirtusizeButtonStyle.ROUND || virtusizeButtonStyle == VirtusizeButtonStyle.ROUND_INVERTED
}
