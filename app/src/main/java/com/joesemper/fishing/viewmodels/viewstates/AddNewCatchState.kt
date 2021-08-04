package com.joesemper.fishing.viewmodels.viewstates

sealed class AddNewCatchState {
    class Loading(var progress: Int? = 0): AddNewCatchState()
    object Success: AddNewCatchState()
    class Error(val error: Throwable): AddNewCatchState()
}