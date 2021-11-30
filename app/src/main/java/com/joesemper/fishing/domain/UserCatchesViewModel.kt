package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.app.CatchesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserCatchesViewModel(private val repository: CatchesRepository) : ViewModel() {

    val currentContent = MutableStateFlow<MutableList<UserCatch>?>(null)

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        viewModelScope.launch {
            repository.getAllUserCatchesState().collect { contentState ->
                if (currentContent.value == null) {
                    currentContent.value = mutableListOf()
                }
                currentContent.value?.apply {
                    addAll(contentState.added)
                    removeAll(contentState.deleted)
                }
            }
        }
    }
}