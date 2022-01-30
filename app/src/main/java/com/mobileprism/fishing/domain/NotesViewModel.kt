package com.mobileprism.fishing.domain

import androidx.lifecycle.ViewModel
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotesViewModel(private val repository: UserContentRepository): ViewModel() {

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun subscribe(): StateFlow<BaseViewState> = viewStateFlow
}