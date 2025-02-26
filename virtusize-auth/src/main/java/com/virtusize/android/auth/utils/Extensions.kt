package com.virtusize.android.auth.utils

import android.net.Uri

internal fun String.isVirtusizeSNSAuthURL(): Boolean = matches(regex = Regex("https://.*(?:facebook|google|line\\.me).*oauth.*virtusize.*"))

internal fun String.isVirtusizeAuthAppURL(): Boolean =
    matches(regex = Regex("https://api\\.virtusize\\.\\w+/auth/\\w+/index\\.html(\\?.*)?"))

fun Uri.withBranch(branchName: String): Uri =
    VirtusizeUriHelper.updateUriParameter(this, "vs-branch", branchName)
