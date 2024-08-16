package com.virtusize.android.compose.ui

import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.annotation.ColorInt
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
import com.virtusize.android.data.local.VirtusizeViewStyle
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.ui.VirtusizeButton
import com.virtusize.android.util.dpInPx
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun VirtusizeButton(
    product: VirtusizeProduct,
    modifier: Modifier = Modifier,
    colors: VirtusizeButtonColors = VirtusizeButtonDefaults.teal(),
    onEvent: (event: VirtusizeEvent) -> Unit = { _ -> },
    onError: (error: VirtusizeError) -> Unit = { _ -> },
) {
    val viewModel: VirtusizeComposeViewModel = viewModel<VirtusizeComposeViewModel>()
    val coroutineScope = rememberCoroutineScope()
    AndroidView(
        modifier = modifier,
        factory = { context -> VirtusizeButton(context) },
        update = { virtusizeButton ->
            viewModel.isLoadedFlow
                .onEach { isLoaded ->
                    if (isLoaded) {
                        virtusizeButton.virtusizeViewStyle = VirtusizeViewStyle.BLACK
                        virtusizeButton.setRoundedBackgroundColor(colors.containerColor.toArgb())
                        colors.contentColor.toArgb().apply {
                            virtusizeButton.setLogoTint(this)
                            virtusizeButton.setTextColor(this)
                        }
                    }
                }
                .launchIn(coroutineScope)
            viewModel.load(product = product, virtusizeView = virtusizeButton)
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

private fun VirtusizeButton.setRoundedBackgroundColor(
    @ColorInt backgroundColor: Int,
) {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.RECTANGLE
    shape.cornerRadii =
        floatArrayOf(
            32.dpInPx.toFloat(),
            32.dpInPx.toFloat(),
            32.dpInPx.toFloat(),
            32.dpInPx.toFloat(),
            32.dpInPx.toFloat(),
            32.dpInPx.toFloat(),
            32.dpInPx.toFloat(),
            32.dpInPx.toFloat(),
        )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        shape.setPadding(12.dpInPx, 10.dpInPx, 12.dpInPx, 10.dpInPx)
    }
    shape.setColor(backgroundColor)
    background = shape
}

private fun VirtusizeButton.setLogoTint(
    @ColorInt iconTint: Int,
) {
    compoundDrawables.getOrNull(0)?.setTint(iconTint)
}

data class VirtusizeButtonColors(
    val containerColor: Color,
    val contentColor: Color,
)

internal object VirtusizeButtonDefaults {
    @Composable
    fun teal(
        containerColor: Color = VirtusizeColors.Teal,
        contentColor: Color = VirtusizeColors.White,
    ): VirtusizeButtonColors =
        VirtusizeButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        )

    @Composable
    fun black(
        containerColor: Color = VirtusizeColors.Black,
        contentColor: Color = VirtusizeColors.White,
    ): VirtusizeButtonColors =
        VirtusizeButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        )
}
