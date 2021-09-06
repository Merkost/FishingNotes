package com.joesemper.fishing.domain

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.ui.adapters.PhotosRecyclerViewItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NewCatchViewModel(private val repository: UserContentRepository) : ViewModel() {

    companion object {
        private const val ITEM_ADD_PHOTO = "ITEM_ADD_PHOTO"
    }
    
    public var images = mutableStateListOf(ITEM_ADD_PHOTO)


    fun addPhoto(uri: String) {
        images.add(uri)
    }

//    fun deletePhoto(item: ) {
//        data.remove(item)
//        notifyDataSetChanged()
//    }

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun subscribe(): StateFlow<BaseViewState> {
        return viewStateFlow
    }

    fun addNewCatch(newCatch: RawUserCatch) {
        viewStateFlow.value = BaseViewState.Loading(null)
        viewModelScope.launch {
            repository.addNewCatch(newCatch).collect { progress ->
                when (progress) {
                    is Progress.Complete -> {
                        viewStateFlow.value = BaseViewState.Success(progress)
                    }
                    is Progress.Loading -> {
                        viewStateFlow.value = BaseViewState.Loading(null)
                    }
                    is Progress.Error -> {
                        viewStateFlow.value =
                            BaseViewState.Error(progress.error)
                    }
                }
            }
        }
    }
}