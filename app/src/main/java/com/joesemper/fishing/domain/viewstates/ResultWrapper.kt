package com.joesemper.fishing.domain.viewstates

sealed class ResultWrapper<out T> {
    class Success<T>(val data: T) : ResultWrapper<T>()
    class Error(val error: Throwable) : ResultWrapper<Nothing>()
    class Loading(val progress: Int? = null) : ResultWrapper<Nothing>()
}