package com.virtusize.libsource.data.local

import java.lang.IllegalArgumentException
import java.net.HttpURLConnection

/**
 * This enum contains all available error types in Virtusize library
 */
enum class VirtusizeErrorType {
    NullVirtusizeButtonError,
    ApiKeyNullOrInvalid,
    UserIdNullOrEmpty,
    NullContext,
    ImageUrlNotValid,
    NullProduct,
    InvalidProduct,
    NetworkError,
    JsonParsingError
}

/**
 * Returns an error code for the VirtusizeErrorType that it is called on
 * @return the error code for the VirtusizeErrorType
 */
fun VirtusizeErrorType.code(): Int? {
    return when(this) {
        VirtusizeErrorType.ApiKeyNullOrInvalid -> HttpURLConnection.HTTP_FORBIDDEN
        VirtusizeErrorType.InvalidProduct -> HttpURLConnection.HTTP_NOT_FOUND
        else -> null
    }
}

/**
 * Returns an error message for the VirtusizeErrorType that it is called on
 * @return the error message for the VirtusizeErrorType
 */
fun VirtusizeErrorType.message(extraMessage: String? = null): String {
    return when(this) {
        VirtusizeErrorType.NullVirtusizeButtonError -> "Virtusize Button is null."
        VirtusizeErrorType.ApiKeyNullOrInvalid -> "Api Key is not set or invalid"
        VirtusizeErrorType.UserIdNullOrEmpty -> "The unique user ID from the client system is not set up or empty"
        VirtusizeErrorType.NullContext -> "Context can not be null"
        VirtusizeErrorType.ImageUrlNotValid -> "Image URL is invalid"
        VirtusizeErrorType.NullProduct -> "Product can not null"
        VirtusizeErrorType.InvalidProduct -> "Product $extraMessage is not valid in the Virtusize server"
        VirtusizeErrorType.NetworkError -> "Network error: $extraMessage"
        VirtusizeErrorType.JsonParsingError -> "JSON parsing error: $extraMessage"
    }
}

/**
 * Returns the [VirtusizeError] corresponding to the VirtusizeErrorType
 * @return the [VirtusizeError] for the VirtusizeErrorType
 */
internal fun VirtusizeErrorType.virtusizeError(extraMessage: String? = null): VirtusizeError {
    return VirtusizeError(this, this.code(), this.message(extraMessage))
}

/**
 * Throws error for the VirtusizeErrorType that it is called on
 * @throws IllegalArgumentException
 */
internal fun VirtusizeErrorType.throwError() {
    throw IllegalArgumentException(this.message())
}