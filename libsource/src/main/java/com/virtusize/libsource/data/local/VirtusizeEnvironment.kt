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
 * Gets the default API URL corresponding to the Virtusize Environment
 * @return A String value of the default API URL
 */
fun VirtusizeEnvironment.defaultApiUrl(): String {
    return when(this) {
        STAGING -> "https://staging.virtusize.jp"
        GLOBAL -> "https://api.virtusize.com"
        JAPAN -> "https://api.virtusize.jp"
        KOREA -> "https://api.virtusize.kr"
    }
}

/**
 * Gets the services API URL corresponding to the Virtusize Environment
 * @return A String value of the services API URL
 */
fun VirtusizeEnvironment.servicesApiUrl(): String {
    return when(this) {
        STAGING -> "https://services.virtusize.jp/stg"
        GLOBAL -> "https://services.virtusize.com"
        JAPAN -> "https://services.virtusize.jp"
        KOREA -> "https://services.virtusize.kr"
    }
}

/**
 * Gets the URL for loading the Virtusize web app corresponding to the Virtusize Environment
 * @return A String value of the Virtusize URL
 */
fun VirtusizeEnvironment.virtusizeUrl(): String {
    return when(this) {
        STAGING, JAPAN -> "https://static.api.virtusize.jp"
        GLOBAL -> "https://static.api.virtusize.com"
        KOREA -> "https://static.api.virtusize.kr"
    }
}

/**
 * Gets the [VirtusizeRegion] value of the region parameter to be passed to the Virtusize web app
 * corresponding to the Virtusize Environment
 *
 * @return A [VirtusizeRegion] value of the region parameter
 */
fun VirtusizeEnvironment.virtusizeRegion(): VirtusizeRegion {
    return when(this) {
        STAGING, JAPAN -> VirtusizeRegion.JP
        GLOBAL -> VirtusizeRegion.COM
        KOREA -> VirtusizeRegion.KR
    }
}

fun VirtusizeEnvironment.virtusizeWebViewEnv(): String {
    return when(this) {
        STAGING -> "staging"
        else -> "production"
    }
}

// The URL for i18n
const val I18N_URL = "https://i18n.virtusize.jp"