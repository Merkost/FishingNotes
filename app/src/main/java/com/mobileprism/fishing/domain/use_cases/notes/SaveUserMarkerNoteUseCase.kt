package com.mobileprism.fishing.domain.use_cases.notes

import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.model.mappers.MarkerNoteMapper
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

class SaveUserMarkerNoteUseCase(private val markersRepository: MarkersRepository) {

    suspend operator fun invoke(
        markerId: String,
        currentNotes: List<Note>,
        note: Note
    ) = flow {
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

