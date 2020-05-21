package com.virtusize.libsource

import com.virtusize.libsource.data.pojo.ProductCheckResponse
import com.virtusize.libsource.model.VirtusizeError

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
 * This interface can be implemented by class that wants to receive VirtusizeError
 * The object of the class can then be used to pass an error to that object
 */
interface ErrorHandler {
    /**
     * Passes an error to any object whose class implements the ErrorHandler interface
     * @param error VirtusizeError
     */
    fun onError(error: VirtusizeError)
}
/**
 * This interface can be implemented by any class that wants to receive ProductCheckResponse, that is received from the product data check endpoint
 * @see ProductCheckResponse
 */
interface VirtusizeButtonSetupHandler: CallbackHandler {

    /**
     * Invokes this method to pass ProductCheckResponse to object
     * @param data the data whose type is ProductCheckResponse
     * @see ProductCheckResponse
     */
    override fun handleEvent(data: Any) {
        if (data is ProductCheckResponse)
            setupProductCheckResponseData(data)
    }

    /**
     * This methods sets up productCheckResponse received from Virtusize server to object
     * @param productCheckResponse is ProductCheckResponse
     * @see ProductCheckResponse
     */
    fun setupProductCheckResponseData(productCheckResponse: ProductCheckResponse)
}

/**
 * This interface can be implemented by any class that wants to receive ProductCheckResponse when server returns valid product for product check response
 * @see ProductCheckResponse
 */
interface ValidProductCheckHandler: CallbackHandler {

    /**
     * Invoke this method on object to pass ProductCheckResponse to object
     * @param data of type ProductCheckResponse
     */
    override fun handleEvent(data: Any) {
        if (data is ProductCheckResponse)
            onValidProductCheckCompleted(data)
    }

    /**
     * Passes productCheckResponse to object
     * @param productCheckResponse is ProductCheckResponse
     * @see ProductCheckResponse
     */
    fun onValidProductCheckCompleted(productCheckResponse: ProductCheckResponse)
}