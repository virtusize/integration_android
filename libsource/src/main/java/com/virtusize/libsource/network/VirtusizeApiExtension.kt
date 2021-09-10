package com.virtusize.libsource.network

import java.net.HttpURLConnection

/**
 * The extension function for HttpURLConnection to check if the request is successful
*/
internal fun HttpURLConnection.isSuccessful(): Boolean {
    return responseCode in 200..299
}
