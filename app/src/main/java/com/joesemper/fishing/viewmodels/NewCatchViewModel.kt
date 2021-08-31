package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.entity.raw.RawUserCatch
import com.joesemper.fishing.data.entity.common.Progress
import com.joesemper.fishing.data.repository.UserContentRepository
import com.joesemper.fishing.viewmodels.viewstates.NewCatchViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NewCatchViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<NewCatchViewState> =
        MutableStateFlow(NewCatchViewState.Success)

    fun subscribe(): StateFlow<NewCatchViewState> {
        return viewStateFlow
    }

    fun addNewCatch(newCatch: RawUserCatch) {
        viewStateFlow.value = NewCatchViewState.Loading
        viewModelScope.launch {
            repository.addNewCatch(newCatch).collect { progress ->
                when(progress) {
                    is Progress.Complete -> { viewStateFlow.value = NewCatchViewState.Success
                    }
                    is Progress.Loading -> { viewStateFlow.value = NewCatchViewState.Loading
                    }
                    is Progress.Error -> { viewStateFlow.value =
                        NewCatchViewState.Error(progress.error)
                    }
                }
            }
        }
    }
}