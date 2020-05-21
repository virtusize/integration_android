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
 * Gets the environment value corresponding to the Virtusize Environment it is called on
 * @return A String value of the Virtusize Environment
 */
fun VirtusizeEnvironment.value(): String {
    return when(this) {
        STAGING -> "https://staging.virtusize.com"
        GLOBAL -> "https://api.virtusize.com"
        JAPAN -> "https://api.virtusize.jp"
        KOREA -> "https://api.virtusize.kr"
    }
}