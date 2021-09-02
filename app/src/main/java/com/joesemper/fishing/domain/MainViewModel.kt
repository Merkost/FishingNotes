package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun subscribe(): StateFlow<BaseViewState> = mutableStateFlow
}