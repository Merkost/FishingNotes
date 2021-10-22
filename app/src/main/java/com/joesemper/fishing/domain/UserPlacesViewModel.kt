package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserPlacesViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Loading(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    init {
        loadAllUserPlaces()
    }

    private fun loadAllUserPlaces() {
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { places ->
                _uiState.value = BaseViewState.Success(places)
            }
        }
    }
}