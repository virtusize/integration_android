package com.virtusize.android.data.local

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
    return when (this) {
        VirtusizeEnvironment.STAGING -> "https://staging.virtusize.com"
        VirtusizeEnvironment.GLOBAL -> "https://api.virtusize.com"
        VirtusizeEnvironment.JAPAN -> "https://api.virtusize.jp"
        VirtusizeEnvironment.KOREA -> "https://api.virtusize.kr"
    }
}

/**
 * Gets the event API URL corresponding to the Virtusize Environment
 * @return A String value of the event API URL
 */
fun VirtusizeEnvironment.eventApiUrl(): String {
    return when (this) {
        VirtusizeEnvironment.STAGING, VirtusizeEnvironment.GLOBAL -> "https://events.virtusize.com"
        VirtusizeEnvironment.JAPAN -> "https://events.virtusize.jp"
        VirtusizeEnvironment.KOREA -> "https://events.virtusize.kr"
    }
}

/**
 * Gets the services API URL corresponding to the Virtusize Environment
 * @return A String value of the services API URL
 */
fun VirtusizeEnvironment.servicesApiUrl(): String {
    return when (this) {
        VirtusizeEnvironment.STAGING -> "https://services.virtusize.com/stg"
        VirtusizeEnvironment.GLOBAL -> "https://services.virtusize.com"
        VirtusizeEnvironment.JAPAN -> "https://services.virtusize.jp"
        VirtusizeEnvironment.KOREA -> "https://services.virtusize.kr"
    }
}

/**
 * Gets the URL for loading the Virtusize web app corresponding to the Virtusize Environment
 * @return A String value of the Virtusize URL
 */
fun VirtusizeEnvironment.virtusizeUrl(): String {
    return when (this) {
        VirtusizeEnvironment.STAGING,
        VirtusizeEnvironment.GLOBAL -> "https://static.api.virtusize.com"
        VirtusizeEnvironment.JAPAN -> "https://static.api.virtusize.jp"
        VirtusizeEnvironment.KOREA -> "https://static.api.virtusize.kr"
    }
}

/**
 * Gets the [VirtusizeRegion] value of the region parameter to be passed to the Virtusize web app
 * corresponding to the Virtusize Environment
 *
 * @return A [VirtusizeRegion] value of the region parameter
 */
fun VirtusizeEnvironment.virtusizeRegion(): VirtusizeRegion {
    return when (this) {
        VirtusizeEnvironment.STAGING, VirtusizeEnvironment.GLOBAL -> VirtusizeRegion.COM
        VirtusizeEnvironment.JAPAN -> VirtusizeRegion.JP
        VirtusizeEnvironment.KOREA -> VirtusizeRegion.KR
    }
}

/**
 * Gets the environment for the web view
 * @return A String value of the web view environment
 */
fun VirtusizeEnvironment.virtusizeWebViewEnv(): String {
    return when (this) {
        VirtusizeEnvironment.STAGING -> "staging"
        else -> "production"
    }
}

/**
 * The URL for i18n
 */
const val I18N_URL = "https://i18n.virtusize.jp"
