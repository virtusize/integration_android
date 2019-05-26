package com.virtusize.libsource

import com.virtusize.libsource.data.pojo.ProductCheckResponse

interface CallbackHandler {
    fun handleEvent(data: Any)
}

interface VirtusizeButtonSetupHandler: CallbackHandler {

    override fun handleEvent(data: Any) {
        setupProduct(data as ProductCheckResponse)
    }

    fun setupProduct(productData: ProductCheckResponse)
}

interface ValidProductFetchHandler: CallbackHandler {

    override fun handleEvent(data: Any) {
        onValidProductCheckCompleted(data as ProductCheckResponse)
    }

    fun onValidProductCheckCompleted(productData: ProductCheckResponse)
}