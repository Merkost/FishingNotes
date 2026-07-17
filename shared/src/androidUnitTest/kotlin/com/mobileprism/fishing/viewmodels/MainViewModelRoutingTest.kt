package com.mobileprism.fishing.viewmodels

import com.mobileprism.fishing.domain.entity.common.SyncState
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.SyncStatusProvider
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.testutils.user
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelRoutingTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testUser = user(
        uid = "user-1",
        displayName = "John",
        email = "john@test.com",
    )

    private val syncStateFlow = MutableStateFlow<SyncState>(SyncState.Synced)

    private lateinit var syncStatusProvider: SyncStatusProvider

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        syncStatusProvider = mockk()
        every { syncStatusProvider.globalSyncState } returns syncStateFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakeUserRepository(
        currentUserFlow: Flow<User?> = flowOf(null),
        isLoggedIn: Boolean = false,
        cachedUser: User? = null,
    ) = object : UserRepository {
        override val currentUser: Flow<User?> = currentUserFlow
        override val datastoreUser: Flow<User> = flowOf(testUser)
        override val isLoggedIn: Boolean = isLoggedIn
        override val cachedUser: User? = cachedUser
        override suspend fun logoutCurrentUser() {}
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
        override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> = Result.success(Unit)
        override suspend fun addNewUser(user: User): Result<Unit> = error("Not used")
        override suspend fun setUserListener(user: User) {}
        override suspend fun setNewProfileData(user: User): Result<Unit> = Result.success(Unit)
    }

    private fun fakeUserPreferences(onboardingFlow: Flow<Boolean>): UserPreferences {
        val prefs = mockk<UserPreferences>()
        every { prefs.hasCompletedOnboarding } returns onboardingFlow
        return prefs
    }

    @Test
    fun `routing stays Splash when onboarding flow has not emitted`() = runTest {
        val authFlow = MutableSharedFlow<User?>()
        val repo = fakeUserRepository(currentUserFlow = authFlow, isLoggedIn = false)
        val onboardingFlow = MutableSharedFlow<Boolean>()
        val prefs = fakeUserPreferences(onboardingFlow)

        val vm = MainViewModel(repo, syncStatusProvider, prefs)
        advanceUntilIdle()

        assertEquals(RoutingDecision.Splash, vm.routing.value)
    }

    @Test
    fun `routing is Onboarding when userState is Success(null) and onboarding flow emits false`() = runTest {
        val repo = fakeUserRepository(currentUserFlow = flowOf(null), isLoggedIn = false)
        val onboardingFlow = MutableStateFlow(false)
        val prefs = fakeUserPreferences(onboardingFlow)

        val vm = MainViewModel(repo, syncStatusProvider, prefs)
        advanceUntilIdle()

        assertEquals(RoutingDecision.Onboarding, vm.routing.value)
    }

    @Test
    fun `routing is Login when userState is Success(null) and onboarding flow emits true`() = runTest {
        val repo = fakeUserRepository(currentUserFlow = flowOf(null), isLoggedIn = false)
        val onboardingFlow = MutableStateFlow(true)
        val prefs = fakeUserPreferences(onboardingFlow)

        val vm = MainViewModel(repo, syncStatusProvider, prefs)
        advanceUntilIdle()

        assertEquals(RoutingDecision.Login, vm.routing.value)
    }

    @Test
    fun `routing is Home when userState is Success(user) and onboarding flow emits true`() = runTest {
        val repo = fakeUserRepository(
            currentUserFlow = flowOf(testUser),
            isLoggedIn = true,
            cachedUser = testUser,
        )
        val onboardingFlow = MutableStateFlow(true)
        val prefs = fakeUserPreferences(onboardingFlow)

        val vm = MainViewModel(repo, syncStatusProvider, prefs)
        advanceUntilIdle()

        assertEquals(RoutingDecision.Home, vm.routing.value)
    }
}
