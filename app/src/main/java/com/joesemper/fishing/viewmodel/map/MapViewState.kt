package com.joesemper.fishing.viewmodel.map

import com.joesemper.fishing.model.entity.common.UserCatch
import com.joesemper.fishing.viewmodel.base.ViewState

sealed class MapViewState: ViewState {
    object Loading: MapViewState()
    class Success(val userCatches: List<UserCatch?>): MapViewState()
    class Error(val error: Throwable): MapViewState()
}