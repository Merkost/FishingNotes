package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.testutils.FakePhotoStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SavePhotosUseCaseTest {

    @Test
    fun localPhotosAreUploaded() = runTest {
        val photoStorage = FakePhotoStorage()
        val useCase = SavePhotosUseCase(photoStorage)

        val result = useCase(listOf("/local/photo1.jpg", "/local/photo2.jpg"))

        assertEquals(2, photoStorage.uploadedPhotos.size)
        assertEquals("/local/photo1.jpg", photoStorage.uploadedPhotos[0])
        assertEquals("/local/photo2.jpg", photoStorage.uploadedPhotos[1])
        // FakePhotoStorage returns "http://uploaded/<path>" for each uploaded photo
        assertTrue(result.all { it.startsWith("http") })
    }

    @Test
    fun httpPhotosAreKeptAsIs() = runTest {
        val photoStorage = FakePhotoStorage()
        val useCase = SavePhotosUseCase(photoStorage)

        val httpPhotos = listOf("http://example.com/photo1.jpg", "https://example.com/photo2.jpg")
        val result = useCase(httpPhotos)

        // No photos should have been uploaded
        assertTrue(photoStorage.uploadedPhotos.isEmpty())
        // All original http URLs should be in the result
        assertTrue(result.contains("http://example.com/photo1.jpg"))
        assertTrue(result.contains("https://example.com/photo2.jpg"))
    }

    @Test
    fun mixedListLocalUploadedHttpKept() = runTest {
        val photoStorage = FakePhotoStorage()
        val useCase = SavePhotosUseCase(photoStorage)

        val photos = listOf("/local/new.jpg", "http://existing.jpg")
        val result = useCase(photos)

        // Only local photo should be uploaded
        assertEquals(1, photoStorage.uploadedPhotos.size)
        assertEquals("/local/new.jpg", photoStorage.uploadedPhotos[0])
        // Result should contain both uploaded local and original http
        assertEquals(2, result.size)
        assertTrue(result.contains("http://existing.jpg"))
        assertTrue(result.any { it.startsWith("http://uploaded/") })
    }

    @Test
    fun emptyListReturnsEmpty() = runTest {
        val photoStorage = FakePhotoStorage()
        val useCase = SavePhotosUseCase(photoStorage)

        val result = useCase(emptyList())

        assertTrue(result.isEmpty())
        assertTrue(photoStorage.uploadedPhotos.isEmpty())
    }
}
