package com.virtusize.android.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtusize.android.R
import com.virtusize.android.compose.theme.VirtusizeColors

@Composable
fun VirtusizeInPageMini(
    modifier: Modifier = Modifier,
    backgroundColor: Color = VirtusizeColors.Black,
) {
    Row(
        modifier =
            modifier
                .height(40.dp)
                .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(id = R.string.inpage_loading_text),
            modifier = Modifier.weight(1f),
            color = VirtusizeColors.White,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.width(4.dp))
        InPageMiniButton(
            onClick = { /*TODO*/ },
            contentColor = backgroundColor,
            modifier = Modifier.height(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
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
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        color = VirtusizeColors.White,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = border,
        interactionSource = interactionSource,
    ) {
        Row(
            Modifier.padding(start = 8.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
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
private fun VirtusizeInPageMiniPreview() {
    VirtusizeInPageMini()
}
