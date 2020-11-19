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
 * Gets the API URL corresponding to the Virtusize Environment
 * @return A String value of the API URL
 */
fun VirtusizeEnvironment.apiUrl(): String {
    return when(this) {
        STAGING -> "https://staging.virtusize.com"
        GLOBAL -> "https://api.virtusize.com"
        JAPAN -> "https://api.virtusize.jp"
        KOREA -> "https://api.virtusize.kr"
    }
}

/**
 * Gets the event API URL corresponding to the Virtusize Environment
 * @return A String value of the event API URL
 */
fun VirtusizeEnvironment.eventApiUrl(): String {
    return when(this) {
        STAGING -> "https://events.staging.virtusize.jp"
        JAPAN -> "https://events.virtusize.jp"
        GLOBAL -> "https://events.virtusize.com"
        KOREA -> "https://events.virtusize.kr"
    }
}

/**
 * Gets the URL for the API endpoint Product Data Check corresponding to the Virtusize Environment
 * @return A String value of the URL for the API endpoint Product Data Check
 */
fun VirtusizeEnvironment.productDataCheckUrl(): String {
    return when(this) {
        STAGING -> "https://services.virtusize.com/stg"
        else -> "https://services.virtusize.com/"
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