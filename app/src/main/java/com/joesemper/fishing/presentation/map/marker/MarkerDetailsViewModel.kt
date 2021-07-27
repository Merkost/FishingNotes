package com.joesemper.fishing.presentation.map.marker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.repository.map.MapRepository
import com.joesemper.fishing.data.repository.map.marker.MarkerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MarkerDetailsViewModel(private val repository: MarkerRepository) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<MarkerDetailsViewState> =
        MutableStateFlow(MarkerDetailsViewState.Loading)

    fun subscribe(markerId: String): StateFlow<MarkerDetailsViewState> {
        loadCatchesByMarkerId(markerId)
        return viewStateFlow
    }

    private fun loadCatchesByMarkerId(markerId: String) {
        viewModelScope.launch {
            val catches = repository.getCatchesByMarkerId(markerId)
            viewStateFlow.value = MarkerDetailsViewState.Success(catches)
        }

    }

}