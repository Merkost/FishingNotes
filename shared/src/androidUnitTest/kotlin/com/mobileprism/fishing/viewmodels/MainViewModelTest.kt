package com.mobileprism.fishing.viewmodels

import com.mobileprism.fishing.domain.entity.common.SyncState
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.SyncStatusProvider
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.testutils.user
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testUser = user(
        uid = "user-1",
        displayName = "John",
        email = "john@test.com",
    )

    private val syncStateFlow = MutableStateFlow<SyncState>(SyncState.Synced)

    private lateinit var syncStatusProvider: SyncStatusProvider
    private lateinit var userPreferences: UserPreferences

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        syncStatusProvider = mockk()
        every { syncStatusProvider.globalSyncState } returns syncStateFlow
        userPreferences = mockk()
        every { userPreferences.hasCompletedOnboarding } returns flowOf(true)
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

    @Test
    fun initLoadsUserSuccessfully() = runTest {
        val repo = fakeUserRepository(currentUserFlow = flowOf(testUser), isLoggedIn = true, cachedUser = testUser)
        val viewModel = MainViewModel(repo, syncStatusProvider, userPreferences)
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertIs<BaseViewState.Success<User>>(state)
        assertEquals("John", state.data.displayName)
    }

    @Test
    fun nullUserFromRepositoryEmitsSuccessNull() = runTest {
        val repo = fakeUserRepository(currentUserFlow = flowOf(null), isLoggedIn = false)
        val viewModel = MainViewModel(repo, syncStatusProvider, userPreferences)
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertIs<BaseViewState.Success<User?>>(state)
        assertEquals(null, state.data)
    }

    @Test
    fun cachedSessionSkipsInitialNullEmission() = runTest {
        val authFlow = MutableSharedFlow<User?>()
        val repo = fakeUserRepository(currentUserFlow = authFlow, isLoggedIn = true, cachedUser = testUser)
        val viewModel = MainViewModel(repo, syncStatusProvider, userPreferences)

        authFlow.emit(null)
        advanceTimeBy(100)

        assertIs<BaseViewState.Success<User?>>(viewModel.userState.value)
        assertEquals("John", (viewModel.userState.value as BaseViewState.Success).data?.displayName)

        authFlow.emit(testUser)
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertIs<BaseViewState.Success<User>>(state)
        assertEquals("John", state.data.displayName)
    }

    @Test
    fun noSessionShowsLoginImmediately() = runTest {
        // No cached session — even before any flow emission, state is Success(null)
        val authFlow = MutableSharedFlow<User?>()
        val repo = fakeUserRepository(currentUserFlow = authFlow, isLoggedIn = false)
        val viewModel = MainViewModel(repo, syncStatusProvider, userPreferences)
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertIs<BaseViewState.Success<User?>>(state)
        assertEquals(null, state.data)
    }

    @Test
    fun errorFromRepositoryProducesErrorState() = runTest {
        val exception = RuntimeException("Connection failed")
        val repo = fakeUserRepository(
            currentUserFlow = flow { throw exception },
            isLoggedIn = false
        )
        val viewModel = MainViewModel(repo, syncStatusProvider, userPreferences)
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertIs<BaseViewState.Error>(state)
        assertEquals("Connection failed", state.error?.message)
    }

    @Test
    fun syncStateReflectsProviderState() = runTest {
        val repo = fakeUserRepository(currentUserFlow = flowOf(testUser), isLoggedIn = true, cachedUser = testUser)
        val viewModel = MainViewModel(repo, syncStatusProvider, userPreferences)
        advanceUntilIdle()

        assertIs<SyncState.Synced>(viewModel.syncState.value)

        syncStateFlow.value = SyncState.Pending
        assertIs<SyncState.Pending>(viewModel.syncState.value)

        syncStateFlow.value = SyncState.Error("Sync failed")
        val errorState = viewModel.syncState.value
        assertIs<SyncState.Error>(errorState)
        assertEquals("Sync failed", errorState.message)
    }
}
