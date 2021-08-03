package com.joesemper.fishing.presentation.map.dialogs.marker

import com.joesemper.fishing.model.common.content.UserCatch
import kotlinx.coroutines.flow.Flow

sealed class MarkerDetailsViewState {
    object Loading: MarkerDetailsViewState()
    class Success(val content: List<UserCatch>): MarkerDetailsViewState()
    class Error(val error: Throwable): MarkerDetailsViewState()
}