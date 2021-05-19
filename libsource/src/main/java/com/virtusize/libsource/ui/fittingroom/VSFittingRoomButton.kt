package com.virtusize.libsource.ui.fittingroom

import android.content.Context
import com.virtusize.libsource.R
import com.virtusize.ui.button.VirtusizeRoundImageButton
import com.virtusize.ui.button.VirtusizeRoundImageButtonStyle

class VSFittingRoomButton(context: Context) : VirtusizeRoundImageButton(context) {

    init {
        roundImageButtonStyle = VirtusizeRoundImageButtonStyle.DEFAULT
        setImageResource(R.drawable.ic_search_product)
    }
}