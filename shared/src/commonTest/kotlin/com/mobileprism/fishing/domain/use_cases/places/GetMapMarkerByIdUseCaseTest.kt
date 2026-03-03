package com.mobileprism.fishing.domain.use_cases.places

import com.mobileprism.fishing.testutils.FakeMarkersRepository
import com.mobileprism.fishing.testutils.userMapMarker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetMapMarkerByIdUseCaseTest {

    @Test
    fun delegatesToRepositoryAndReturnsSuccess() = runTest {
        val marker = userMapMarker(id = "m1", title = "Lake")
        val repo = FakeMarkersRepository(mapOf("m1" to Result.success(marker)))

        val useCase = GetMapMarkerByIdUseCase(repo)
        val result = useCase("m1")

        assertTrue(result.isSuccess)
        assertEquals(marker, result.getOrThrow())
    }

    @Test
    fun returnsFailureWhenRepositoryFails() = runTest {
        val error = RuntimeException("Not found")
        val repo = FakeMarkersRepository(mapOf("m-bad" to Result.failure(error)))

        val useCase = GetMapMarkerByIdUseCase(repo)
        val result = useCase("m-bad")

        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }
}
