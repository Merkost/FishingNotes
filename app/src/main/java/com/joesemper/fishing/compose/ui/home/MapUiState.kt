package com.joesemper.fishing.compose.ui.home

sealed class MapUiState {
    object NormalMode : MapUiState()
    object PlaceSelectMode : MapUiState()
    object BottomSheetAddMode : MapUiState()
    object BottomSheetInfoMode : MapUiState()
}