package com.virtusize.libsource.data.local

import com.virtusize.libsource.data.local.VirtusizeEnvironment.*

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

// Gets the Fit Illustrator URL corresponding to the Virtusize Environment
fun VirtusizeEnvironment.fitIllustratorUrl(): String {
    return when(this) {
        STAGING -> "https://static.api.virtusize.com/a/fit-illustrator/v2/staging/index.html"
        GLOBAL -> "https://static.api.virtusize.com/a/fit-illustrator/v2/latest/index.html"
        JAPAN -> "https://static.api.virtusize.jp/a/fit-illustrator/v2/latest/index.html"
        KOREA -> "https://static.api.virtusize.kr/a/fit-illustrator/v2/latest/index.html"
    }
}