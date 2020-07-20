package com.virtusize.libsource.data.local

import com.virtusize.libsource.ui.VirtusizeButton

/**
 * This interface can be implemented by an object to receive Virtusize specific messages
 */
interface VirtusizeMessageHandler {

    fun virtusizeControllerShouldClose(virtusizeButton: VirtusizeButton)

    fun onEvent(virtusizeButton: VirtusizeButton?, event: VirtusizeEvent)

    fun onError(virtusizeButton: VirtusizeButton?, error: VirtusizeError)
}