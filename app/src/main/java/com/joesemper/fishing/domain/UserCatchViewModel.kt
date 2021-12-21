package com.joesemper.fishing.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserRepository
import com.joesemper.fishing.model.repository.app.CatchesRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserCatchViewModel(
    private val markersRepo: MarkersRepository,
    private val catchesRepo: CatchesRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    val catch: MutableState<UserCatch?> = mutableStateOf(null)
    val mapMarker: MutableState<UserMapMarker?> = mutableStateOf(null)

    fun updateCatch(data: Map<String, Any>) {
        mapMarker.value?.let { marker ->
            catch.value?.let { catch ->
                viewModelScope.launch {
                    catchesRepo.updateUserCatch(
                        markerId = marker.id,
                        catchId = catch.id,
                        data = data
                    )
                }
            }
        }
    }

    fun deleteCatch() {
        viewModelScope.launch {
            catch.value?.let {
                catchesRepo.deleteCatch(it)
            }
        }
    }

    fun getCurrentUser() = userRepository.currentUser

    fun getMapMarker(markerId: String) {
        viewModelScope.launch {
            markersRepo.getMapMarker(markerId).collect {
                mapMarker.value = it
                subscribeOnCatchChanges()
            }
        }
    }

    suspend fun subscribeOnCatchChanges() {
        mapMarker.value?.let { marker ->
            catch.value?.let { oldCatch ->
                catchesRepo.subscribeOnUserCatchState(
                    markerId = marker.id,
                    catchId = oldCatch.id
                ).collect {
                    catch.value = it
                }
            }
        }
    }

}