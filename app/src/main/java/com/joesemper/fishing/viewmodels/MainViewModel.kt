package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.viewmodels.viewstates.MainViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<MainViewState> =
        MutableStateFlow(MainViewState.Success)

    fun subscribe(): StateFlow<MainViewState> = mutableStateFlow

}