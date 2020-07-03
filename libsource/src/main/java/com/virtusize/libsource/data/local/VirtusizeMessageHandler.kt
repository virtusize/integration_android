package com.virtusize.libsource.data.local

import com.virtusize.libsource.ui.AoyamaButton

/**
 * This interface can be implemented by an object to receive Virtusize specific messages
 */
interface VirtusizeMessageHandler {

    fun virtusizeControllerShouldClose(aoyamaButton: AoyamaButton)

    fun onEvent(aoyamaButton: AoyamaButton?, event: VirtusizeEvents)

    fun onError(aoyamaButton: AoyamaButton?, error: VirtusizeError)
}