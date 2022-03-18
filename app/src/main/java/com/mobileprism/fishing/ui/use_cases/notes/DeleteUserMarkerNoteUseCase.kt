package com.mobileprism.fishing.ui.use_cases.notes

import com.mobileprism.fishing.model.entity.common.Note
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.*

class DeleteUserMarkerNoteUseCase(private val markersRepository: MarkersRepository) {

    suspend operator fun invoke(
        markerId: String,
        currentNotes: List<Note>,
        noteToDelete: Note
    ) = flow<Result<List<Note>>> {
        val newNotes = currentNotes.toMutableList().apply {
            remove(noteToDelete)
        }
        markersRepository.updateNotes(markerId, newNotes).collect {
            it.fold(
                onSuccess = {
                    emit(Result.success(newNotes))
                },
                onFailure = {
                    emit(Result.failure(it))
                }
            )
        }
    }
}

