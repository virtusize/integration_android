package com.virtusize.android.compose.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virtusize.android.R
import com.virtusize.android.compose.theme.VirtusizeColors
import com.virtusize.android.data.local.VirtusizeError
import com.virtusize.android.data.local.VirtusizeEvent
import com.virtusize.android.data.local.VirtusizeProduct
import com.virtusize.android.model.VirtusizeMessage
import com.virtusize.android.ui.VirtusizeInPageMini

@Composable
fun VirtusizeInPageMini(
    product: VirtusizeProduct,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VirtusizeColors.Black,
    onEvent: (event: VirtusizeEvent) -> Unit = { _ -> },
    onError: (error: VirtusizeError) -> Unit = { _ -> },
) {
    val context = LocalContext.current
    val viewModel: VirtusizePageMiniViewModel = viewModel<VirtusizePageMiniViewModel>()
    val state by viewModel.uiStateFlow.collectAsState()
    VirtusizeInPageMini(
        state = state,
        onClick = { viewModel.onButtonClick(context, product) },
        backgroundColor = backgroundColor,
        modifier = modifier,
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
    state: VirtusizeInPageMiniUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VirtusizeColors.Black,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    if (state is VirtusizeInPageMiniUiState.Hidden) return
    Row(
        modifier =
            modifier
                .height(40.dp)
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        LoadingDottedText(
            text = stringResource(id = R.string.inpage_loading_text),
            modifier = Modifier.weight(1f),
            style =
                TextStyle(
                    color = VirtusizeColors.White,
                    fontSize = 14.sp,
                ),
        )
        if (state is VirtusizeInPageMiniUiState.Success) {
            Spacer(modifier = Modifier.width(4.dp))
            InPageMiniButton(
                onClick = onClick,
                contentColor = backgroundColor,
                modifier = Modifier.height(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
private fun LoadingDottedText(
    text: String,
    modifier: Modifier,
    style: TextStyle,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val animatedDotCount =
        infiniteTransition.animateValue(
            initialValue = 0,
            targetValue = 4,
            typeConverter = Int.VectorConverter,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "ValueAnimation",
        )

    Text(
        text = text + "·".repeat(animatedDotCount.value),
        modifier = modifier,
        style = style,
    )
}

@Composable
private fun InPageMiniButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        color = VirtusizeColors.White,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = border,
    ) {
        Row(
            Modifier
                .padding(start = 8.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.virtusize_button_text),
                fontSize = 14.sp,
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_angle_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun LoadingVirtusizeInPageMiniPreview() {
    VirtusizeInPageMini(
        state = VirtusizeInPageMiniUiState.Loading,
        onClick = {},
        backgroundColor = VirtusizeColors.Teal,
    )
}

@Preview
@Composable
private fun SuccessVirtusizeInPageMiniPreview() {
    VirtusizeInPageMini(
        state = VirtusizeInPageMiniUiState.Success("Find the right size"),
        onClick = {},
        backgroundColor = VirtusizeColors.Black,
    )
}

@Preview
@Composable
private fun ErrorVirtusizeInPageMiniPreview() {
    VirtusizeInPageMini(
        state = VirtusizeInPageMiniUiState.Error,
        onClick = {},
        backgroundColor = VirtusizeColors.Black,
    )
}
