package com.virtusize.libsource.data.local

import com.virtusize.libsource.ui.FitIllustratorButton

/**
 * This interface can be implemented by an object to receive Virtusize specific messages
 */
interface VirtusizeMessageHandler {

    fun virtusizeControllerShouldClose(fitIllustratorButton: FitIllustratorButton)

    fun onEvent(fitIllustratorButton: FitIllustratorButton?, event: VirtusizeEvents)

    fun onError(fitIllustratorButton: FitIllustratorButton?, error: VirtusizeError)

}