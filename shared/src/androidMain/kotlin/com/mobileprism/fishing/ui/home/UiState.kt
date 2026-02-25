package com.mobileprism.fishing.ui.home

import androidx.compose.runtime.Immutable

@Immutable
sealed class UiState {
    object InProgress : UiState()
    object Error : UiState()
    object Success : UiState()
}
