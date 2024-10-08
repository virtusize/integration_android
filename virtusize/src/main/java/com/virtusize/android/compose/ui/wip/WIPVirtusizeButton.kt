package com.virtusize.android.compose.ui.wip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virtusize.android.R
import com.virtusize.android.compose.ui.VirtusizeButtonColors
import com.virtusize.android.compose.ui.VirtusizeButtonDefaults
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.model.VirtusizeMessage

@Composable
private fun VirtusizeButton(
    product: VirtusizeProduct,
    modifier: Modifier = Modifier,
    colors: VirtusizeButtonColors = VirtusizeButtonDefaults.teal(),
    onEvent: (event: VirtusizeEvent) -> Unit = { _ -> },
    onError: (error: VirtusizeError) -> Unit = { _ -> },
) {
    val context = LocalContext.current
    val viewModel: VirtusizeButtonViewModel = viewModel<VirtusizeButtonViewModel>()

    val state by viewModel.uiStateFlow.collectAsState()
    VirtusizeButton(
        state = state,
        onClick = { viewModel.onButtonClick(context, product) },
        modifier = modifier,
        colors = colors,
    )

    LaunchedEffect(product) {
        viewModel.loadProduct(product)
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
    state: VirtusizeButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = WIPVirtusizeButtonDefaults.shape,
    colors: VirtusizeButtonColors = VirtusizeButtonDefaults.teal(),
    elevation: ButtonElevation? = WIPVirtusizeButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = WIPVirtusizeButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit = emptyContent,
) {
    if (state == VirtusizeButtonUiState.Loaded) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colors.containerColor,
                    contentColor = colors.contentColor,
                ),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
        ) {
            if (content == emptyContent) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_vs_icon_white),
                    contentDescription = null,
                    modifier = Modifier.size(width = 18.dp, height = 13.dp),
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = stringResource(id = R.string.virtusize_button_text),
                    fontSize = 14.sp,
                )
            } else {
                content()
            }
        }
    }
}

private object WIPVirtusizeButtonDefaults {
    val ContentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)

    val shape: Shape @Composable get() = RoundedCornerShape(32.0.dp)

    @Composable
    fun buttonElevation(
        defaultElevation: Dp = Dp.Unspecified,
        pressedElevation: Dp = Dp.Unspecified,
        focusedElevation: Dp = Dp.Unspecified,
        hoveredElevation: Dp = Dp.Unspecified,
        disabledElevation: Dp = Dp.Unspecified,
    ) = ButtonDefaults.buttonElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        disabledElevation = disabledElevation,
    )
}

private val emptyContent: @Composable RowScope.() -> Unit = {}

@Composable
@Preview
private fun WIPVirtusizeButtonPreview() {
    VirtusizeButton(
        state = VirtusizeButtonUiState.Loaded,
        onClick = {},
    )
}
