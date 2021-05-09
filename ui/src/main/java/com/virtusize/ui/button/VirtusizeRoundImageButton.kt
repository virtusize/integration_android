package com.virtusize.ui.button

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver.*
import androidx.core.content.ContextCompat
import com.virtusize.ui.R
import com.virtusize.ui.utils.dp

class VirtusizeRoundImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : androidx.appcompat.widget.AppCompatImageButton(context, attrs, defStyleAttr) {

    var isInverted = false
        set(value) {
            field = value
            setButtonStyle()
        }

    init {
        adjustViewBounds = true
        minimumWidth = 40.dp.toInt()
        minimumHeight = 40.dp.toInt()

        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                val size = if (width > height) {
                    width
                } else {
                    height
                }
                minimumWidth = size
                minimumHeight = size
            }
        })

        val horizontalPadding = resources.getDimension(R.dimen.virtusize_button_round_padding).toInt()
        val verticalPadding = resources.getDimension(R.dimen.virtusize_button_round_padding).toInt()
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }

    private fun setButtonStyle() {
        if(!isInverted) {
            setBackgroundResource(R.drawable.virtusize_button_round_background)
            setColorFilter(ContextCompat.getColor(context, R.color.vs_gray_900))
        } else {
            setBackgroundResource(R.drawable.virtusize_button_invertd_background)
            setColorFilter(ContextCompat.getColor(context, R.color.vs_white))
        }
    }
}