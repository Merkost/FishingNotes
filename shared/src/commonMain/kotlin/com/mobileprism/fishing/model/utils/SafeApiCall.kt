package com.mobileprism.fishing.model.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    retries: Int = 2,
    apiCall: suspend () -> T
): Result<T> {
    return withContext(dispatcher) {
        var lastException: Throwable? = null
        repeat(retries + 1) { attempt ->
            try {
                return@withContext Result.success(apiCall.invoke())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                lastException = e
                if (attempt < retries) delay(1000L * (attempt + 1))
            }
        }
        Result.failure(lastException!!)
    }
}
