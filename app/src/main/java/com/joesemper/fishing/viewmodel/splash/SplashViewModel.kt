package com.joesemper.fishing.viewmodel.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.splash.datasource.UsersRepository
import com.joesemper.fishing.model.splash.entity.SplashState
import kotlinx.coroutines.*

class SplashViewModel(private val repository: UsersRepository) : ViewModel() {

    private val mutableLiveData: MutableLiveData<SplashState> = MutableLiveData()

    private val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        })

    fun subscribe(): LiveData<SplashState> = mutableLiveData

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        cancelJob()

        viewModelCoroutineScope.launch {
            val user = repository.getCurrentUser()

            mutableLiveData.value = if (user != null) {
                SplashState.Authorised
            } else {
                SplashState.NotAuthorised
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelJob()
    }

    private fun handleError(error: Throwable) {
        mutableLiveData.postValue(SplashState.Error(error))
    }

    private fun cancelJob() {
        viewModelCoroutineScope.coroutineContext.cancelChildren()
    }
}