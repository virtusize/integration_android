package com.virtusize.android.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
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
import com.virtusize.android.R
import com.virtusize.android.compose.theme.VirtusizeColors
import com.virtusize.android.data.local.VirtusizeProduct

@Composable
fun VirtusizeButton(
    product: VirtusizeProduct,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: VirtusizeButtonColors? = null,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
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
            )
            Text(text = stringResource(id = R.string.virtusize_button_text))
        } else {
            content()
        }
    }

    LaunchedEffect(product) {
        virtusizeButtonViewModel.loadProduct(product)
    }
}

object VirtusizeButtonDefaults {
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
