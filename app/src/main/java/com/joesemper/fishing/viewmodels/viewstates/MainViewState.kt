package com.joesemper.fishing.viewmodels.viewstates

sealed class MainViewState {
    object Loading: MainViewState()
    object Success: MainViewState()
    class Error(val error: Throwable): MainViewState()
}
