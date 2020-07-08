package com.virtusize.libsource.data.local

import com.virtusize.libsource.data.local.VirtusizeEnvironment.*
import com.virtusize.libsource.data.local.aoyama.AoyamaEnvironment
import com.virtusize.libsource.data.local.aoyama.AoyamaRegion

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

// TODO
fun VirtusizeEnvironment.aoyamaBaseUrl(): String {
    return when(this) {
        STAGING -> "https://static.api.virtusize.jp"
        GLOBAL -> "https://static.api.virtusize.com"
        JAPAN -> "https://static.api.virtusize.jp"
        KOREA -> "https://static.api.virtusize.kr"
    }
}

// TODO
fun VirtusizeEnvironment.aoyamaEnv(): AoyamaEnvironment {
    return when(this) {
        STAGING -> AoyamaEnvironment.STAGE
        else -> AoyamaEnvironment.PRODUCTION
    }
}

// TODO
fun VirtusizeEnvironment.aoyamaRegion(): AoyamaRegion {
    return when(this) {
        STAGING -> AoyamaRegion.JP
        GLOBAL -> AoyamaRegion.COM
        JAPAN -> AoyamaRegion.JP
        KOREA -> AoyamaRegion.KR
    }
}