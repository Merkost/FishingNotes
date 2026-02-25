package com.mobileprism.fishing.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.statistics.CatchStatistics
import com.mobileprism.fishing.domain.use_cases.catches.GetCatchStatisticsUseCase
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val getStatisticsUseCase: GetCatchStatisticsUseCase
) : ViewModel() {

    private val _statisticsState =
        MutableStateFlow<BaseViewState<CatchStatistics>>(BaseViewState.Loading())
    val statisticsState: StateFlow<BaseViewState<CatchStatistics>> = _statisticsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        _statisticsState.value = BaseViewState.Loading()
        viewModelScope.launch {
            getStatisticsUseCase()
                .catch { e ->
                    Log.e("StatisticsVM", "Failed to load statistics", e)
                    _statisticsState.value = BaseViewState.Error(e)
                }
                .collectLatest { stats ->
                    _statisticsState.value = BaseViewState.Success(stats)
                    _isRefreshing.value = false
                }
        }
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            getStatisticsUseCase()
                .catch { e ->
                    Log.e("StatisticsVM", "Failed to refresh statistics", e)
                    _isRefreshing.value = false
                }
                .collectLatest { stats ->
                    _statisticsState.value = BaseViewState.Success(stats)
                    _isRefreshing.value = false
                }
        }
    }

    fun retry() {
        loadStatistics()
    }
}
