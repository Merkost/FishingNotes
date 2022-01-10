package com.joesemper.fishing.domain.viewstates

sealed class RetrofitWrapper<out T> {
    //TODO: Убрать loading и перенести Error сюда
    class Success<T>(val data: T) : RetrofitWrapper<T>()
    class Error(val errorType: ErrorType) : RetrofitWrapper<Nothing>()
    class Loading(val progress: Int? = null) : RetrofitWrapper<Nothing>()
}

sealed class ErrorType(val error: Throwable) {
    class NetworkError(error: Throwable) : ErrorType(error)
    class OtherError(error: Throwable) : ErrorType(error)
}

/*
open class NetworkException(error: Throwable): RuntimeException(error)
class NoNetworkException(error: Throwable): NetworkException(error)
class ServerUnreachableException(error: Throwable): NetworkException(error)
class HttpCallFailureException(error: Throwable): NetworkException(error)*/
