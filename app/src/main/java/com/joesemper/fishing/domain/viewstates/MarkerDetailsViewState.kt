package com.joesemper.fishing.domain.viewstates

import com.joesemper.fishing.model.entity.content.UserCatch

sealed class MarkerDetailsViewState {
    object Loading: MarkerDetailsViewState()
    class Success(val content: List<UserCatch>): MarkerDetailsViewState()
    class Error(val error: Throwable): MarkerDetailsViewState()
}