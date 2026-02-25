package com.mobileprism.fishing.model.utils

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is ResponseException -> println("SafeApiCall: HTTP ${throwable.response.status}")
                else -> println("SafeApiCall: ${throwable.message}")
            }
            Result.failure(throwable)
        }
    }
}
