package com.mobileprism.fishing.model.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.success(apiCall.invoke())
        } catch (throwable: Throwable) {
            Result.failure(throwable)
            /*val error = when (throwable) {
                is IOException -> RetrofitWrapper.Error(ErrorType.NetworkError(Throwable()))
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    *//*RetrofitWrapper.Error(ErrorType.GenericError(code, errorResponse))*//*
                    RetrofitWrapper.Error(ErrorType.NetworkError(Throwable()))
                }
                else -> {
                    *//*RetrofitWrapper.Error(ErrorType.GenericError(null, null))*//*
                    RetrofitWrapper.Error(ErrorType.NetworkError(Throwable()))
                }
            }
            error*/
        }
    }
}

/*private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
    return try {
        throwable.response()?.errorBody()?.source()?.let {
            val moshiAdapter = Moshi.Builder().build().adapter(ErrorResponse::class.java)
            moshiAdapter.fromJson(it)
        }
    } catch (exception: Exception) {
        null
    }
}*/
