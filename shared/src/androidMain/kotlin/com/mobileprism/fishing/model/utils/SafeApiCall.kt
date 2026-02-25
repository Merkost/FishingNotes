package com.mobileprism.fishing.model.utils

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> Log.e("SafeApiCall", "Network error", throwable)
                is HttpException -> Log.e("SafeApiCall", "HTTP ${throwable.code()}", throwable)
                else -> Log.e("SafeApiCall", "Unexpected error", throwable)
            }
            Result.failure(throwable)
        }
    }
}
