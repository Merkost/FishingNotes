package com.joesemper.fishing.presentation.map.catches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.data.repository.map.MapRepository
import com.joesemper.fishing.data.repository.map.catches.CatchesRepository
import com.joesemper.fishing.model.common.Progress
import com.joesemper.fishing.presentation.map.marker.MarkerDetailsViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NewCatchViewModel(private val repository: CatchesRepository) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<MarkerDetailsViewState> =
        MutableStateFlow(MarkerDetailsViewState.Loading)

    fun addNewCatch(newCatch: RawUserCatch) {
//        viewStateFlow.value = MapViewState.Loading
        viewModelScope.launch {
            repository.addNewCatch(newCatch).collect { progress ->
                when(progress) {
                    is Progress.Complete -> { }
                    is Progress.Loading -> { }
                    is Progress.Error -> { }
                }
            }
        }
    }
}