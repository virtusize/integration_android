package com.virtusize.android.ui.fittingroom

import android.content.Context
import android.util.AttributeSet
import com.virtusize.android.ui.avatar.VirtusizeAvatar
import com.virtusize.android.ui.avatar.VirtusizeAvatarSize

class VirtusizeCurationAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : VirtusizeAvatar(context, attrs) {

    init {
        vsAvatarGapEnabled = false
        vsAvatarSize = VirtusizeAvatarSize.FITTING_ROOM
    }
}
