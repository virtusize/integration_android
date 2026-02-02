package com.virtusize.android.compose.ui

import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virtusize.android.Virtusize
import com.virtusize.android.compose.theme.VirtusizeColors
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.VirtusizeViewStyle
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.ui.VirtusizeInPageStandard

/**
 * A composable that displays the VirtusizeInPageStandard
 * @param product The product to be linked with the [VirtusizeInPageStandard].
 * @param modifier The modifier to be applied to the [VirtusizeInPageStandard] layout.
 * @param backgroundColor The background color to be applied to the [VirtusizeInPageStandard].
 * @param onEvent The callback to be invoked when an [VirtusizeEvent] is triggered.
 * @param onError The callback to be invoked when an [VirtusizeError] is triggered.
 */
@Composable
fun VirtusizeInPageStandard(
    product: VirtusizeProduct,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VirtusizeColors.Black,
    onEvent: (event: VirtusizeEvent) -> Unit = { _ -> },
    onError: (error: VirtusizeError) -> Unit = { _ -> },
) {
    val viewModel: VirtusizeComposeViewModel = viewModel<VirtusizeComposeViewModel>()
    val viewRef = remember { mutableStateOf<VirtusizeInPageStandard?>(null) }

    VirtusizeInPageStandard(
        modifier = modifier,
        update = { virtusizeInPageStandard ->
            viewRef.value = virtusizeInPageStandard
            viewModel.load(product = product, virtusizeView = virtusizeInPageStandard)
        },
    )

    LaunchedEffect(Unit) {
        viewModel.isLoadedFlow.collect { isLoaded ->
            if (isLoaded) {
                viewRef.value?.setButtonBackgroundColor(backgroundColor.toArgb())
            }
        }
    }

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
private fun VirtusizeInPageStandard(
    modifier: Modifier = Modifier,
    update: (VirtusizeInPageStandard) -> Unit = NoOpUpdate,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VirtusizeInPageStandard(context).apply {
                horizontalMargin = 0
                clipChildren = false
            }
        },
        update = update,
        onRelease = { virtusizeView ->
            Virtusize.getInstanceOrNull()?.cleanupVirtusizeView(virtusizeView)
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun VirtusizeInPageStandardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        VirtusizeInPageStandard(
            modifier = Modifier.padding(16.dp),
            update = { virtusizeInPageStandard ->
                virtusizeInPageStandard.visibility = View.VISIBLE
                virtusizeInPageStandard.virtusizeViewStyle = VirtusizeViewStyle.TEAL
            },
        )
        VirtusizeInPageStandard(
            modifier = Modifier.padding(16.dp),
            update = { virtusizeInPageStandard ->
                virtusizeInPageStandard.visibility = View.VISIBLE
                virtusizeInPageStandard.virtusizeViewStyle = VirtusizeViewStyle.BLACK
            },
        )
    }
}
