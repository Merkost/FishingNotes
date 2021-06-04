package com.joesemper.fishing.viewmodel.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

abstract class BaseViewModel<T: ViewState>: ViewModel() {

    protected val mutableLiveData: MutableLiveData<T> = MutableLiveData()

    protected val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        })

    protected abstract fun handleError(error: Throwable)

    override fun onCleared() {
        super.onCleared()
        cancelJob()
    }

     protected fun cancelJob() {
        viewModelCoroutineScope.coroutineContext.cancelChildren()
    }
}