package com.virtusize.libsource.model

import com.virtusize.libsource.model.VirtusizeEnvironment.*

/**
 * This enum contains all available Virtusize environments
 */
enum class VirtusizeEnvironment {
    STAGING,
    GLOBAL,
    JAPAN,
    KOREA
}

/**
 * This method is used to get environment value corresponding to the VirtusizeEnvironment it is called on
 * @return Value of Virtusize Environment
 */
fun VirtusizeEnvironment.value(): String {
    return when(this) {
        STAGING -> "https://staging.virtusize.com"
        GLOBAL -> "https://api.virtusize.com"
        JAPAN -> "https://api.virtusize.jp"
        KOREA -> "https://api.virtusize.kr"
    }
}