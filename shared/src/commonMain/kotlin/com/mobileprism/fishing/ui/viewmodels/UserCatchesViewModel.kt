package com.mobileprism.fishing.ui.viewmodels

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import org.kimplify.cedar.logging.Cedar
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.use_cases.catches.GetUserCatchesUseCase
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.ui.utils.enums.toFirestoreOrder
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.catch_deleted
import fishing.shared.generated.resources.undo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserCatchesViewModel(
    private val userCatchesUseCase: GetUserCatchesUseCase,
    private val repository: CatchesRepository
    ) : ViewModel() {

    private val _currentContent = MutableStateFlow<List<UserCatch>>(mutableListOf())
    val currentContent = _currentContent.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.InProgress)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _sortOrder = MutableStateFlow(CatchesSortValues.Default)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val catchesPaged: Flow<PagingData<UserCatch>> =
        combine(_sortOrder, _searchQuery) { sort, query -> sort to query }
            .flatMapLatest { (sort, query) ->
                val (field, dir) = sort.toFirestoreOrder()
                repository.getAllUserCatchesPaged(field, dir).map { pagingData ->
                    if (query.isBlank()) {
                        pagingData
                    } else {
                        pagingData.filter { catch ->
                            catch.fishType.contains(query, ignoreCase = true) ||
                                catch.placeTitle.contains(query, ignoreCase = true) ||
                                catch.description.contains(query, ignoreCase = true)
                        }
                    }
                }
            }
            .cachedIn(viewModelScope)

    init {
        loadAllUserCatches()
    }

    fun deleteCatch(userCatch: UserCatch) {
        viewModelScope.launch {
            repository.deleteCatch(userCatch)
            SnackbarManager.showMessage(
                messageTextId = Res.string.catch_deleted,
                snackbarAction = SnackbarAction(
                    textId = Res.string.undo,
                    action = {
                        viewModelScope.launch {
                            repository.addNewCatch(userCatch.userMarkerId, userCatch)
                        }
                    },
                ),
                duration = SnackbarDuration.Long,
            )
        }
    }

    private fun loadAllUserCatches() {
        _uiState.value = UiState.InProgress
        viewModelScope.launch {
            userCatchesUseCase.invoke()
                .catch {
                    Cedar.tag("UserCatchesVM").e("Failed to load catches")
                    _uiState.value = UiState.Error
                }
                .collectLatest {
                    _currentContent.emit(it)
                    _uiState.value = UiState.Success
                }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var refreshJob: Job? = null

    fun refresh() {
        refreshJob?.cancel()
        _isRefreshing.value = true
        refreshJob = viewModelScope.launch {
            try {
                userCatchesUseCase.invoke()
                    .collectLatest {
                        _currentContent.emit(it)
                        _uiState.value = UiState.Success
                    }
            } catch (e: Exception) {
                Cedar.tag("UserCatchesVM").e("Failed to refresh catches")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setSortOrder(sort: CatchesSortValues) {
        _sortOrder.value = sort
    }

    fun retry() { loadAllUserCatches() }
}
