package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.domain.viewstates.MarkerDetailsViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarkerDetailsViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<MarkerDetailsViewState> =
        MutableStateFlow(MarkerDetailsViewState.Loading)

    fun subscribe(markerId: String): StateFlow<MarkerDetailsViewState> {
        return viewStateFlow
    }

}