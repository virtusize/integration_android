package com.virtusize.libsource.network

import com.virtusize.libsource.data.local.VirtusizeError

/**
 * A generic class that holds a API response from the Virtusize server
 * @param <T>
 */
internal sealed class VirtusizeApiResponse<out R> {
    // The successful response with data of <T>
    data class Success<out T>(val data: T) : VirtusizeApiResponse<T>()
    // The error response with an error of [VirtusizeError]
    data class Error(val error: VirtusizeError) : VirtusizeApiResponse<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[error=$error]"
        }
    }
}