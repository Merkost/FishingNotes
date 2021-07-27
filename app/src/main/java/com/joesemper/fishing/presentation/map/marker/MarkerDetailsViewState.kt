package com.joesemper.fishing.presentation.map.marker

import com.joesemper.fishing.model.common.content.UserCatch
import kotlinx.coroutines.flow.Flow

sealed class MarkerDetailsViewState {
    object Loading: MarkerDetailsViewState()
    class Success(val content: Flow<UserCatch>): MarkerDetailsViewState()
    class Error(val error: Throwable): MarkerDetailsViewState()
}