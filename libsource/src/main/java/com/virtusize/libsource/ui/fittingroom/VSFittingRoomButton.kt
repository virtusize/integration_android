package com.virtusize.libsource.ui.fittingroom

import android.content.Context
import android.util.AttributeSet
import com.virtusize.libsource.R
import com.virtusize.ui.button.VirtusizeRoundImageButton
import com.virtusize.ui.button.VirtusizeRoundImageButtonStyle

class VSFittingRoomButton@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : VirtusizeRoundImageButton(context, attrs, defStyleAttr) {

    init {
        roundImageButtonStyle = VirtusizeRoundImageButtonStyle.DEFAULT
        setImageResource(R.drawable.ic_search_product)
    }
}