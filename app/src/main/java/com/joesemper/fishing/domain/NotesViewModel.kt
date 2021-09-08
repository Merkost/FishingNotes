package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: UserContentRepository): ViewModel() {

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    init {
        loadUserContent()
    }

    fun subscribe(): StateFlow<BaseViewState> = viewStateFlow

    private fun loadUserContent() {
        viewModelScope.launch {
            repository.getAllUserContentList().collect { userContent ->
                viewStateFlow.value = BaseViewState.Success(userContent)
            }
        }
    }

}