package com.mobileprism.fishing.model.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

class SafeApiCallTest {

    @Test
    fun returnsFailureAfterRetriesAreExhausted() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var attempts = 0

        val result = safeApiCall(dispatcher = dispatcher, retries = 1) {
            attempts++
            throw IllegalStateException("Network down")
        }

        assertTrue(result.isFailure)
        assertEquals("Network down", result.exceptionOrNull()?.message)
        assertEquals(2, attempts)
    }

    @Test
    fun rethrowsCancellationException() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)

        assertFailsWith<CancellationException> {
            safeApiCall(dispatcher = dispatcher) {
                throw CancellationException("Cancelled")
            }
        }
    }
}
