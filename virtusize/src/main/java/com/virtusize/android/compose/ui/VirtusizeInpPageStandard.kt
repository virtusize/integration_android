package com.virtusize.android.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virtusize.android.compose.theme.VirtusizeColors
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.ui.VirtusizeInPageStandard
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun VirtusizeInPageStandard(
    product: VirtusizeProduct,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VirtusizeColors.Black,
    onEvent: (event: VirtusizeEvent) -> Unit = { _ -> },
    onError: (error: VirtusizeError) -> Unit = { _ -> },
) {
    val viewModel: VirtusizeComposeViewModel = viewModel<VirtusizeComposeViewModel>()
    val coroutineScope = rememberCoroutineScope()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VirtusizeInPageStandard(context).apply {
                horizontalMargin = 0
            }
        },
        update = { virtusizeInPageStandard ->
            viewModel.isLoadedFlow
                .onEach { isLoaded ->
                    if (isLoaded) {
                        virtusizeInPageStandard.setButtonBackgroundColor(backgroundColor.toArgb())
                    }
                }
                .launchIn(coroutineScope)
            viewModel.load(product = product, virtusizeView = virtusizeInPageStandard)
        },
    )

    LaunchedEffect(Unit) {
        viewModel.messageFlow.collect { message ->
            when (message) {
                is VirtusizeMessage.Event -> onEvent(message.event)
                is VirtusizeMessage.Error -> onError(message.error)
            }
        }
    }
}
