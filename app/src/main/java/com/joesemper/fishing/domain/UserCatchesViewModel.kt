package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.domain.viewstates.ContentState
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserCatchesViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Loading(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        viewModelScope.launch {
            repository.getAllUserCatchesState().collect { contentState ->
                val currentContent: List<UserCatch> = when (_uiState.value) {
                    is BaseViewState.Loading -> listOf()
                    is BaseViewState.Success<*> -> {
                        val state = _uiState.value as BaseViewState.Success<List<UserCatch>>
                        state.data
                    }
                    else -> listOf()
                }
                when (contentState) {
                    is ContentState.Added -> {
                        val newContent = contentState.content
                        val result = mutableListOf<UserCatch>()
                        result.addAll(currentContent)
                        result.addAll(newContent)
                        _uiState.value = BaseViewState.Success(result)
                    }
                    is ContentState.Deleted -> {
                        val deletedContent = contentState.content
                        (currentContent as MutableList).removeAll(deletedContent)
                        _uiState.value = BaseViewState.Success(currentContent)
                    }
                }
            }
//            repository.getAllUserCatchesList().collect { catches ->
//                _uiState.value = BaseViewState.Success(catches)
//            }
        }
    }
}