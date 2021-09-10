package com.joesemper.fishing.domain

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NewCatchViewModel(private val repository: UserContentRepository) : ViewModel() {

    val marker = mutableStateOf(UserMapMarker())

    val title = mutableStateOf("")
    var description = mutableStateOf("")
    var fishAmount = mutableStateOf("0")
    var weight = mutableStateOf("0")
    var date = mutableStateOf("")
    var time = mutableStateOf("")
    var rod = mutableStateOf("")
    var bite = mutableStateOf("")
    var lure = mutableStateOf("")

    var images = mutableStateListOf<Uri>()

    fun addPhoto(uri: Uri) {
        images.add(uri)
    }

    fun deletePhoto(uri: Uri) {
        images.remove(uri)
    }

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun subscribe(): StateFlow<BaseViewState> {
        return viewStateFlow
    }

    private fun addNewCatch(newCatch: RawUserCatch) {
        viewStateFlow.value = BaseViewState.Loading(null)
        viewModelScope.launch {
            repository.addNewCatch(marker.value.id, newCatch).collect { progress ->
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

    private fun isInputCorrect(): Boolean {
        return title.value.isNotBlank()
    }

    fun createNewUserCatch(photos: List<ByteArray>): Boolean {
        if (isInputCorrect()) {
            addNewCatch(RawUserCatch(
                title = title.value,
                description = description.value,
                time = time.value,
                date = date.value,
                //fishType = fish,
                fishAmount = fishAmount.value.toInt(),
                fishWeight = weight.value.toDouble(),
                fishingRodType = rod.value,
                fishingBait = bite.value,
                fishingLure = lure.value,
                markerId = marker.value.id,
                isPublic = false,
                photos = photos
            ))
            return true
        } else return false

    }

}