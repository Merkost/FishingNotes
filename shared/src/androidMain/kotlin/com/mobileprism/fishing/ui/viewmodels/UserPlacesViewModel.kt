package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import com.mobileprism.fishing.ui.utils.enums.toFirestoreOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import android.util.Log

class UserPlacesViewModel(private val repository: MarkersRepositoryPaged) : ViewModel() {

    private val _currentContent = MutableStateFlow<List<UserMapMarker>>(listOf())
    val currentContent: StateFlow<List<UserMapMarker>>
    get() = _currentContent

    private val _uiState = MutableStateFlow<UiState>(UiState.InProgress)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _sortOrder = MutableStateFlow(PlacesSortValues.Default)

    val placesPaged: Flow<PagingData<UserMapMarker>> = _sortOrder
        .flatMapLatest { sort ->
            val (field, dir) = sort.toFirestoreOrder()
            repository.getAllUserMarkersListPaged(field, dir)
        }
        .cachedIn(viewModelScope)

    init {
        loadAllUserPlaces()
    }

    private fun loadAllUserPlaces() {
        _uiState.value = UiState.InProgress
        viewModelScope.launch {
            repository.getAllUserMarkersList()
                .catch {
                    Log.e("UserPlacesVM", "Failed to load places", it)
                    _uiState.value = UiState.Error
                }
                .collect { userPlaces ->
                    _currentContent.value = userPlaces as List<UserMapMarker>
                    _uiState.value = UiState.Success
                }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            repository.getAllUserMarkersList()
                .catch {
                    Log.e("UserPlacesVM", "Failed to refresh places", it)
                    _isRefreshing.value = false
                }
                .collect { userPlaces ->
                    _currentContent.value = userPlaces as List<UserMapMarker>
                    _uiState.value = UiState.Success
                    _isRefreshing.value = false
                }
        }
    }

    fun setSortOrder(sort: PlacesSortValues) {
        _sortOrder.value = sort
    }

    fun retry() { loadAllUserPlaces() }
}
