package com.joesemper.fishing.viewmodels.viewstates

sealed class BaseViewState {
    class Success<T>(val data: T) : BaseViewState()
    class Error(val error: Throwable) : BaseViewState()
    class LoadingProgress(val progress: Int?) : BaseViewState()
    class Loading(val isProgress: Boolean?) : BaseViewState()
}