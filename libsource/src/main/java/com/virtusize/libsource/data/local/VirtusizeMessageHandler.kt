package com.virtusize.libsource.data.local

import com.virtusize.libsource.ui.VirtusizeView

/**
 * This interface can be implemented by an object to receive Virtusize specific messages
 */
interface VirtusizeMessageHandler {
    fun onEvent(product: VirtusizeProduct, event: VirtusizeEvent)
    fun onError(error: VirtusizeError)
}