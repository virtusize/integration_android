package com.virtusize.android.ui.avatar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.virtusize.android.ui.databinding.VirtusizeAvatarBinding

class VirtusizeAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = VirtusizeAvatarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        orientation = VERTICAL
    }
}
