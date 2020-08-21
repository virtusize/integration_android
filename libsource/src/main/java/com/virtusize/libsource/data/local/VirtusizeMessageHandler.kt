package com.virtusize.libsource.data.local

import com.virtusize.libsource.ui.VirtusizeView

/**
 * This interface can be implemented by an object to receive Virtusize specific messages
 */
interface VirtusizeMessageHandler {

    fun virtusizeControllerShouldClose(virtusizeView: VirtusizeView)

    fun onEvent(virtusizeView: VirtusizeView?, event: VirtusizeEvent)

    fun onError(virtusizeView: VirtusizeView?, error: VirtusizeError)
}