package com.joesemper.fishing.viewmodels.viewstates

sealed class NewCatchViewState {
    object Loading: NewCatchViewState()
    object Success: NewCatchViewState()
    class Error(val error: Throwable): NewCatchViewState()
}