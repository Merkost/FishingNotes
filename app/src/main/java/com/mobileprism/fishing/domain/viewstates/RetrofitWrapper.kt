package com.mobileprism.fishing.domain.viewstates

sealed class RetrofitWrapper<out T> {
    //TODO: Убрать loading и перенести Error сюда
    class Success<T>(val data: T) : RetrofitWrapper<T>()
    class Error(val errorType: ErrorType) : RetrofitWrapper<Nothing>()
    class Loading(val progress: Int? = null) : RetrofitWrapper<Nothing>()
}

sealed class ErrorType(val error: Throwable?) {
    class NetworkError(error: Throwable) : ErrorType(error)
    class OtherError(error: Throwable?) : ErrorType(error)
}

data class ErrorResponse(
    val error_description: String, // this is the translated error shown to the user directly from the API
    val causes: Map<String, String> = emptyMap() //this is for errors on specific field on a form
)

/*
open class NetworkException(error: Throwable): RuntimeException(error)
class NoNetworkException(error: Throwable): NetworkException(error)
class ServerUnreachableException(error: Throwable): NetworkException(error)
class HttpCallFailureException(error: Throwable): NetworkException(error)*/
