package com.virtusize.libsource.data.local

import java.lang.IllegalArgumentException

/**
 * This enum contains all available errors in Virtusize library
 */
enum class VirtusizeError{
    NullFitButtonError,
    ApiKeyNullOrEmpty,
    UserIdNullOrEmpty,
    NullContext,
    ImageUrlNotValid,
    InvalidProduct,
    FitIllustratorError,
    NetworkError
}

/**
 * Returns an error message for the VirtusizeError that it is called on
 * @return the error message for the VirtusizeError
 */
fun VirtusizeError.message(): String {
    return when(this) {
        VirtusizeError.NullFitButtonError -> "Fit Illustrator Button is null."
        VirtusizeError.ApiKeyNullOrEmpty -> "Api Key can not be null or empty."
        VirtusizeError.UserIdNullOrEmpty -> "The unique user ID from the client system is not set up or empty"
        VirtusizeError.NullContext -> "Context can not be null"
        VirtusizeError.ImageUrlNotValid -> "Image URL is invalid"
        VirtusizeError.InvalidProduct -> "Product can not null"
        VirtusizeError.FitIllustratorError -> "Failed to load fit illustrator"
        VirtusizeError.NetworkError -> "Network error"
    }
}

/**
 * Throws error for the VirtusizeError that it is called on
 * @throws IllegalArgumentException
 */
fun VirtusizeError.throwError() {
    throw IllegalArgumentException(this.message())
}