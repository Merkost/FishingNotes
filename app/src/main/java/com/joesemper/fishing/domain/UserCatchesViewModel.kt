package com.joesemper.fishing.domain

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.repository.app.CatchesRepository
import kotlinx.coroutines.launch

class UserCatchesViewModel(private val repository: CatchesRepository) : ViewModel() {

    val currentContent = mutableStateListOf<UserCatch>()

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        viewModelScope.launch {
            repository.getAllUserCatchesState().collect { contentState ->
//                if (currentContent.firstOrNull() == null) {
//                    currentContent.
//                }
                contentState.modified.forEach { newCatch ->
                    currentContent.removeAll { oldCatch ->
                        newCatch.id == oldCatch.id
                    }
                }
                currentContent.apply {
                    addAll(contentState.added)
                    removeAll(contentState.deleted)
                    addAll(contentState.modified)
                }
            }
        }
    }
}