package com.mobileprism.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserPlaceCatchesViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<BaseViewState<List<UserCatch>>> =
        MutableStateFlow(BaseViewState.Loading(null))
    val uiState = viewStateFlow.asStateFlow()

    fun loadCatchesByMarkerId(markerId: String) {
        viewModelScope.launch {
            repository.getCatchesByMarkerId(markerId).collect { catches ->
                viewStateFlow.value = BaseViewState.Success(catches)
            }
        }
    }
}