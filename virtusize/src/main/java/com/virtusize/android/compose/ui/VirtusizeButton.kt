package com.virtusize.android.compose.ui

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
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
import com.virtusize.android.R
import com.virtusize.android.compose.theme.VirtusizeColors
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.VirtusizeViewStyle
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.ui.VirtusizeButton
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A button composable that displays the Virtusize button
 * @param product The product to be linked with the button.
 * @param modifier The modifier to be applied to the button layout.
 * @param colors The colors to be applied to the button.
 * @param onEvent The callback to be invoked when an [VirtusizeEvent] is triggered.
 * @param onError The callback to be invoked when an [VirtusizeError] is triggered.
 */
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
                        virtusizeButton.setRoundedCornerBackground(colors.containerColor.toArgb())
                        colors.contentColor.toArgb().apply {
                            virtusizeButton.setVirtusizeLogoTint(this)
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

/**
 * Represents the container and content colors used in a [VirtusizeButton].
 * @param containerColor the container color of this [VirtusizeButton].
 * @param contentColor the content color of this [VirtusizeButton].
 */
data class VirtusizeButtonColors(
    val containerColor: Color,
    val contentColor: Color,
)

/**
 * Contains the default values used by [VirtusizeButton].
 */
object VirtusizeButtonDefaults {
    /**
     * Creates a [VirtusizeButtonColors] that represents the default container and content colors used for the [teal] style.
     */
    @Composable
    fun teal(): VirtusizeButtonColors =
        VirtusizeButtonColors(
            containerColor = VirtusizeColors.Teal,
            contentColor = VirtusizeColors.White,
        )

    /**
     * Creates a [VirtusizeButtonColors] that represents the default container and content colors used for the [black] style.
     */
    @Composable
    fun black(): VirtusizeButtonColors =
        VirtusizeButtonColors(
            containerColor = VirtusizeColors.Black,
            contentColor = VirtusizeColors.White,
        )

    /**
     * Creates a [VirtusizeButtonColors] that represents the default container and content colors used in a [VirtusizeButton].
     * @param containerColor the container color of this [VirtusizeButton].
     * @param contentColor the content color of this [VirtusizeButton].
     */
    @Composable
    fun colors(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
    ): VirtusizeButtonColors =
        VirtusizeButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        )
}

/**
 * A extension function to set rounded corner background of the [VirtusizeButton].
 * @param backgroundColor The color to be set as the background of the [VirtusizeButton].
 */
private fun VirtusizeButton.setRoundedCornerBackground(
    @ColorInt backgroundColor: Int,
) {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.RECTANGLE
    val cornerRadius = resources.getDimension(R.dimen.virtusize_button_corner_radius)
    drawable.cornerRadii =
        floatArrayOf(
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
        )
    drawable.setColor(backgroundColor)
    val verticalPadding = resources.getDimension(R.dimen.virtusize_button_vertical_padding).toInt()
    val horizontalPadding = resources.getDimension(R.dimen.virtusize_button_horizontal_padding).toInt()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        drawable.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        background = drawable
    } else {
        val layers = arrayOf<Drawable>(drawable)
        val layerDrawable = LayerDrawable(layers)
        layerDrawable.setLayerInset(0, horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        background = layerDrawable.getDrawable(0)
    }
}

/**
 * A extension function to specify the Virtusize logo tint color of the [VirtusizeButton].
 */
private fun VirtusizeButton.setVirtusizeLogoTint(
    @ColorInt iconTint: Int,
) {
    compoundDrawables.getOrNull(0)?.setTint(iconTint)
}
