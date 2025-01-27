package com.virtusize.android.auth.utils

import androidx.annotation.Keep
import com.virtusize.android.auth.data.SnsType

@Keep
object VirtusizeURLCheck {
    /**
     * Checks if the URL is a Virtusize external link to be opened with a browser app
     */
    fun isExternalLinkFromVirtusize(url: String?): Boolean {
        return (url?.contains("virtusize") == true && url.contains("privacy")) ||
            url?.contains("surveymonkey") == true
    }

    /**
     * Checks if the URL or the link title is from Virtusize
     */
    fun isLinkFromVirtusize(
        url: String?,
        title: String?,
    ): Boolean {
        return isExternalLinkFromVirtusize(url) || title?.let {
            title.contains("Virtusize") || SnsType.containsTitle(title)
        } ?: false
    }
}
