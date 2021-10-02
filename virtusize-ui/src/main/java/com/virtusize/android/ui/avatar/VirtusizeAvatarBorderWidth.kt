package com.virtusize.android.ui.avatar

import android.graphics.DashPathEffect

enum class VirtusizeAvatarBorderWidth {
    THIN {
        override fun getDashPathEffect(): DashPathEffect {
            return DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
    },
    THICK {
        override fun getDashPathEffect(): DashPathEffect {
            return DashPathEffect(floatArrayOf(20f, 30f), 0f)
        }
    };

    abstract fun getDashPathEffect(): DashPathEffect
}