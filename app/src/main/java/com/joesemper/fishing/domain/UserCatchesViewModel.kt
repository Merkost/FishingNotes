package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserCatchesViewModel(private val repository: UserContentRepository) : ViewModel() {

    val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    init {
        loadAllUserPlaces()
    }

    fun subscribe(): StateFlow<BaseViewState> {
        return viewStateFlow
    }

    private fun loadAllUserPlaces() {
        viewModelScope.launch {
            repository.getAllUserCatchesList().collect { catches ->
                viewStateFlow.value = BaseViewState.Success(catches)
            }
        }
    }
}