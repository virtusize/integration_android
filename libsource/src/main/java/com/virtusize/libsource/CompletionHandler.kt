package com.virtusize.libsource

import com.virtusize.libsource.data.pojo.ProductCheckResponse

interface CompletionHandler {
    fun handleEvent(data: Any)
}

interface VirtusizeButtonSetupHandler: CompletionHandler {

    override fun handleEvent(data: Any) {
        setupProduct(data as ProductCheckResponse)
    }

    fun setupProduct(productData: ProductCheckResponse)
}

interface ValidProductCompletionHandler: CompletionHandler {

    override fun handleEvent(data: Any) {
        onValidProductCheckCompleted(data as ProductCheckResponse)
    }

    fun onValidProductCheckCompleted(productData: ProductCheckResponse)
}