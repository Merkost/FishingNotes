package com.mobileprism.fishing.domain.use_cases.notes

import app.cash.turbine.test
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.testutils.FakeMarkersRepository
import com.mobileprism.fishing.testutils.note
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SaveUserMarkerNoteUseCaseTest {

    private val markersRepository = FakeMarkersRepository()

    @Test
    fun emptyIdCallsSaveNewNoteAndReturnsUpdatedList() = runTest {
        markersRepository.saveNewNoteResult = Result.success(Unit)

        val useCase = SaveUserMarkerNoteUseCase(markersRepository)
        val existingNotes = listOf(note(id = "n1"))
        val newNote = Note(id = "", title = "New", description = "Desc")

        useCase("marker-1", existingNotes, newNote).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val notes = result.getOrThrow()
            assertEquals(2, notes.size)
            // The existing note is kept
            assertEquals("n1", notes[0].id)
            // The new note gets a generated non-empty id
            assertTrue(notes[1].id.isNotEmpty())
            assertEquals("New", notes[1].title)
            assertEquals("Desc", notes[1].description)
            awaitComplete()
        }
    }

    @Test
    fun existingIdFoundReplacesInList() = runTest {
        markersRepository.updateNotesResult = Result.success(Unit)

        val useCase = SaveUserMarkerNoteUseCase(markersRepository)
        val existingNotes = listOf(
            note(id = "n1", title = "Original"),
            note(id = "n2", title = "Other"),
        )
        val editedNote = note(id = "n1", title = "Edited")

        useCase("marker-1", existingNotes, editedNote).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val notes = result.getOrThrow()
            assertEquals(2, notes.size)
            assertEquals("Edited", notes[0].title)
            assertEquals("Other", notes[1].title)
            awaitComplete()
        }
    }

    @Test
    fun existingIdNotFoundAppendsToList() = runTest {
        markersRepository.updateNotesResult = Result.success(Unit)

        val useCase = SaveUserMarkerNoteUseCase(markersRepository)
        val existingNotes = listOf(note(id = "n1"))
        val newNote = note(id = "n-unknown", title = "Appended")

        useCase("marker-1", existingNotes, newNote).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val notes = result.getOrThrow()
            assertEquals(2, notes.size)
            assertEquals("n1", notes[0].id)
            assertEquals("n-unknown", notes[1].id)
            assertEquals("Appended", notes[1].title)
            awaitComplete()
        }
    }

    @Test
    fun propagatesFailureFromRepository() = runTest {
        val error = RuntimeException("Network error")
        markersRepository.saveNewNoteResult = Result.failure(error)

        val useCase = SaveUserMarkerNoteUseCase(markersRepository)
        val newNote = Note(id = "", title = "New", description = "Desc")

        useCase("marker-1", emptyList(), newNote).test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
            awaitComplete()
        }
    }

    @Test
    fun blankDescriptionReturnsFailure() = runTest {
        val useCase = SaveUserMarkerNoteUseCase(markersRepository)
        val blankNote = Note(id = "", title = "Title", description = "   ")

        useCase("marker-1", emptyList(), blankNote).test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            awaitComplete()
        }
    }

    @Test
    fun returnsUpdatedListOnEditSuccess() = runTest {
        markersRepository.updateNotesResult = Result.success(Unit)

        val useCase = SaveUserMarkerNoteUseCase(markersRepository)
        val existingNotes = listOf(
            note(id = "n1", title = "First"),
            note(id = "n2", title = "Second"),
            note(id = "n3", title = "Third"),
        )
        val editedNote = note(id = "n2", title = "Updated Second", description = "New desc")

        useCase("marker-1", existingNotes, editedNote).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val notes = result.getOrThrow()
            assertEquals(3, notes.size)
            assertEquals("First", notes[0].title)
            assertEquals("Updated Second", notes[1].title)
            assertEquals("New desc", notes[1].description)
            assertEquals("Third", notes[2].title)
            awaitComplete()
        }
    }
}
