package com.virtusize.android.compose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtusize.android.Virtusize
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeEvents
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.getEventName
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.ui.VirtusizeView
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

internal class VirtusizeComposeViewModel : ViewModel() {
    private val virtusize: Virtusize by lazy { Virtusize.getInstance() }

    private val mutableMessageFlow: Channel<VirtusizeMessage> by lazy { Channel() }
    val messageFlow: SharedFlow<VirtusizeMessage> =
        mutableMessageFlow.receiveAsFlow()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
            )

    val isLoadedFlow: StateFlow<Boolean> =
        messageFlow
            .filter { message -> message is VirtusizeMessage.Event && message.event.name == VirtusizeEvents.UserSawProduct.getEventName() }
            .map { true }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = false,
            )

    private val messageHandler =
        object : VirtusizeMessageHandler {
            override fun onEvent(
                product: VirtusizeProduct,
                event: VirtusizeEvent,
            ) {
                mutableMessageFlow.trySend(VirtusizeMessage.Event(product, event))
            }

            override fun onError(error: VirtusizeError) {
                mutableMessageFlow.trySend(VirtusizeMessage.Error(error))
            }
        }

    init {
        virtusize.registerMessageHandler(messageHandler)
    }

    fun load(
        product: VirtusizeProduct,
        virtusizeView: VirtusizeView,
    ) {
        virtusize.load(product)
        virtusize.setupVirtusizeView(product = product, virtusizeView = virtusizeView)
    }

    override fun onCleared() {
        super.onCleared()
        virtusize.unregisterMessageHandler(messageHandler)
    }
}
