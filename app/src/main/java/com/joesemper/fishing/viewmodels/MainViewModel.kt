package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.viewmodels.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(private val repository: AuthManager) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun subscribe(): StateFlow<BaseViewState> = mutableStateFlow
}