package com.joesemper.fishing.compose.ui.home.map

sealed class MapUiState {
    object NormalMode : MapUiState()
    object PlaceSelectMode : MapUiState()
    object BottomSheetInfoMode : MapUiState()
}