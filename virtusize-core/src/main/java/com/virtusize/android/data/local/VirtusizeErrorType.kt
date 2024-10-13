package com.virtusize.android.data.local

import java.lang.IllegalArgumentException
import java.net.HttpURLConnection

/**
 * This enum contains all available error types in Virtusize library
 */
enum class VirtusizeErrorType {
    NullVirtusizeViewError,
    ApiKeyNullOrInvalid,
    UserIdNullOrEmpty,
    NullContext,
    ImageUrlNotValid,
    NullProduct,
    UnParsedProduct,
    InvalidProduct,
    APIError,
    JsonParsingError,
    WardrobeNotFound,
    PrivacyLinkNotOpen,
}

/**
 * Returns an error code for the VirtusizeErrorType that it is called on
 * @return the error code for the VirtusizeErrorType
 */
fun VirtusizeErrorType.code(): Int? =
    when (this) {
        VirtusizeErrorType.ApiKeyNullOrInvalid -> HttpURLConnection.HTTP_FORBIDDEN
        VirtusizeErrorType.InvalidProduct,
        VirtusizeErrorType.UnParsedProduct,
        VirtusizeErrorType.WardrobeNotFound,
        -> HttpURLConnection.HTTP_NOT_FOUND
        else -> null
    }

/**
 * Returns an error message for the VirtusizeErrorType that it is called on
 * @return the error message for the VirtusizeErrorType
 */
fun VirtusizeErrorType.message(extraMessage: String? = null): String =
    when (this) {
        VirtusizeErrorType.NullVirtusizeViewError ->
            "The Virtusize view in the layout is null."
        VirtusizeErrorType.ApiKeyNullOrInvalid ->
            "The Virtusize API key is not set or invalid."
        VirtusizeErrorType.UserIdNullOrEmpty ->
            "The unique user ID from the client system is not set up or empty."
        VirtusizeErrorType.NullContext ->
            "Context can not be null."
        VirtusizeErrorType.ImageUrlNotValid ->
            "The image URL is invalid."
        VirtusizeErrorType.NullProduct ->
            "The store product is null. Please set up your store product"
        VirtusizeErrorType.InvalidProduct ->
            "The store product $extraMessage is not valid in the Virtusize server"
        VirtusizeErrorType.UnParsedProduct ->
            "The store product $extraMessage is not parsed in the Virtusize server yet"
        VirtusizeErrorType.APIError ->
            "Virtusize API error: $extraMessage"
        VirtusizeErrorType.JsonParsingError ->
            "JSON response parsing error: $extraMessage"
        VirtusizeErrorType.WardrobeNotFound ->
            "The user's wardrobe hasn't been created in the Virtusize server yet"
        VirtusizeErrorType.PrivacyLinkNotOpen ->
            "The privacy link can not be open. The error is: $extraMessage"
    }

/**
 * Returns the [VirtusizeError] corresponding to the VirtusizeErrorType
 * @param code an error code
 * @param extraMessage an extra error message specific to this error type
 * @return the [VirtusizeError] for the VirtusizeErrorType
 */
fun VirtusizeErrorType.virtusizeError(
    code: Int? = null,
    extraMessage: String? = null,
): VirtusizeError = VirtusizeError(this, code ?: this.code(), this.message(extraMessage))

/**
 * Throws error for the VirtusizeErrorType that it is called on
 * @throws IllegalArgumentException
 */
fun VirtusizeErrorType.throwError(): Nothing = throw IllegalArgumentException(this.message())
