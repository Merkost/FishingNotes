package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
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

    private val currentContent = mutableListOf<UserCatch>()

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        viewModelScope.launch {
            repository.getAllUserCatchesState().collect { contentState ->

                currentContent.apply {
                    addAll(contentState.added)
                    removeAll(contentState.deleted)
                }

                _uiState.value = BaseViewState.Success(currentContent)
            }
        }
    }
}