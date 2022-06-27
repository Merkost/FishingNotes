package com.mobileprism.fishing.ui.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.LiteProgress
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.use_cases.notes.DeleteUserMarkerNoteUseCase
import com.mobileprism.fishing.domain.use_cases.notes.SaveUserMarkerNoteUseCase
import com.mobileprism.fishing.ui.home.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class UserPlaceViewModel(
    private val markersRepo: MarkersRepository,
    private val catchesRepo: CatchesRepository,
    private val saveNewUserMarkerNoteUseCase: SaveUserMarkerNoteUseCase,
    private val deleteUserMarkerNoteUseCase: DeleteUserMarkerNoteUseCase
) : ViewModel() {

    var markerVisibility: MutableState<Boolean?> = mutableStateOf(true)

    private val _marker: MutableStateFlow<UserMapMarker?> = MutableStateFlow(null)
    val marker: StateFlow<UserMapMarker?>
        get() = _marker

    private val _markerNotes = MutableStateFlow<List<Note>>(listOf())
    val markerNotes: StateFlow<List<Note>>
        get() = _markerNotes

    private val _catchesList = MutableStateFlow<List<UserCatch>>(listOf())
    val catchesList = _catchesList.asStateFlow()

    val currentNote: MutableState<Note?> = mutableStateOf(null)


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
            _marker.value?.let {
                markersRepo.deleteMarker(it)
            }
        }
    }

    fun changeVisibility(newIsVisible: Boolean) {
        viewModelScope.launch {
            _marker.value?.let {
                markerVisibility.value = newIsVisible
                markersRepo.changeMarkerVisibility(it, changeTo = newIsVisible).single().fold(
                    onSuccess = {
                        SnackbarManager.showMessage(R.string.marker_visibility_change_success)
                    },
                    onFailure = {
                        markerVisibility.value = !newIsVisible
                        SnackbarManager.showMessage(R.string.marker_visibility_change_error)
                    })
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
        markerVisibility.value = m.visible
    }

}