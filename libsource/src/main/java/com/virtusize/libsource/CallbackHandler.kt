package com.virtusize.libsource

import com.virtusize.libsource.data.pojo.ProductCheckResponse

/**
 * This interface can be implemented by any class that wants to receive any form of Data
 * The object of the class can then be used to pass data to that object
 */
interface CallbackHandler {
    /**
     * This method is used to pass data to any object whose class implements CallbackHandler interface
     * @param data Any data that invoked wants to pass to the object
     */
    fun handleEvent(data: Any)
}

/**
 * This interface can be implemented by any class that wants to receive ProductCheckResponse, that is received from product data check endpoint
 * @see ProductCheckResponse
 */
interface VirtusizeButtonSetupHandler: CallbackHandler {

    /**
     * Invoke this method on object to pass ProductCheckResponse to object
     * @param data of type ProductCheckResponse
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
     * This methods passes productCheckResponse to object
     * @param productCheckResponse is ProductCheckResponse
     * @see ProductCheckResponse
     */
    fun onValidProductCheckCompleted(productCheckResponse: ProductCheckResponse)
}