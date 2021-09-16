package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotesViewModel(private val repository: UserContentRepository): ViewModel() {

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun subscribe(): StateFlow<BaseViewState> = viewStateFlow

    private fun loadUserContent() {
//        viewModelScope.launch {
//            repository.getAllUserContentList().collectLatest { userContent ->
//                viewStateFlow.value = BaseViewState.Success(userContent)
//            }
//        }
    }

}