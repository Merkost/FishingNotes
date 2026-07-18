package com.mobileprism.fishing.viewmodels

import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.testutils.user
import com.mobileprism.fishing.ui.viewmodels.LoginUiState
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testUser = user(
        uid = "user-1",
        displayName = "John",
        email = "john@test.com",
    )

    private lateinit var analyticsTracker: AnalyticsTracker

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        analyticsTracker = mockk(relaxed = true)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakeUserRepository(
        currentUserFlow: Flow<User?> = flowOf(null),
        addNewUserResult: Result<Unit> = Result.success(Unit),
    ) = object : UserRepository {
        override val currentUser: Flow<User?> = currentUserFlow
        override val datastoreUser: Flow<User> = flowOf(testUser)
        override val isLoggedIn: Boolean = false
        override val cachedUser: User? = null
        override suspend fun logoutCurrentUser() {}
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
        override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> = Result.success(Unit)
        override suspend fun addNewUser(user: User): Result<Unit> = addNewUserResult
        override suspend fun setUserListener(user: User) {}
        override suspend fun setNewProfileData(user: User): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun `onGoogleSignInCancelled sets state to Idle regardless of prior state`() = runTest {
        val repo = fakeUserRepository(
            currentUserFlow = flowOf(testUser),
            addNewUserResult = Result.success(Unit),
        )
        val viewModel = LoginViewModel(repo, analyticsTracker)
        advanceUntilIdle()

        assertEquals(LoginUiState.Success, viewModel.uiState.value)

        viewModel.onGoogleSignInCancelled()

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `observeCurrentUser emits Error when repository flow throws`() = runTest {
        val repo = fakeUserRepository(
            currentUserFlow = flow { throw RuntimeException("boom") }
        )
        val viewModel = LoginViewModel(repo, analyticsTracker)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<LoginUiState.Error>(state)
        assertEquals("boom", state.message)
        verify(exactly = 1) { analyticsTracker.logEvent(AnalyticsEvent.SignInError("boom")) }
    }

    @Test
    fun `observeCurrentUser keeps Error state when analytics logging throws`() = runTest {
        every { analyticsTracker.logEvent(any()) } throws RuntimeException("analytics failed")
        val repo = fakeUserRepository(
            currentUserFlow = flow { throw RuntimeException("boom") }
        )
        val viewModel = LoginViewModel(repo, analyticsTracker)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<LoginUiState.Error>(state)
        assertEquals("boom", state.message)
    }

    @Test
    fun `onGoogleSignInFailed sets Error state and logs analytics`() = runTest {
        val repo = fakeUserRepository(currentUserFlow = flowOf(null))
        val viewModel = LoginViewModel(repo, analyticsTracker)
        advanceUntilIdle()

        viewModel.onGoogleSignInFailed()

        val state = viewModel.uiState.value
        assertIs<LoginUiState.Error>(state)
        verify(exactly = 1) {
            analyticsTracker.logEvent(AnalyticsEvent.SignInError("google_sign_in_null_result"))
        }
    }

    @Test
    fun successfulUserSignInTransitionsSigningToSuccess() = runTest {
        val repo = fakeUserRepository(
            currentUserFlow = flowOf(testUser),
            addNewUserResult = Result.success(Unit),
        )
        val viewModel = LoginViewModel(repo, analyticsTracker)
        advanceUntilIdle()

        assertEquals(LoginUiState.Success, viewModel.uiState.value)
    }
}
