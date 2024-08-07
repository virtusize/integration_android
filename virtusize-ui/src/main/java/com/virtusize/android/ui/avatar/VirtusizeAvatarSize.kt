package com.virtusize.android.ui.avatar

import android.content.Context
import com.virtusize.android.ui.R

enum class VirtusizeAvatarSize {
    SMALLER {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_smaller_size)
        }
    },
    SMALL {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_small_size)
        }
    },
    MEDIUM {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_medium_size)
        }
    },
    LARGE {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_large_size)
        }
    },
    FITTING_ROOM {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_fitting_room_size)
        }
    }, ;

    abstract fun getAvatarSize(context: Context): Float

    fun getAvatarRadiusSize(context: Context): Float = getAvatarSize(context) / 2
}
