package com.virtusize.android.compose.ui.wip

internal sealed interface VirtusizeButtonUiState {
    data object Idle : VirtusizeButtonUiState

    data object Loading : VirtusizeButtonUiState

    data object Loaded : VirtusizeButtonUiState
}