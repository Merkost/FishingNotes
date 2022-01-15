package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.compose.ui.home.UiState
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.app.CatchesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Collections.addAll

class UserCatchesViewModel(private val repository: CatchesRepository) : ViewModel() {

    private val _currentContent = MutableStateFlow<MutableList<UserCatch>>(mutableListOf())
    val currentContent: StateFlow<List<UserCatch>>
        get() = _currentContent

    private val _uiState = MutableStateFlow<UiState>(UiState.InProgress)
    val uiState: StateFlow<UiState>
        get() = _uiState

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        _uiState.value = UiState.InProgress
        viewModelScope.launch {
            repository.getAllUserCatchesState().collect { contentState ->

                contentState.modified.forEach { newCatch ->
                    _currentContent.value.removeAll { oldCatch ->
                        newCatch.id == oldCatch.id
                    }
                }
                _currentContent.value.apply {
                    addAll(contentState.added)
                    removeAll(contentState.deleted)
                    addAll(contentState.modified)
                }

                _uiState.value = UiState.Success
            }
        }
    }
}