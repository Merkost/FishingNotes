package com.mobileprism.fishing.ui.home.map

import com.mobileprism.fishing.domain.entity.content.UserMapMarker

sealed class MapUiState {
    object NormalMode : MapUiState()
    object PlaceSelectMode : MapUiState()
    class BottomSheetInfoMode(val marker: UserMapMarker) : MapUiState()
    //object BottomSheetFullyExpanded : MapUiState()
}