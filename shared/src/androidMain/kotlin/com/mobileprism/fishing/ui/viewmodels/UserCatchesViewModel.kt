package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.use_cases.catches.GetUserCatchesUseCase
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.ui.utils.enums.toFirestoreOrder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import android.util.Log

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

    val catchesPaged: Flow<PagingData<UserCatch>> = _sortOrder
        .flatMapLatest { sort ->
            val (field, dir) = sort.toFirestoreOrder()
            repository.getAllUserCatchesPaged(field, dir)
        }
        .cachedIn(viewModelScope)

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        _uiState.value = UiState.InProgress
        viewModelScope.launch {
            userCatchesUseCase.invoke()
                .catch {
                    Log.e("UserCatchesVM", "Failed to load catches", it)
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
                Log.e("UserCatchesVM", "Failed to refresh catches", e)
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
