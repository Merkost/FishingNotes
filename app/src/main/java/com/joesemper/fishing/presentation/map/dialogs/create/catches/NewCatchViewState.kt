package com.joesemper.fishing.presentation.map.dialogs.create.catches

sealed class NewCatchViewState {
    object Loading: NewCatchViewState()
    object Success: NewCatchViewState()
    class Error(val error: Throwable): NewCatchViewState()
}