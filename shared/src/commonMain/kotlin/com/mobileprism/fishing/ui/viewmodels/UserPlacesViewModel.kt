package com.mobileprism.fishing.ui.viewmodels

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import org.kimplify.cedar.logging.Cedar
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import com.mobileprism.fishing.ui.utils.enums.toFirestoreOrder
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.place_deleted
import fishing.shared.generated.resources.undo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserPlacesViewModel(private val repository: MarkersRepositoryPaged) : ViewModel() {

    private val _currentContent = MutableStateFlow<List<UserMapMarker>>(listOf())
    val currentContent: StateFlow<List<UserMapMarker>>
    get() = _currentContent

    private val _uiState = MutableStateFlow<UiState>(UiState.InProgress)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _sortOrder = MutableStateFlow(PlacesSortValues.Default)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val placesPaged: Flow<PagingData<UserMapMarker>> =
        combine(_sortOrder, _searchQuery) { sort, query -> sort to query }
            .flatMapLatest { (sort, query) ->
                val (field, dir) = sort.toFirestoreOrder()
                repository.getAllUserMarkersListPaged(field, dir).map { pagingData ->
                    if (query.isBlank()) {
                        pagingData
                    } else {
                        pagingData.filter { marker ->
                            marker.title.contains(query, ignoreCase = true) ||
                                marker.description.contains(query, ignoreCase = true)
                        }
                    }
                }
            }
            .cachedIn(viewModelScope)

    fun deletePlace(marker: UserMapMarker) {
        viewModelScope.launch {
            repository.deleteMarker(marker)
            SnackbarManager.showMessage(
                messageTextId = Res.string.place_deleted,
                snackbarAction = SnackbarAction(
                    textId = Res.string.undo,
                    action = {
                        viewModelScope.launch { repository.addNewMarker(marker) }
                    },
                ),
                duration = SnackbarDuration.Long,
            )
        }
    }

    init {
        loadAllUserPlaces()
    }

    private fun loadAllUserPlaces() {
        _uiState.value = UiState.InProgress
        viewModelScope.launch {
            repository.getAllUserMarkersList()
                .catch {
                    Cedar.tag("UserPlacesVM").e("Failed to load places")
                    _uiState.value = UiState.Error
                }
                .collect { userPlaces ->
                    _currentContent.value = userPlaces.filterIsInstance<UserMapMarker>()
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
                    Cedar.tag("UserPlacesVM").e("Failed to refresh places")
                    _isRefreshing.value = false
                }
                .collect { userPlaces ->
                    _currentContent.value = userPlaces.filterIsInstance<UserMapMarker>()
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
