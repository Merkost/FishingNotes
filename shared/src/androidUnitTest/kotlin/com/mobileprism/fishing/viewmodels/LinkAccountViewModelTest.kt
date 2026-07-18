package com.mobileprism.fishing.viewmodels

import com.mobileprism.fishing.domain.repository.LinkOutcome
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.viewmodels.LinkAccountViewModel
import com.mobileprism.fishing.ui.viewmodels.LinkState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LinkAccountViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val analytics: AnalyticsTracker = mockk(relaxed = true)

    @BeforeTest fun setUp() { Dispatchers.setMain(dispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `successful link ends in Success`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        coEvery { repo.linkWithGoogle("tok") } returns Result.success(LinkOutcome.Linked)
        val vm = LinkAccountViewModel(repo, analytics)

        vm.linkWithGoogle("tok")
        advanceUntilIdle()

        assertEquals(LinkState.Success, vm.uiState.value)
    }

    @Test
    fun `null result is a cancel, not an error`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        val vm = LinkAccountViewModel(repo, analytics)

        vm.onSignInCancelled()
        advanceUntilIdle()

        assertEquals(LinkState.Idle, vm.uiState.value)
    }

    @Test
    fun `thrown link error ends in Error`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        coEvery { repo.linkWithGoogle("tok") } returns Result.failure(RuntimeException("net"))
        val vm = LinkAccountViewModel(repo, analytics)

        vm.linkWithGoogle("tok")
        advanceUntilIdle()

        assertIs<LinkState.Error>(vm.uiState.value)
    }

    @Test
    fun `collision moves to MergeConfirm`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        coEvery { repo.linkWithGoogle("tok") } returns
            Result.failure(mockk<dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException>(relaxed = true))
        val vm = LinkAccountViewModel(repo, analytics)

        vm.linkWithGoogle("tok")
        advanceUntilIdle()

        assertIs<LinkState.MergeConfirm>(vm.uiState.value)
    }

    @Test
    fun `confirmMerge ends in MergeSuccess with counts`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        coEvery { repo.linkWithGoogle("tok") } returns
            Result.failure(mockk<dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException>(relaxed = true))
        coEvery { repo.mergeGuestIntoGoogle("tok") } returns
            Result.success(LinkOutcome.Merged(catchesAdded = 3, markersAdded = 1, alreadyPresent = 2))
        val vm = LinkAccountViewModel(repo, analytics)

        vm.linkWithGoogle("tok")
        advanceUntilIdle()
        vm.confirmMerge()
        advanceUntilIdle()

        val s = vm.uiState.value
        assertIs<LinkState.MergeSuccess>(s)
        assertEquals(3, s.catchesAdded)
    }
}
