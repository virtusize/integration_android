package com.virtusize.android.compose.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.virtusize.android.Virtusize
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeEvents
import com.virtusize.android.data.local.VirtusizeMessageHandler
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.getEventName
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.util.VirtusizeUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class VirtusizeButtonViewModel : ViewModel() {
    private val mutableUiStateFlow = MutableStateFlow<VirtusizeButtonUiState>(VirtusizeButtonUiState.Hidden)
    val uiStateFlow: StateFlow<VirtusizeButtonUiState> = mutableUiStateFlow.asStateFlow()

    private val mutableMessageFlow: Channel<VirtusizeMessage> by lazy { Channel() }
    val messageFlow = mutableMessageFlow.receiveAsFlow()

    private val virtusize: Virtusize by lazy { Virtusize.getInstance() }

    private val messageHandler =
        object : VirtusizeMessageHandler {
            override fun onEvent(
                product: VirtusizeProduct,
                event: VirtusizeEvent,
            ) {
                if (event.name == VirtusizeEvents.UserSawWidgetButton.getEventName()) {
                    mutableUiStateFlow.tryEmit(VirtusizeButtonUiState.Shown)
                }
                mutableMessageFlow.trySend(VirtusizeMessage.Event(product, event))
            }

            override fun onError(error: VirtusizeError) {
                mutableMessageFlow.trySend(VirtusizeMessage.Error(error))
            }
        }

    init {
        virtusize.registerMessageHandler(messageHandler)
    }

    fun loadProduct(product: VirtusizeProduct) {
        virtusize.load(product)
    }

    fun onButtonClick(
        context: Context,
        product: VirtusizeProduct,
    ) {
        VirtusizeUtils.openVirtusizeWebView(
            context,
            virtusize.params,
            product,
            messageHandler,
        )
    }

    override fun onCleared() {
        super.onCleared()
        virtusize.unregisterMessageHandler(messageHandler)
    }
}
