package com.joesemper.fishing.viewmodel.map

import com.joesemper.fishing.model.entity.map.Marker
import com.joesemper.fishing.viewmodel.base.ViewState

sealed class MapViewState: ViewState {
    object Loading: MapViewState()
    class Success(val markers: List<Marker?>): MapViewState()
    class Error(val error: Throwable): MapViewState()
}