package com.mobileprism.fishing.domain.use_cases.places

import app.cash.turbine.test
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.testutils.FakeAuthRepository
import com.mobileprism.fishing.testutils.rawMapMarker
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class AddNewPlaceUseCaseTest {

    private val markersRepository = mockk<MarkersRepository>()
    private val authRepository = FakeAuthRepository(userId = "user-1")

    @Test
    fun convertsRawMapMarkerToUserMapMarkerWithUserIdFromAuth() = runTest {
        val markerSlot = slot<UserMapMarker>()
        coEvery { markersRepository.addNewMarker(capture(markerSlot)) } returns Result.success(Unit)

        val useCase = AddNewPlaceUseCase(markersRepository, authRepository)
        val raw = rawMapMarker(
            title = "My Spot",
            latitude = 55.5,
            longitude = 37.5,
            description = "Nice lake",
            visible = true,
            public = false,
        )

        useCase(raw).test {
            awaitItem()
            awaitComplete()
        }

        val captured = markerSlot.captured
        assertEquals("user-1", captured.userId)
        assertEquals("My Spot", captured.title)
        assertEquals(55.5, captured.latitude)
        assertEquals(37.5, captured.longitude)
        assertEquals("Nice lake", captured.description)
        assertTrue(captured.visible)
        assertEquals(false, captured.public)
    }

    @Test
    fun setsNonEmptyGeneratedId() = runTest {
        val markerSlot = slot<UserMapMarker>()
        coEvery { markersRepository.addNewMarker(capture(markerSlot)) } returns Result.success(Unit)

        val useCase = AddNewPlaceUseCase(markersRepository, authRepository)

        useCase(rawMapMarker()).test {
            awaitItem()
            awaitComplete()
        }

        assertTrue(markerSlot.captured.id.isNotEmpty())
    }

    @Test
    fun setsDateOfCreationToApproximatelyCurrentTime() = runTest {
        val markerSlot = slot<UserMapMarker>()
        coEvery { markersRepository.addNewMarker(capture(markerSlot)) } returns Result.success(Unit)

        val useCase = AddNewPlaceUseCase(markersRepository, authRepository)
        val beforeTime = kotlin.time.Clock.System.now().toEpochMilliseconds()

        useCase(rawMapMarker()).test {
            awaitItem()
            awaitComplete()
        }

        val afterTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val creationTime = markerSlot.captured.dateOfCreation
        assertTrue(creationTime in beforeTime..afterTime,
            "dateOfCreation $creationTime should be between $beforeTime and $afterTime")
    }
}
