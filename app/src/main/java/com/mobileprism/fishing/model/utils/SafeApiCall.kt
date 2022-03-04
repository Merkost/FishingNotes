package com.mobileprism.fishing.model.utils

import com.mobileprism.fishing.domain.viewstates.ErrorResponse
import com.mobileprism.fishing.domain.viewstates.ErrorType
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): RetrofitWrapper<T> {
    return withContext(dispatcher) {
        try {
            RetrofitWrapper.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            val error = when (throwable) {
                is IOException -> RetrofitWrapper.Error(ErrorType.NetworkError)
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    RetrofitWrapper.Error(ErrorType.GenericError(code, errorResponse))
                }
                else -> {
                    RetrofitWrapper.Error(ErrorType.GenericError(null, null))
                }
            }
            error
        }
    }
}

private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
    return try {
        throwable.response()?.errorBody()?.source()?.let {
            val moshiAdapter = Moshi.Builder().build().adapter(ErrorResponse::class.java)
            moshiAdapter.fromJson(it)
        }
    } catch (exception: Exception) {
        null
    }
}