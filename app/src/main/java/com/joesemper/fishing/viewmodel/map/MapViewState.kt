package com.joesemper.fishing.viewmodel.map

import com.joesemper.fishing.model.entity.map.UserMarker
import com.joesemper.fishing.viewmodel.base.ViewState

sealed class MapViewState: ViewState {
    object Loading: MapViewState()
    class Success(val userMarkers: List<UserMarker?>): MapViewState()
    class Error(val error: Throwable): MapViewState()
}