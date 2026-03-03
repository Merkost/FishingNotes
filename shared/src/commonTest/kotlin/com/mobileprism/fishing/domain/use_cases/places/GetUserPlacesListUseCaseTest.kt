package com.mobileprism.fishing.domain.use_cases.places

import app.cash.turbine.test
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.testutils.userMapMarker
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class GetUserPlacesListUseCaseTest {

    private val markersRepository = mockk<MarkersRepository>()

    @Test
    fun emitsMarkersFromRepository() = runTest {
        val markers = listOf(
            userMapMarker(id = "m1", title = "Lake"),
            userMapMarker(id = "m2", title = "River"),
        )
        every { markersRepository.getAllUserMarkersList() } returns flowOf(markers)

        val useCase = GetUserPlacesListUseCase(markersRepository)

        useCase().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Lake", result[0].title)
            assertEquals("River", result[1].title)
            awaitComplete()
        }
    }

    @Test
    fun emitsEmptyListWhenNoMarkers() = runTest {
        every { markersRepository.getAllUserMarkersList() } returns flowOf(emptyList())

        val useCase = GetUserPlacesListUseCase(markersRepository)

        useCase().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }
}
