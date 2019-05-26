package com.virtusize.libsource.model

import java.lang.IllegalArgumentException

/**
 * This enum contains all available errors in virtusize library
 */
enum class VirtusizeError{
    NullFitButtonError,
    ApiKeyNullOrEmpty,
    NullContext,
    ImageUrlNotValid,
    InvalidProduct,
    FitIllustratorError
}

/**
 * This method returns error message for the VirtusizeError that it is called on
 * @return Error message
 */
fun VirtusizeError.message(): String {
    return when(this) {
        VirtusizeError.NullFitButtonError -> "Fit Illustrator Button is null."
        VirtusizeError.ApiKeyNullOrEmpty-> "Api Key can not be null or empty."
        VirtusizeError.NullContext -> "Context can not be null"
        VirtusizeError.ImageUrlNotValid -> "Image URL is invalid"
        VirtusizeError.InvalidProduct -> "Product can not null"
        VirtusizeError.FitIllustratorError -> "Failed to load fit illustrator"
    }
}

/**
 * This method throws error for the VirtusizeError that it is called on
 * @throws IllegalArgumentException
 */
fun VirtusizeError.throwError() {
    throw IllegalArgumentException(this.message())
}