package com.virtusize.libsource.model

import com.virtusize.libsource.model.VirtusizeEnvironment.*

enum class VirtusizeEnvironment {
    STAGING,
    GLOBAL,
    JAPAN,
    KOREA
}

fun VirtusizeEnvironment.value(): String {
    return when(this) {
        STAGING -> "https://staging.virtusize.com"
        GLOBAL -> "www.virtusize.com"
        JAPAN -> "api.virtusize.jp"
        KOREA -> "api.virtusize.kr"
    }
}