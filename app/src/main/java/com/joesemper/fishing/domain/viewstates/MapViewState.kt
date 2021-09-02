package com.joesemper.fishing.domain.viewstates

import com.joesemper.fishing.model.entity.content.Content
import kotlinx.coroutines.flow.Flow

sealed class MapViewState {
    object Loading: MapViewState()
    class Success(val content: Flow<Content>? = null): MapViewState()
    class Error(val error: Throwable): MapViewState()
}