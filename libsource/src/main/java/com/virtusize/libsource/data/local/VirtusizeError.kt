package com.virtusize.libsource.data.local

import android.util.Log
import com.virtusize.libsource.util.Constants

/**
 * The class that wraps Virtusize specific error information
 *
 * @param type Any Virtusize error type that is wanted to be passed
 * @param code Any error code that is wanted to passed
 * @param message Any error message that is wanted to be passed
 */
data class VirtusizeError(val type: VirtusizeErrorType? = null, val code: Int? = null, val message: String)

/**
 * Throws a VirtusizeError.
 * It logs the error information and exits the normal app flow by throwing an error
 * @param errorType VirtusizeErrorType
 * @throws IllegalArgumentException
 * @see VirtusizeErrorType
 */
internal fun throwError(errorType: VirtusizeErrorType) {
    Log.e(Constants.VIRTUSIZE_LOG_TAG, errorType.message())
    errorType.throwError()
}