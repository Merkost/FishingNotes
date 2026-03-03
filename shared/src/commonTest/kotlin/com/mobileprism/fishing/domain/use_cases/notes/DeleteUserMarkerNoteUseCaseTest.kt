package com.mobileprism.fishing.domain.use_cases.notes

import app.cash.turbine.test
import com.mobileprism.fishing.testutils.FakeMarkersRepository
import com.mobileprism.fishing.testutils.note
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DeleteUserMarkerNoteUseCaseTest {

    private val markersRepository = FakeMarkersRepository()

    @Test
    fun removesNoteFromListAndReturnsListWithoutDeletedNote() = runTest {
        markersRepository.updateNotesResult = Result.success(Unit)

        val useCase = DeleteUserMarkerNoteUseCase(markersRepository)
        val noteToDelete = note(id = "n2", title = "Delete me")
        val currentNotes = listOf(
            note(id = "n1", title = "Keep"),
            noteToDelete,
            note(id = "n3", title = "Also keep"),
        )

        useCase("marker-1", currentNotes, noteToDelete).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val notes = result.getOrThrow()
            assertEquals(2, notes.size)
            assertEquals("n1", notes[0].id)
            assertEquals("n3", notes[1].id)
            awaitComplete()
        }
    }

    @Test
    fun propagatesFailure() = runTest {
        val error = RuntimeException("Sync error")
        markersRepository.updateNotesResult = Result.failure(error)

        val useCase = DeleteUserMarkerNoteUseCase(markersRepository)
        val noteToDelete = note(id = "n1")

        useCase("marker-1", listOf(noteToDelete), noteToDelete).test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            assertEquals("Sync error", result.exceptionOrNull()?.message)
            awaitComplete()
        }
    }

    @Test
    fun deletingNonExistentNoteReturnsOriginalList() = runTest {
        markersRepository.updateNotesResult = Result.success(Unit)

        val useCase = DeleteUserMarkerNoteUseCase(markersRepository)
        val currentNotes = listOf(
            note(id = "n1", title = "Keep"),
            note(id = "n2", title = "Also keep"),
        )
        val nonExistentNote = note(id = "n-unknown", title = "Not in list")

        useCase("marker-1", currentNotes, nonExistentNote).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val notes = result.getOrThrow()
            assertEquals(2, notes.size)
            assertEquals("n1", notes[0].id)
            assertEquals("n2", notes[1].id)
            awaitComplete()
        }
    }
}
