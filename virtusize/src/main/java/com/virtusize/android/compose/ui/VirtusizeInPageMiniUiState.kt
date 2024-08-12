package com.virtusize.android.compose.ui

internal sealed interface VirtusizeInPageMiniUiState {
    data object Hidden : VirtusizeInPageMiniUiState

    data object Loading : VirtusizeInPageMiniUiState

    data class Success(val sizeRecommendationText: String) : VirtusizeInPageMiniUiState

    data object Error : VirtusizeInPageMiniUiState
}
