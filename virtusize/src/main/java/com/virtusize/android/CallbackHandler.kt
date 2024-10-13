package com.virtusize.android

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.virtusize.android.data.local.VirtusizeError

/**
 * This interface can be implemented for passing the result of the successful response of an API request
 */
interface SuccessResponseHandler {
    /**
     * Passes optional data to any object
     * @param data Any invoked data that wants to be passed
     */
    fun onSuccess(
        @Nullable data: Any? = null,
    )
}

/**
 * This interface can be implemented for passing the result of the error response of an API request
 */
interface ErrorResponseHandler {
    /**
     * Passes the error of type [VirtusizeError]
     * @param error Any error that wants to be passed
     */
    fun onError(
        @NonNull error: VirtusizeError,
    )
}
