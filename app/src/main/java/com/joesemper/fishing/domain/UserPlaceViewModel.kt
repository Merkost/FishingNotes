package com.joesemper.fishing.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.LiteProgress
import com.joesemper.fishing.model.entity.common.Note
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.app.CatchesRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserPlaceViewModel(
    private val markersRepo: MarkersRepository,
    private val catchesRepo: CatchesRepository,
) : ViewModel() {

    var markerVisibility: MutableState<Boolean?> = mutableStateOf(null)
    private val _marker: MutableStateFlow<UserMapMarker?> = MutableStateFlow(null)
    val marker: StateFlow<UserMapMarker?>
        get() = _marker

    private val _markerNotes = MutableStateFlow<List<Note>>(listOf())
    val markerNotes: StateFlow<List<Note>>
        get() = _markerNotes

    val currentNote: MutableState<Note?> = mutableStateOf(null)


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
                markerVisibility.value = newIsVisible
                markersRepo.changeMarkerVisibility(it, changeTo = newIsVisible).collect {
                    when (it) {
                        is LiteProgress.Loading -> {}
                        is LiteProgress.Complete -> {
                            SnackbarManager.showMessage(R.string.marker_visibility_change_success)
                        }
                        is LiteProgress.Error -> {
                            markerVisibility.value = !newIsVisible
                            SnackbarManager.showMessage(R.string.marker_visibility_change_error)
                        }
                    }
                }
            }

        }
    }

    fun updateMarkerNotes(note: Note) {
        _marker.value?.let { marker ->
            viewModelScope.launch {
                markersRepo.updateUserMarkerNote(
                    markerId = marker.id,
                    _markerNotes.value,
                    note = note
                ).collect { baseViewState ->
                    when (baseViewState) {
                        is BaseViewState.Success<*> -> {
                            val newNotesList = baseViewState.data as List<Note>
                            _markerNotes.value = newNotesList
                        }
                        is BaseViewState.Error -> {
                            SnackbarManager.showMessage(R.string.place_note_not_saved)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun deleteMarkerNote(note: Note) {
        _marker.value?.let { marker ->
            viewModelScope.launch {
                markersRepo.deleteMarkerNote(
                    markerId = marker.id,
                    currentNotes = _markerNotes.value,
                    noteToDelete = note
                ).collect { baseViewState ->
                    when (baseViewState) {
                        is BaseViewState.Success<*> -> {
                            val newNotesList = baseViewState.data as List<Note>
                            _markerNotes.value = newNotesList
                        }
                        is BaseViewState.Error -> {
                            SnackbarManager.showMessage(R.string.place_note_not_deleted)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun setMarker(m: UserMapMarker) {
        _marker.value = m
        _markerNotes.value = m.notes
    }

}