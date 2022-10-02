package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.use_cases.notes.DeleteUserMarkerNoteUseCase
import com.mobileprism.fishing.domain.use_cases.notes.SaveUserMarkerNoteUseCase
import com.mobileprism.fishing.ui.home.SnackbarManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserPlaceViewModel(
    private val markersRepo: MarkersRepository,
    private val catchesRepo: CatchesRepository,
    private val initialMarker: UserMapMarker,
    private val saveNewUserMarkerNoteUseCase: SaveUserMarkerNoteUseCase,
    private val deleteUserMarkerNoteUseCase: DeleteUserMarkerNoteUseCase
) : ViewModel() {

    private val _markerVisibility = MutableStateFlow(true)
    val markerVisibility = _markerVisibility.asStateFlow()

    private val _marker: MutableStateFlow<UserMapMarker> = MutableStateFlow(initialMarker)
    val marker: StateFlow<UserMapMarker>
        get() = _marker

    private val _markerNotes = MutableStateFlow<List<Note>>(listOf())
    val markerNotes: StateFlow<List<Note>>
        get() = _markerNotes

    private val _catchesList = MutableStateFlow<List<UserCatch>>(listOf())
    val catchesList = _catchesList.asStateFlow()

    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote = _currentNote.asStateFlow()


    private fun getCatchesByMarkerId(markerId: String) {
        viewModelScope.launch {
            catchesRepo.getCatchesByMarkerId(markerId).fold(
                onSuccess = {
                    _catchesList.value = it
                },
                onFailure = {

                }
            )
        }
    }

    fun deletePlace() {
        viewModelScope.launch {
            markersRepo.deleteMarker(marker.value)
        }
    }

    fun changeVisibility(newIsVisible: Boolean) {
        viewModelScope.launch {
            _markerVisibility.update { newIsVisible }
            markersRepo.changeMarkerVisibility(marker.value, changeTo = newIsVisible).single().fold(
                onSuccess = {
                    SnackbarManager.showMessage(R.string.marker_visibility_change_success)
                },
                onFailure = {
                    _markerVisibility.update { !newIsVisible }
                    SnackbarManager.showMessage(R.string.marker_visibility_change_error)
                })
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
                            SnackbarManager.showMessage(R.string.place_note_not_saved)
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
                            SnackbarManager.showMessage(R.string.place_note_not_deleted)
                        }
                    )
                }
            }
        }
    }

    fun setMarker(m: UserMapMarker) {
        getCatchesByMarkerId(m.id)
        _marker.value = m
        _markerNotes.value = m.notes
        //markerVisibility.value = m.visible
    }

    fun setCurrentNote(note: Note) {
        _currentNote.update{ note }
    }

}