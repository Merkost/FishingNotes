package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import com.mobileprism.fishing.domain.use_cases.notes.DeleteUserMarkerNoteUseCase
import com.mobileprism.fishing.domain.use_cases.notes.SaveUserMarkerNoteUseCase
import com.mobileprism.fishing.ui.home.SnackbarManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserPlaceViewModel(
    private val markersRepo: MarkersRepository,
    private val catchesRepo: CatchesRepositoryRead,
    private val saveNewUserMarkerNoteUseCase: SaveUserMarkerNoteUseCase,
    private val deleteUserMarkerNoteUseCase: DeleteUserMarkerNoteUseCase

) : ViewModel() {

    private val _markerVisibility = MutableStateFlow<Boolean?>(true)
    val markerVisibility: StateFlow<Boolean?> = _markerVisibility.asStateFlow()

    private val _marker: MutableStateFlow<UserMapMarker?> = MutableStateFlow(null)
    val marker: StateFlow<UserMapMarker?>
        get() = _marker

    private val _markerNotes = MutableStateFlow<List<Note>>(listOf())
    val markerNotes: StateFlow<List<Note>>
        get() = _markerNotes

    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote.asStateFlow()


    fun getCatchesByMarkerId(markerId: String): Flow<List<UserCatch>> {
        return viewModelScope.run {
            catchesRepo.getCatchesByMarkerId(markerId)
        }
    }

    fun deletePlace() {
        viewModelScope.launch {
            _marker.value?.let {
                markersRepo.deleteMarker(it)
            }
        }
    }

    fun changeVisibility(newIsVisible: Boolean) {
        viewModelScope.launch {
            _marker.value?.let {
                _markerVisibility.value = newIsVisible
                markersRepo.changeMarkerVisibility(it, changeTo = newIsVisible).fold(
                    onSuccess = {
                        SnackbarManager.showMessage(Res.string.marker_visibility_change_success)
                    },
                    onFailure = {
                        _markerVisibility.value = !newIsVisible
                        SnackbarManager.showMessage(Res.string.marker_visibility_change_error)
                    }
                )
            }

        }
    }

    fun updateMarkerNotes(note: Note) {
        _marker.value?.let { marker ->
            viewModelScope.launch {
                saveNewUserMarkerNoteUseCase.invoke(
                    markerId = marker.id,
                    _markerNotes.value,
                    note = note
                ).collect {
                    it.fold(
                        onSuccess = {
                            val newNotesList = it
                            _markerNotes.value = newNotesList
                        },
                        onFailure = {
                            SnackbarManager.showMessage(Res.string.place_note_not_saved)
                        }
                    )
                }
            }
        }
    }

    fun deleteMarkerNote(note: Note) {
        _marker.value?.let { marker ->
            viewModelScope.launch {
                deleteUserMarkerNoteUseCase.invoke(
                    markerId = marker.id,
                    currentNotes = _markerNotes.value,
                    noteToDelete = note
                ).collect {
                    it.fold(
                        onSuccess = {
                            val newNotesList = it
                            _markerNotes.value = newNotesList
                        },
                        onFailure = {
                            SnackbarManager.showMessage(Res.string.place_note_not_deleted)
                        }
                    )
                }
            }
        }
    }

    fun setCurrentNote(note: Note?) {
        _currentNote.value = note
    }

    fun setMarkerVisibility(visible: Boolean?) {
        _markerVisibility.value = visible
    }

    fun setMarker(m: UserMapMarker) {
        _marker.value = m
        _markerNotes.value = m.notes
        _markerVisibility.value = m.visible
    }

}
