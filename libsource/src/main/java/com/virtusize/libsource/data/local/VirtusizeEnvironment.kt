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
fun VirtusizeEnvironment.apiBaseUrl(): String {
    return when(this) {
        STAGING -> "https://staging.virtusize.com"
        GLOBAL -> "https://api.virtusize.com"
        JAPAN -> "https://api.virtusize.jp"
        KOREA -> "https://api.virtusize.kr"
    }
}

fun VirtusizeEnvironment.productDataCheckBaseUrl(): String {
    return when(this) {
        STAGING -> "https://services.virtusize.com/stg"
        else -> "https://services.virtusize.com/"
    }
}

// TODO
fun VirtusizeEnvironment.virtusizeBaseUrl(): String {
    return when(this) {
        STAGING, JAPAN -> "https://static.api.virtusize.jp"
        GLOBAL -> "https://static.api.virtusize.com"
        KOREA -> "https://static.api.virtusize.kr"
    }
}

// TODO
fun VirtusizeEnvironment.virtusizeRegion(): VirtusizeRegion {
    return when(this) {
        STAGING, JAPAN -> VirtusizeRegion.JP
        GLOBAL -> VirtusizeRegion.COM
        KOREA -> VirtusizeRegion.KR
    }
}