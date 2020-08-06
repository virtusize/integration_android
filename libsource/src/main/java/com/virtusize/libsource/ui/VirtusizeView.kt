package com.virtusize.libsource.ui

import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck

interface VirtusizeView {
    var virtusizeParams: VirtusizeParams?
    fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler)
    fun setupProductCheckResponseData(productCheck: ProductCheck)
    fun dismissVirtusizeView()
}