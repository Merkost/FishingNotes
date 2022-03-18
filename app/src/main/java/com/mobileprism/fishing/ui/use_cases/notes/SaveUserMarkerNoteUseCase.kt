package com.mobileprism.fishing.ui.use_cases.notes

import com.mobileprism.fishing.model.entity.common.Note
import com.mobileprism.fishing.model.mappers.MarkerNoteMapper
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.*

class SaveUserMarkerNoteUseCase(private val markersRepository: MarkersRepository) {

    suspend operator fun invoke(
        markerId: String,
        currentNotes: List<Note>,
        note: Note
    ) = flow<Result<List<Note>>> {
        if (note.id.isEmpty()) {
            saveNewNote(this, markerId, note, currentNotes)
        } else {
            editNote(this, markerId, note, currentNotes)
        }
    }

    private suspend fun editNote(
        flow: FlowCollector<Result<List<Note>>>,
        markerId: String,
        note: Note,
        currentNotes: List<Note>
    ) {
        val newNotes = currentNotes.toMutableList().apply {
            set(indexOf(find { it.id == note.id }), note)
        }
        markersRepository.updateNotes(markerId, newNotes).collect {
            it.fold(
                onSuccess = {
                    flow.emit(Result.success(newNotes))
                },
                onFailure = {
                    flow.emit(Result.failure(it))
                }
            )
        }
    }

    private suspend fun saveNewNote(
        flow: FlowCollector<Result<List<Note>>>,
        markerId: String,
        note: Note,
        currentNotes: List<Note>
    ) {
        val newNote = MarkerNoteMapper().mapRawMarkerNote(note)
        markersRepository.saveNewNote(markerId, newNote).collect {
            it.fold(
                onSuccess = {
                    flow.emit(Result.success(currentNotes + newNote))
                },
                onFailure = {
                    flow.emit(Result.failure(it))
                }
            )
        }
    }
}

