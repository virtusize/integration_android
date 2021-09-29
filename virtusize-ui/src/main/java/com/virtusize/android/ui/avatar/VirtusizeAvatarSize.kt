package com.virtusize.android.ui.avatar

import android.content.Context
import com.virtusize.android.ui.R

enum class VirtusizeAvatarSize {
    SMALLER {
        override fun getAvatarCircleSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_smaller_size)
        }

        override fun getAvatarCircleRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_radius_smaller_size)
        }

        override fun getAvatarImageSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_image_smaller_size)
        }

        override fun getAvatarImageRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_image_radius_smaller_size)
        }
    },
    SMALL {
        override fun getAvatarCircleSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_small_size)
        }

        override fun getAvatarCircleRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_radius_small_size)
        }

        override fun getAvatarImageSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_image_small_size)
        }

        override fun getAvatarImageRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_image_radius_small_size)
        }
    },
    MEDIUM {
        override fun getAvatarCircleSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_medium_size)
        }

        override fun getAvatarCircleRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_radius_medium_size)
        }

        override fun getAvatarImageSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_image_medium_size)
        }

        override fun getAvatarImageRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_image_radius_medium_size)
        }
    },
    LARGE {
        override fun getAvatarCircleSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_large_size)
        }

        override fun getAvatarCircleRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_radius_large_size)
        }

        override fun getAvatarImageSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_image_large_size)
        }

        override fun getAvatarImageRadiusSize(context: Context): Float {
            return context.resources.getDimension(R.dimen.vs_avatar_image_radius_large_size)
        }
    };

    abstract fun getAvatarCircleSize(context: Context): Float
    abstract fun getAvatarCircleRadiusSize(context: Context): Float
    abstract fun getAvatarImageSize(context: Context): Float
    abstract fun getAvatarImageRadiusSize(context: Context): Float
}
