package com.mobileprism.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.ui.home.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewPlaceViewModel(private val repository: MarkersRepository) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.InProgress)

    fun subscribe(): StateFlow<UiState> = viewStateFlow

    fun addNewMarker(newMarker: RawMapMarker) {
        viewModelScope.launch {
            repository.addNewMarker(newMarker).collect { progress ->
                when (progress) {
                    is Progress.Complete -> {
                        onSuccess()
                    }
                    is Progress.Loading -> {
                        onLoading()
                    }
                    is Progress.Error -> onError(progress.error)
                }
            }
        }
    }

    private fun onSuccess() {
        viewStateFlow.value = UiState.Success
    }

    private fun onLoading() {
        viewStateFlow.value = UiState.InProgress
    }

    private fun onError(error: Throwable) {
        viewStateFlow.value = UiState.Error
    }
}