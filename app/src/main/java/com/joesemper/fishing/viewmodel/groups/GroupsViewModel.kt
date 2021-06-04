package com.joesemper.fishing.viewmodel.groups

import androidx.lifecycle.LiveData
import com.joesemper.fishing.model.repository.groups.GroupsRepository
import com.joesemper.fishing.viewmodel.base.BaseViewModel
import kotlinx.coroutines.*

class GroupsViewModel(private val repository: GroupsRepository) : BaseViewModel<GroupsViewState>() {



    fun subscribe(): LiveData<GroupsViewState> = mutableLiveData


    override fun handleError(error: Throwable) {
        mutableLiveData.postValue(GroupsViewState.Error(error))
    }

}