package com.joesemper.fishing.domain

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.ui.composable.UiState
import com.joesemper.fishing.view.weather.utils.getDateByMilliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.sql.Time

class UserPlacesViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Loading(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    init {
        loadAllUserPlaces()
    }

    private fun loadAllUserPlaces() {
        val start = System.currentTimeMillis()
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { places ->
                //for loading animation
                if (System.currentTimeMillis() - start < 1500) delay(1500)
                _uiState.value = BaseViewState.Success(places)
            }
        }
    }

}