package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NewCatchViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun subscribe(): StateFlow<BaseViewState> {
        return viewStateFlow
    }

    fun addNewCatch(newCatch: RawUserCatch) {
        viewStateFlow.value = BaseViewState.Loading(null)
        viewModelScope.launch {
            repository.addNewCatch(newCatch).collect { progress ->
                when (progress) {
                    is Progress.Complete -> {
                        viewStateFlow.value = BaseViewState.Success(progress)
                    }
                    is Progress.Loading -> {
                        viewStateFlow.value = BaseViewState.Loading(null)
                    }
                    is Progress.Error -> {
                        viewStateFlow.value =
                            BaseViewState.Error(progress.error)
                    }
                }
            }
        }
    }
}