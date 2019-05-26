package com.virtusize.libsource.model

import com.virtusize.libsource.ui.FitIllustratorButton

// @TODO Implement and use VirtusizeMessageHandler
/**
 * This interface can be implemented by an object to receive Virtusize messages
 */
interface VirtusizeMessageHandler {

    fun virtusizeControllerShouldClose(fitIllustratorButton: FitIllustratorButton)

    fun onEvent(fitIllustratorButton: FitIllustratorButton, event: VirtusizeEvents)

    fun onError(fitIllustratorButton: FitIllustratorButton, error: VirtusizeError)

}