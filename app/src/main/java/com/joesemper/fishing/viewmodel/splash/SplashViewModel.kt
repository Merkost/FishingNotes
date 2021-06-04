package com.joesemper.fishing.viewmodel.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.repository.user.UsersRepository
import kotlinx.coroutines.*

class SplashViewModel(private val repository: UsersRepository) : ViewModel() {

    private val mutableLiveData: MutableLiveData<SplashViewState> = MutableLiveData()

    private val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        })

    fun subscribe(): LiveData<SplashViewState> = mutableLiveData

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        cancelJob()

        viewModelCoroutineScope.launch {
            val user = repository.getCurrentUser()

                mutableLiveData.value = if (user != null) {
                    SplashViewState.Authorised
                } else {
                    SplashViewState.NotAuthorised
                }
            }

    }

    override fun onCleared() {
        super.onCleared()
        cancelJob()
    }

    private fun handleError(error: Throwable) {
        mutableLiveData.postValue(SplashViewState.Error(error))
    }

    private fun cancelJob() {
        viewModelCoroutineScope.coroutineContext.cancelChildren()
    }
}