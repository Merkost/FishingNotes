package com.joesemper.fishing.presentation.map

import com.joesemper.fishing.model.common.MapMarker
import kotlinx.coroutines.flow.Flow

sealed class MapViewState {
    object Loading: MapViewState()
    class Success(val userMarkers: Flow<MapMarker>): MapViewState()
    class Error(val error: Throwable): MapViewState()
}