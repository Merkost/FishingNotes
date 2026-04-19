package com.mobileprism.fishing.ui.viewmodels

import com.mobileprism.fishing.domain.entity.statistics.CatchStatistics
import com.mobileprism.fishing.domain.use_cases.catches.GetCatchStatisticsUseCase
import com.mobileprism.fishing.testutils.userCatch
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getStatisticsUseCase: GetCatchStatisticsUseCase

    private val sampleStatistics = CatchStatistics(
        totalCatches = 3,
        totalWeight = 7.5,
        averageWeight = 2.5,
        heaviestCatch = userCatch(fishWeight = 5.0),
        totalSpecies = 2,
        mostCaughtSpecies = "Bass",
        catchesByMonth = mapOf("2023-11" to 3),
        catchesBySpecies = mapOf("Bass" to 2, "Trout" to 1),
        weightBySpecies = mapOf("Bass" to 5.0, "Trout" to 2.5),
        catchesByWeather = mapOf("Clear" to 3),
        catchesByTemperatureRange = mapOf("10-20\u00B0C" to 3),
        catchesByMoonPhase = mapOf("Full Moon" to 3),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getStatisticsUseCase = mockk()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsStatisticsSuccessfully() {
        every { getStatisticsUseCase() } returns flowOf(sampleStatistics)

        val viewModel = StatisticsViewModel(getStatisticsUseCase)

        val state = viewModel.statisticsState.value
        assertIs<BaseViewState.Success<CatchStatistics>>(state)
        assertEquals(3, state.data.totalCatches)
        assertEquals(7.5, state.data.totalWeight)
        assertEquals("Bass", state.data.mostCaughtSpecies)
    }

    @Test
    fun initErrorFromUseCaseProducesErrorState() {
        val exception = RuntimeException("Database error")
        every { getStatisticsUseCase() } returns flow { throw exception }

        val viewModel = StatisticsViewModel(getStatisticsUseCase)

        val state = viewModel.statisticsState.value
        assertIs<BaseViewState.Error>(state)
        assertEquals("Database error", state.error?.message)
    }

    @Test
    fun refreshSetsIsRefreshingAndLoadsStatistics() {
        every { getStatisticsUseCase() } returns flowOf(sampleStatistics)

        val viewModel = StatisticsViewModel(getStatisticsUseCase)

        // After init, isRefreshing should be false
        assertFalse(viewModel.isRefreshing.value)

        // refresh triggers another load
        viewModel.refresh()

        // With UnconfinedTestDispatcher, the flow completes immediately,
        // so isRefreshing should be false after completion
        assertFalse(viewModel.isRefreshing.value)
        val state = viewModel.statisticsState.value
        assertIs<BaseViewState.Success<CatchStatistics>>(state)
        assertEquals(3, state.data.totalCatches)
    }

    @Test
    fun refreshErrorKeepsIsRefreshingFalse() {
        // First call succeeds (init), second call fails (refresh)
        every { getStatisticsUseCase() } returns flowOf(sampleStatistics) andThen
                flow { throw RuntimeException("Network error") }

        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        assertIs<BaseViewState.Success<CatchStatistics>>(viewModel.statisticsState.value)

        viewModel.refresh()

        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun retryReloadsStatistics() {
        // First call fails (init), second call succeeds (retry)
        every { getStatisticsUseCase() } returns flow { throw RuntimeException("Fail") } andThen
                flowOf(sampleStatistics)

        val viewModel = StatisticsViewModel(getStatisticsUseCase)
        assertIs<BaseViewState.Error>(viewModel.statisticsState.value)

        viewModel.retry()

        val state = viewModel.statisticsState.value
        assertIs<BaseViewState.Success<CatchStatistics>>(state)
        assertEquals(3, state.data.totalCatches)
        verify(exactly = 2) { getStatisticsUseCase() }
    }
}
