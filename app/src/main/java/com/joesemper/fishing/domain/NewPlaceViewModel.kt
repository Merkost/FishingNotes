package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.maps.model.Marker
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NewPlaceViewModel(private val repository: MarkersRepository) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    fun subscribe(): StateFlow<BaseViewState> = viewStateFlow

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
        viewStateFlow.value = BaseViewState.Success(null)
    }

    private fun onLoading() {
        viewStateFlow.value = BaseViewState.Loading(null)
    }

    private fun onError(error: Throwable) {
        viewStateFlow.value = BaseViewState.Error(error)
    }
}