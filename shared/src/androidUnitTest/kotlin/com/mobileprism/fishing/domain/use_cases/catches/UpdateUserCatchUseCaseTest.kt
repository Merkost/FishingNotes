package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase
import com.mobileprism.fishing.testutils.userCatch
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class UpdateUserCatchUseCaseTest {

    private val catchesRepository = mockk<CatchesRepositoryUpdate>()
    private val savePhotos = mockk<SavePhotosUseCase>()

    init {
        coEvery { catchesRepository.updateUserCatch(any(), any(), any()) } coAnswers { Result.success(Unit) }
    }

    @Test
    fun uploadsPhotosAndUpdatesCatchData() = runTest {
        val photos = listOf("/local/photo.jpg")
        val uploadedPhotos = listOf("http://uploaded/photo.jpg")
        coEvery { savePhotos(photos, any()) } returns uploadedPhotos

        val catch = userCatch(
            id = "c1",
            userMarkerId = "m1",
            fishType = "Bass",
            downloadPhotoLinks = photos,
        )
        val useCase = UpdateUserCatchUseCase(catchesRepository, savePhotos)

        useCase(catch)

        coVerify { savePhotos(photos, any()) }
        coVerify { catchesRepository.updateUserCatch("m1", "c1", any()) }
    }

    @Test
    fun passesCorrectDataMapToUpdateUserCatch() = runTest {
        val uploadedPhotos = listOf("http://uploaded/photo.jpg")
        coEvery { savePhotos(any(), any()) } returns uploadedPhotos

        val catch = userCatch(
            id = "c1",
            userMarkerId = "m1",
            fishType = "Trout",
            fishAmount = 3,
            fishWeight = 4.5,
            fishingRodType = "Spinning",
            fishingBait = "Worm",
            fishingLure = "Spoon",
            downloadPhotoLinks = listOf("http://existing.jpg"),
        )

        val dataSlot = slot<Map<String, Any>>()
        coEvery { catchesRepository.updateUserCatch(any(), any(), capture(dataSlot)) } coAnswers { Result.success(Unit) }

        val useCase = UpdateUserCatchUseCase(catchesRepository, savePhotos)
        useCase(catch)

        val data = dataSlot.captured
        assertEquals(uploadedPhotos, data["downloadPhotoLinks"])
        assertEquals("Trout", data["fishType"])
        assertEquals(3, data["fishAmount"])
        assertEquals(4.5, data["fishWeight"])
        assertEquals("Spinning", data["fishingRodType"])
        assertEquals("Worm", data["fishingBait"])
        assertEquals("Spoon", data["fishingLure"])
        assertEquals(catch.note, data["note"])
    }

    @Test
    fun httpUrlsKeptAsIsLocalUrlsUploaded() = runTest {
        val mixedPhotos = listOf("http://existing.jpg", "/local/new.jpg")
        val uploadedResult = listOf("http://uploaded/new.jpg", "http://existing.jpg")
        coEvery { savePhotos(mixedPhotos, any()) } returns uploadedResult

        val catch = userCatch(downloadPhotoLinks = mixedPhotos)
        val useCase = UpdateUserCatchUseCase(catchesRepository, savePhotos)

        useCase(catch)

        coVerify { savePhotos(mixedPhotos, any()) }
    }
}
