package com.joesemper.fishing.viewmodels.viewstates

import com.joesemper.fishing.data.entity.content.UserCatch

sealed class MarkerDetailsViewState {
    object Loading: MarkerDetailsViewState()
    class Success(val content: List<UserCatch>): MarkerDetailsViewState()
    class Error(val error: Throwable): MarkerDetailsViewState()
}