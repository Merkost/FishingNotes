package com.joesemper.fishing.viewmodel.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.common.UserCatch
import com.joesemper.fishing.model.entity.states.AddNewCatchState
import com.joesemper.fishing.model.repository.map.MapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MapViewModel(
    private val mapRepository: MapRepository
) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<MapViewState> =
        MutableStateFlow(MapViewState.Loading)

    fun subscribe(): StateFlow<MapViewState> = mutableStateFlow

    init {
        loadUsersMarkers()
    }

    override fun onCleared() {
        super.onCleared()
        mutableStateFlow.value = MapViewState.Loading
    }

    private fun loadUsersMarkers() {
        viewModelScope.launch {
            mapRepository.getAllUserMarkers()
                .collect { markers -> onSuccess(markers) }
        }
    }

    fun addNewCatch(userCatch: UserCatch) {
        mutableStateFlow.value = MapViewState.Loading
        viewModelScope.launch {
            mapRepository.addNewCatch(userCatch).collect { addNewCatchState ->
                when(addNewCatchState) {
                    is AddNewCatchState.Success -> onSuccess(listOf())
                    is AddNewCatchState.Loading -> mutableStateFlow.value = MapViewState.Loading
                    is AddNewCatchState.Error -> onError(addNewCatchState.error)
                }
            }
        }
    }

    fun deleteMarker(userCatch: UserCatch) {
        viewModelScope.launch {
            mapRepository.deleteMarker(userCatch)
        }
    }

    private fun onSuccess(userCatches: List<UserCatch?>) {
        mutableStateFlow.value = MapViewState.Success(userCatches)
    }

    private fun onError(error: Throwable) {
        mutableStateFlow.value = MapViewState.Error(error)
    }
}