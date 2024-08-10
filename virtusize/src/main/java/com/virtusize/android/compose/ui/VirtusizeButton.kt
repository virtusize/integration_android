package com.virtusize.android.compose.ui

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtusize.android.R
import com.virtusize.android.compose.theme.VirtusizeColors
import com.virtusize.android.data.local.VirtusizeProduct

@Composable
fun VirtusizeButton(
    product: VirtusizeProduct,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    shape: Shape = VirtusizeButtonDefaults.shape,
    colors: VirtusizeButtonColors? = null,
    elevation: ButtonElevation? = VirtusizeButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = VirtusizeButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit = emptyContent,
) {
    val virtusizeButtonViewModel = VirtusizeButtonViewModel()
    val context = LocalContext.current
    Button(
        onClick = { virtusizeButtonViewModel.onButtonClick(context, product) },
        modifier = modifier,
        enabled = enable,
        shape = shape,
        colors =
            colors?.let {
                ButtonDefaults.buttonColors(
                    containerColor = colors.containerColor,
                    contentColor = colors.contentColor,
                    disabledContainerColor = colors.disabledContainerColor,
                    disabledContentColor = colors.disabledContentColor,
                )
            } ?: ButtonDefaults.buttonColors(),
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

    LaunchedEffect(product) {
        virtusizeButtonViewModel.loadProduct(product)
    }
}

object VirtusizeButtonDefaults {
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

    @Composable
    fun teal(
        containerColor: Color = VirtusizeColors.Teal,
        contentColor: Color = VirtusizeColors.White,
        disabledContainerColor: Color = Color.Unspecified,
        disabledContentColor: Color = Color.Unspecified,
    ): VirtusizeButtonColors =
        VirtusizeButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )

    @Composable
    fun black(
        containerColor: Color = VirtusizeColors.Black,
        contentColor: Color = VirtusizeColors.White,
        disabledContainerColor: Color = Color.Unspecified,
        disabledContentColor: Color = Color.Unspecified,
    ): VirtusizeButtonColors =
        VirtusizeButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )
}

data class VirtusizeButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)

private val emptyContent: @Composable RowScope.() -> Unit = {}

@Composable
@Preview
private fun VirtusizeButtonPreview() {
    VirtusizeButton(
        product =
            VirtusizeProduct(
                "694",
                "http://www.image.com/goods/12345.jpg",
            ),
        colors = VirtusizeButtonDefaults.teal(),
    )
}
