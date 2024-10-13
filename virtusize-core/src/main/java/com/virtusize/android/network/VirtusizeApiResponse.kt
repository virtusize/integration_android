package com.virtusize.android.network

import com.virtusize.android.data.local.VirtusizeError

/**
 * A generic class that holds a API response from the Virtusize server
 * @param <T>
 */
sealed class VirtusizeApiResponse<out R> {
    val isSuccessful
        get() = this is Success<*> && failureData == null

    val successData: R?
        get() = if (this is Success<*>) data as? R else null
    val failureData: VirtusizeError?
        get() = if (this is Error) error else null

    // The successful response with data of <T>
    data class Success<out T>(
        val data: T,
    ) : VirtusizeApiResponse<T>()

    // The error response with an error of [VirtusizeError]
    data class Error(
        val error: VirtusizeError,
    ) : VirtusizeApiResponse<Nothing>()

    override fun toString(): String =
        when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[error=$error]"
        }
}
