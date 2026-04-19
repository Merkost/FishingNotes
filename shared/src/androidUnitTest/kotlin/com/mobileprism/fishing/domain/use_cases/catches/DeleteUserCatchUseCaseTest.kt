package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import com.mobileprism.fishing.testutils.FakePhotoStorage
import com.mobileprism.fishing.testutils.userCatch
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DeleteUserCatchUseCaseTest {

    private val catchesRepository = mockk<CatchesRepositoryUpdate>()
    private val photoStorage = FakePhotoStorage()

    init {
        coEvery { catchesRepository.deleteCatch(any()) } coAnswers { Result.success(Unit) }
    }

    @Test
    fun deletesCatchAndAllPhotos() = runTest {
        val photos = listOf("http://photo1.jpg", "http://photo2.jpg", "http://photo3.jpg")
        val catch = userCatch(downloadPhotoLinks = photos)
        val useCase = DeleteUserCatchUseCase(catchesRepository, photoStorage)

        val result = useCase(catch)

        assertTrue(result.isSuccess)
        coVerify { catchesRepository.deleteCatch(catch) }
        assertEquals(3, photoStorage.deletedPhotos.size)
        assertEquals(photos, photoStorage.deletedPhotos)
    }

    @Test
    fun catchWithNoPhotosJustDeletesCatch() = runTest {
        val catch = userCatch(downloadPhotoLinks = emptyList())
        val useCase = DeleteUserCatchUseCase(catchesRepository, photoStorage)

        val result = useCase(catch)

        assertTrue(result.isSuccess)
        coVerify { catchesRepository.deleteCatch(catch) }
        assertTrue(photoStorage.deletedPhotos.isEmpty())
    }

    @Test
    fun verifiesEachPhotoUrlPassedToDeletePhoto() = runTest {
        val photos = listOf("http://example.com/a.jpg", "http://example.com/b.jpg")
        val catch = userCatch(downloadPhotoLinks = photos)
        val useCase = DeleteUserCatchUseCase(catchesRepository, photoStorage)

        useCase(catch)

        assertEquals("http://example.com/a.jpg", photoStorage.deletedPhotos[0])
        assertEquals("http://example.com/b.jpg", photoStorage.deletedPhotos[1])
    }
}
