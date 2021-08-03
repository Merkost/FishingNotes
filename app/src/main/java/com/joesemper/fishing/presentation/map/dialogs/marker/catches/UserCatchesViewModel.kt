package com.joesemper.fishing.presentation.map.dialogs.marker.catches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.repository.map.catches.CatchesRepository
import com.joesemper.fishing.presentation.map.dialogs.marker.MarkerDetailsViewState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserCatchesViewModel(private val repository: CatchesRepository): ViewModel() {

    private val viewStateFlow: MutableStateFlow<MarkerDetailsViewState> =
        MutableStateFlow(MarkerDetailsViewState.Loading)

    fun subscribe(): StateFlow<MarkerDetailsViewState> {
        return viewStateFlow
    }

    fun loadCatchesByMarkerId(markerId: String) {
        viewModelScope.launch {
            repository.getCatchesByMarkerId(markerId).collect { catches ->
                viewStateFlow.value = MarkerDetailsViewState.Success(catches)
            }
        }

    }
}