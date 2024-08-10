package com.virtusize.android.model

import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeProduct

internal sealed interface VirtusizeMessage {
    data class Event(
        val product: VirtusizeProduct,
        val event: VirtusizeEvent,
    ) : VirtusizeMessage

    data class Error(
        val error: VirtusizeError,
    ) : VirtusizeMessage
}
