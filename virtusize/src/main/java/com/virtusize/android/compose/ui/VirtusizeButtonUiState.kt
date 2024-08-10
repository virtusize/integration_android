package com.virtusize.android.compose.ui

internal sealed interface VirtusizeButtonUiState {
    data object Hidden : VirtusizeButtonUiState

    data object Shown : VirtusizeButtonUiState
}
