package com.virtusize.libsource

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.virtusize.libsource.data.local.VirtusizeError
import com.virtusize.libsource.data.local.VirtusizeErrorType
import com.virtusize.libsource.data.remote.ProductCheck


/**
 * This interface can be implemented for passing the result of the successful response of an API request
 */
interface SuccessResponseHandler {
    /**
     * Passes optional data to any object
     * @param data Any invoked data that wants to be passed
     */
    fun onSuccess(@Nullable data: Any? = null)
}

/**
 * This interface can be implemented for passing the result of the error response of an API request
 */
interface ErrorResponseHandler {
    /**
     * Passes the error of type [VirtusizeError]
     * @param error Any error that wants to be passed
     */
    fun onError(@NonNull error: VirtusizeError)
}

/**
 * This interface can be implemented by any class that wants to receive ProductCheckResponse when server returns valid product for product check response
 * @see ProductCheck
 */
interface ValidProductCheckHandler: SuccessResponseHandler {

    /**
     * Invoke this method on object to pass ProductCheckResponse to object
     * @param data of type ProductCheckResponse
     */
    override fun onSuccess(data: Any?) {
        if (data is ProductCheck)
            onValidProductCheckCompleted(data)
    }

    /**
     * Passes productCheckResponse to object
     * @param productCheck is ProductCheckResponse
     * @see ProductCheck
     */
    fun onValidProductCheckCompleted(productCheck: ProductCheck)
}