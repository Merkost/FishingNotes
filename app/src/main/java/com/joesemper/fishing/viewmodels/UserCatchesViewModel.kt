package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.repository.CatchesRepository
import com.joesemper.fishing.viewmodels.viewstates.MarkerDetailsViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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