package com.virtusize.sampleapp.compose

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.virtusize.android.compose.ui.VirtusizeButton
import com.virtusize.android.data.local.VirtusizeProduct

@Composable
internal fun VirtusizeSampleApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val product by remember {
                mutableStateOf(
                    VirtusizeProduct(
                        externalId = "vs_dress",
                        imageUrl = "http://www.image.com/goods/12345.jpg",
                    ),
                )
            }

            VirtusizeButton(
                product = product,
                onEvent = { event ->
                    Log.i(VIRTUSIZE_BUTTON_TAG, event.name)
                },
                onError = { error ->
                    Log.e(VIRTUSIZE_BUTTON_TAG, error.message)
                },
            )
        }
    }
}

@Preview
@Composable
private fun VirtusizeSampleAppPreview() {
    VirtusizeSampleApp()
}

private const val VIRTUSIZE_BUTTON_TAG = "VirtusizeButton"
