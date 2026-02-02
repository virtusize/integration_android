package com.virtusize.android.compose.ui

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import com.virtusize.android.R
import com.virtusize.android.Virtusize
import com.virtusize.android.compose.theme.VirtusizeColors
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.data.local.VirtusizeViewStyle
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.ui.VirtusizeButton

/**
 * A composable that displays the VirtusizeButton
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
    colors: VirtusizeButtonColors = VirtusizeButtonDefaults.black(),
    onEvent: (event: VirtusizeEvent) -> Unit = { _ -> },
    onError: (error: VirtusizeError) -> Unit = { _ -> },
) {
    val viewModel: VirtusizeComposeViewModel = viewModel<VirtusizeComposeViewModel>()
    val viewRef = remember { mutableStateOf<VirtusizeButton?>(null) }

    VirtusizeButton(
        modifier = modifier,
        update = { virtusizeButton ->
            viewRef.value = virtusizeButton
            viewModel.load(product = product, virtusizeView = virtusizeButton)
        },
    )

    LaunchedEffect(Unit) {
        viewModel.isLoadedFlow.collect { isLoaded ->
            if (isLoaded) {
                viewRef.value?.let { virtusizeButton ->
                    virtusizeButton.virtusizeViewStyle = VirtusizeViewStyle.BLACK
                    virtusizeButton.setRoundedCornerBackground(colors.containerColor.toArgb())
                    colors.contentColor.toArgb().apply {
                        virtusizeButton.setVirtusizeLogoTint(this)
                        virtusizeButton.setTextColor(this)
                    }
                }
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
private fun VirtusizeButton(
    modifier: Modifier = Modifier,
    style: VirtusizeViewStyle = VirtusizeViewStyle.BLACK,
    update: (VirtusizeButton) -> Unit = NoOpUpdate,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VirtusizeButton(context)
        },
        update = { virtusizeButton ->
            virtusizeButton.virtusizeViewStyle = style
            update(virtusizeButton)
        },
        onRelease = { virtusizeView ->
            Virtusize.getInstanceOrNull()?.cleanupVirtusizeView(virtusizeView)
        },
    )
}

/**
 * Represents the container and content colors used in a [VirtusizeButton].
 * @param containerColor the container color of this [VirtusizeButton].
 * @param contentColor the content color of this [VirtusizeButton].
 */
@Immutable
data class VirtusizeButtonColors internal constructor(
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
    val horizontalPadding =
        resources.getDimension(R.dimen.virtusize_button_horizontal_padding).toInt()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        drawable.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        background = drawable
    } else {
        val layers = arrayOf<Drawable>(drawable)
        val layerDrawable = LayerDrawable(layers)
        layerDrawable.setLayerInset(
            0,
            horizontalPadding,
            verticalPadding,
            horizontalPadding,
            verticalPadding,
        )
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

@Composable
@Preview
private fun VirtusizeButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val update: (VirtusizeButton) -> Unit = { virtusizeButton ->
            virtusizeButton.text = virtusizeButton.context.resources.getText(R.string.virtusize_button_text)
            virtusizeButton.visibility = View.VISIBLE
        }
        VirtusizeButton(
            style = VirtusizeViewStyle.TEAL,
            update = update,
        )
        VirtusizeButton(
            style = VirtusizeViewStyle.BLACK,
            update = update,
        )
    }
}
