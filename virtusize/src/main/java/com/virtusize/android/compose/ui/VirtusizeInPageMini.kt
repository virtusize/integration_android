package com.virtusize.android.compose.ui

import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virtusize.android.compose.theme.VirtusizeColors
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.VirtusizeViewStyle
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.ui.VirtusizeInPageMini
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A composable that displays the VirtusizeInPageMini
 * @param product The product to be linked with the [VirtusizeInPageMini].
 * @param modifier The modifier to be applied to the [VirtusizeInPageMini] layout.
 * @param backgroundColor The background color to be applied to the [VirtusizeInPageMini].
 * @param onEvent The callback to be invoked when an [VirtusizeEvent] is triggered.
 * @param onError The callback to be invoked when an [VirtusizeError] is triggered.
 */
@Composable
fun VirtusizeInPageMini(
    product: VirtusizeProduct,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VirtusizeColors.Black,
    onEvent: (event: VirtusizeEvent) -> Unit = { _ -> },
    onError: (error: VirtusizeError) -> Unit = { _ -> },
) {
    val viewModel: VirtusizeComposeViewModel = viewModel<VirtusizeComposeViewModel>()
    val coroutineScope = rememberCoroutineScope()
    VirtusizeInPageMini(
        modifier = modifier,
        update = { virtusizeInPageMini ->
            viewModel.isLoadedFlow
                .onEach { isLoaded ->
                    if (isLoaded) {
                        virtusizeInPageMini.setInPageMiniBackgroundColor(backgroundColor.toArgb())
                    }
                }
                .launchIn(coroutineScope)
            viewModel.load(product = product, virtusizeView = virtusizeInPageMini)
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

@Composable
private fun VirtusizeInPageMini(
    modifier: Modifier = Modifier,
    update: (VirtusizeInPageMini) -> Unit = NoOpUpdate,
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> VirtusizeInPageMini(context) },
        update = update,
    )
}

@Preview
@Composable
fun VirtusizeInPageMiniPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        VirtusizeInPageMini(
            update = { virtusizeInPageMini ->
                virtusizeInPageMini.visibility = View.VISIBLE
                virtusizeInPageMini.virtusizeViewStyle = VirtusizeViewStyle.TEAL
            },
        )
        VirtusizeInPageMini(
            update = { virtusizeInPageMini ->
                virtusizeInPageMini.visibility = View.VISIBLE
                virtusizeInPageMini.virtusizeViewStyle = VirtusizeViewStyle.BLACK
            },
        )
    }
}
