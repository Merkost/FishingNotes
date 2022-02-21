package com.mobileprism.fishing.ui.home

sealed class UiState {
    object InProgress : UiState()
    object Error : UiState()
    object Success : UiState()
}
