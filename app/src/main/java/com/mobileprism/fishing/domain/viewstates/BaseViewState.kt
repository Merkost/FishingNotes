package com.mobileprism.fishing.domain.viewstates

sealed class BaseViewState<out T> {
    data class Success<out T>(val data: T) : BaseViewState<T>()
    data class Error(val error: Throwable?) : BaseViewState<Nothing>()
    class Loading(val progress: Int? = null) : BaseViewState<Nothing>()
}

