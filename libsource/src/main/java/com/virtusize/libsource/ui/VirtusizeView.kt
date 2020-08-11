package com.virtusize.libsource.ui

import com.virtusize.libsource.data.local.*
import com.virtusize.libsource.data.remote.ProductCheck

/**
 * An interface for the Virtusize specific views such as VirtusizeButton and VirtusizeInPage
 */
interface VirtusizeView {
    var virtusizeParams: VirtusizeParams?
    fun setup(params: VirtusizeParams, messageHandler: VirtusizeMessageHandler)
    fun setupProductCheckResponseData(productCheck: ProductCheck)
    fun dismissVirtusizeView()
}