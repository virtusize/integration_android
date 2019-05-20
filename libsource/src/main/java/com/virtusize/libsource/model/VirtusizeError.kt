package com.virtusize.libsource.model

import java.lang.IllegalArgumentException

enum class VirtusizeError{
    NullFitButtonError,
    ApiKeyNullOrEmpty,
    NullContext,
    ImageUrlNotValid,
    InvalidProduct,
    FitIllustratorError
}

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

fun VirtusizeError.throwError() {
    throw IllegalArgumentException(this.message())
}