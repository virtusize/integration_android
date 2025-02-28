package com.virtusize.android.auth.utils

internal fun String.isVirtusizeSNSAuthURL(): Boolean = matches(regex = Regex("https://.*(?:facebook|google|line\\.me).*oauth.*virtusize.*"))

internal fun String.isVirtusizeAuthAppURL(): Boolean =
    matches(regex = Regex("https://api\\.virtusize\\.\\w+/auth/\\w+/index\\.html(\\?.*)?"))
