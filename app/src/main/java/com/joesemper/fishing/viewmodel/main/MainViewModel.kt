package com.joesemper.fishing.viewmodel.main

import androidx.lifecycle.LiveData
import com.joesemper.fishing.model.repository.user.UsersRepository
import com.joesemper.fishing.viewmodel.base.BaseViewModel
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UsersRepository) : BaseViewModel<MainViewState>() {

    init {
        getCurrentUser()
    }

    fun subscribe(): LiveData<MainViewState> = mutableLiveData

    fun getCurrentUser() {
        cancelJob()
        viewModelCoroutineScope.launch {
            loadUserFromRepository()
        }

    }

    fun logOut() {
        cancelJob()
        viewModelCoroutineScope.launch {
            repository.logoutCurrentUser()
        }
        mutableLiveData.value = MainViewState.LoggedOut
    }

    private suspend fun loadUserFromRepository() {
        val user = repository.getCurrentUser()
        if (user != null) {
            mutableLiveData.value = MainViewState.LoggedIn(user)
        } else {
            mutableLiveData.value = MainViewState.LoggedOut
        }

    }

    override fun handleError(error: Throwable) {
        mutableLiveData.postValue(MainViewState.Error(error))
    }


}