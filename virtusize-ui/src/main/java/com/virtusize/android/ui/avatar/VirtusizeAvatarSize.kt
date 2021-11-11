package com.virtusize.android.ui.avatar

import android.content.Context
import com.virtusize.android.ui.R

enum class VirtusizeAvatarSize {
    SMALLER {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_smaller_size)
        }

        override fun getAvatarRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_smaller_size) / 2
        }
    },
    SMALL {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_small_size)
        }

        override fun getAvatarRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_small_size) / 2
        }
    },
    MEDIUM {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_medium_size)
        }

        override fun getAvatarRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_medium_size) / 2
        }
    },
    LARGE {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_large_size)
        }

        override fun getAvatarRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_large_size) / 2
        }
    },
    FITTING_ROOM {
        override fun getAvatarSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_fitting_room_size)
        }

        override fun getAvatarRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_fitting_room_size) / 2
        }
    };

    abstract fun getAvatarSize(context: Context): Float
    abstract fun getAvatarRadiusSize(context: Context): Float
}
