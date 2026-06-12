package com.mobileprism.fishing.domain.use_cases.notes

import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.model.mappers.MarkerNoteMapper
import com.mobileprism.fishing.utils.ValidationUtils
import kotlinx.coroutines.flow.flow

class SaveUserMarkerNoteUseCase(private val markersRepository: MarkersRepository) {

    suspend operator fun invoke(
        markerId: String,
        currentNotes: List<Note>,
        note: Note
    ) = flow {
        if (!ValidationUtils.isNoteValid(note.description)) {
            emit(Result.failure(IllegalArgumentException("Note description cannot be blank")))
            return@flow
        }
        if (note.id.isEmpty()) {
            emit(saveNewNote(markerId, note, currentNotes))
        } else {
            emit(editNote(markerId, note, currentNotes))
        }
    }

    private suspend fun editNote(
        markerId: String,
        note: Note,
        currentNotes: List<Note>
    ): Result<List<Note>> {
        val newNotes = currentNotes.toMutableList().apply {
            val index = indexOfFirst { it.id == note.id }
            if (index != -1) {
                set(index, note)
            } else {
                add(note)
            }
        }
        return markersRepository.updateNotes(markerId, newNotes).fold(
            onSuccess = { Result.success(newNotes) },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun saveNewNote(
        markerId: String,
        note: Note,
        currentNotes: List<Note>
    ): Result<List<Note>> {
        val newNote = MarkerNoteMapper().mapRawMarkerNote(note)
        return markersRepository.saveNewNote(markerId, newNote).fold(
            onSuccess = { Result.success(currentNotes + newNote) },
            onFailure = { Result.failure(it) }
        )
    }
}
