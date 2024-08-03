package com.virtusize.android.data.local

/**
 * The class that wraps Virtusize specific error information
 *
 * @param type Any Virtusize error type that is wanted to be passed
 * @param code Any error code that is wanted to passed
 * @param message Any error message that is wanted to be passed
 */
data class VirtusizeError(
    val type: VirtusizeErrorType? = null,
    val code: Int? = null,
    val message: String,
)
