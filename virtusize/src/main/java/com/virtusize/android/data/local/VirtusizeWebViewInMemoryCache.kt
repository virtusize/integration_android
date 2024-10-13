package com.virtusize.android.data.local

import java.util.concurrent.atomic.AtomicReference

internal object VirtusizeWebViewInMemoryCache {
    private val virtusizeMessageHandler: AtomicReference<VirtusizeMessageHandler?> =
        AtomicReference()

    fun setupMessageHandler(messageHandler: VirtusizeMessageHandler) {
        virtusizeMessageHandler.set(messageHandler)
    }

    fun getMessageHandler(): VirtusizeMessageHandler? = virtusizeMessageHandler.getAndSet(null)
}
