package com.virtusize.android.auth.data

import android.net.Uri

/**
 * An enum class for the supported social networks.
 */
enum class SnsType(val value: String, val title: String, val host: String) {
    FACEBOOK("facebook", "Facebook", "facebook.com"),
    GOOGLE("google", "Google", "google.com"),
    LINE("line", "LINE", "line.me"),
    ;

    companion object {
        fun containsTitle(title: String): Boolean = values().map { it.title }.any { title.contains(it) }

        fun fromHost(uri: Uri): SnsType? =
            uri.host?.let { host ->
                values().first { host.contains(it.host) }
            }
    }
}
