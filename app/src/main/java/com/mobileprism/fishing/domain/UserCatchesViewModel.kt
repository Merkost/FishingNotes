package com.mobileprism.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.use_cases.GetUserCatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserCatchesViewModel(
    private val userCatchesUseCase: GetUserCatchesUseCase
    ) : ViewModel() {

    private val _currentContent = MutableStateFlow<List<UserCatch>>(mutableListOf())
    val currentContent = _currentContent.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.InProgress)
    val uiState: StateFlow<UiState>
        get() = _uiState

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        _uiState.value = UiState.InProgress
        viewModelScope.launch {
            userCatchesUseCase.invoke().collectLatest {
                _currentContent.emit(it)
                _uiState.value = UiState.Success
            }
        }
    }
}