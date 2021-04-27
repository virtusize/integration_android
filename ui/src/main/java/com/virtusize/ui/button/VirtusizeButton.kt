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
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    init {
        setBackgroundResource(R.drawable.virtusize_button_default_background)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(R.style.TextAppearance_Virtusize_Button)
        } else {
            setTextAppearance(context, R.style.TextAppearance_Virtusize_Button)
        }

        isClickable = true

        setElevation()
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        setElevation()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setElevation()
    }

    private fun setElevation() {
        elevation = if (isEnabled && !isPressed) {
            1.dp
        } else {
            0.dp
        }
    }
}