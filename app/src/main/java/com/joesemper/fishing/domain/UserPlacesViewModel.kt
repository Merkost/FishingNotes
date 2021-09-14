package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.view.weather.utils.getDateByMilliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.sql.Time

class UserPlacesViewModel(private val repository: UserContentRepository) : ViewModel() {

    val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    init {
        loadAllUserPlaces()
    }

    fun subscribe(): StateFlow<BaseViewState> {
        return viewStateFlow
    }

    private fun loadAllUserPlaces() {
        val start = System.currentTimeMillis()
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { places ->
                //for loading animation
                if (System.currentTimeMillis() - start < 1500) delay(1500)
                viewStateFlow.value = BaseViewState.Success(places)
            }
        }
    }

}