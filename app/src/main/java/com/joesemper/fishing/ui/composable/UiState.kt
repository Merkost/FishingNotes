package com.joesemper.fishing.ui.composable

sealed class UiState {
    object InProgress : UiState()
    object Error : UiState()
    object Success : UiState()
}
