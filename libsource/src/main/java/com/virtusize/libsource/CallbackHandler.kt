package com.virtusize.libsource

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.virtusize.libsource.data.local.VirtusizeError
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
     * Passes optional error of type [VirtusizeError]
     * @param error Any error that wants to be passed
     */
    fun onError(@NonNull error: VirtusizeError)
}

/**
 * This interface can be implemented by any class that wants to receive any form of Data
 * The object of the class can then be used to pass data to that object
 */
interface CallbackHandler {
    /**
     * Passes data to any object whose class implements the CallbackHandler interface
     * @param data Any invoked data that wants to be passed to the object
     */
    fun handleEvent(data: Any)
}

/**
 * This interface can be implemented by any class that wants to receive ProductCheckResponse, that is received from the product data check endpoint
 * @see ProductCheck
 */
interface VirtusizeButtonSetupHandler: CallbackHandler {

    /**
     * Invokes this method to pass ProductCheckResponse to object
     * @param data the data whose type is ProductCheckResponse
     * @see ProductCheck
     */
    override fun handleEvent(data: Any) {
        if (data is ProductCheck)
            setupProductCheckResponseData(data)
    }

    /**
     * This methods sets up productCheckResponse received from Virtusize server to object
     * @param productCheck is ProductCheckResponse
     * @see ProductCheck
     */
    fun setupProductCheckResponseData(productCheck: ProductCheck)
}

/**
 * This interface can be implemented by any class that wants to receive ProductCheckResponse when server returns valid product for product check response
 * @see ProductCheck
 */
interface ValidProductCheckHandler: CallbackHandler {

    /**
     * Invoke this method on object to pass ProductCheckResponse to object
     * @param data of type ProductCheckResponse
     */
    override fun handleEvent(data: Any) {
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