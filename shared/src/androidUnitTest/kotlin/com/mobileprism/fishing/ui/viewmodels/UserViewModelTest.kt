package com.mobileprism.fishing.ui.viewmodels

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.OfflineRepository
import com.mobileprism.fishing.domain.use_cases.catches.GetUserCatchesUseCase
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.testutils.user
import com.mobileprism.fishing.testutils.userCatch
import com.mobileprism.fishing.testutils.userMapMarker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var userRepository: UserRepository
    private lateinit var userDatastore: UserDatastore
    private lateinit var offlineRepository: OfflineRepository
    private lateinit var getUserCatchesUseCase: GetUserCatchesUseCase

    private val testUser = user(uid = "user-1", displayName = "John")

    private val catches = listOf(
        userCatch(id = "c1", fishWeight = 2.0, fishType = "Bass"),
        userCatch(id = "c2", fishWeight = 5.0, fishType = "Trout"),
        userCatch(id = "c3", fishWeight = 1.5, fishType = "Bass"),
    )

    private val places = listOf(
        userMapMarker(id = "m1", title = "Lake A", catchesCount = 3),
        userMapMarker(id = "m2", title = "River B", catchesCount = 7),
        userMapMarker(id = "m3", title = "Pond C", catchesCount = 1),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
        userDatastore = mockk()
        offlineRepository = mockk()
        getUserCatchesUseCase = mockk()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UserViewModel {
        return UserViewModel(userRepository, userDatastore, offlineRepository, getUserCatchesUseCase)
    }

    @Test
    fun initLoadsUserFromDatastore() {
        every { userDatastore.getUser } returns flowOf(testUser)
        coEvery { getUserCatchesUseCase() } returns flowOf(catches)
        every { offlineRepository.getAllUserMarkersList() } returns flowOf(places)

        val viewModel = createViewModel()

        assertEquals("John", viewModel.currentUser.value.displayName)
        assertEquals("user-1", viewModel.currentUser.value.uid)
    }

    @Test
    fun initLoadsCatchesAndFindsBestCatch() {
        every { userDatastore.getUser } returns flowOf(testUser)
        coEvery { getUserCatchesUseCase() } returns flowOf(catches)
        every { offlineRepository.getAllUserMarkersList() } returns flowOf(places)

        val viewModel = createViewModel()

        val loadedCatches = viewModel.currentCatches.value
        assertNotNull(loadedCatches)
        assertEquals(3, loadedCatches.size)

        val best = viewModel.bestCatch.value
        assertNotNull(best)
        assertEquals("c2", best.id)
        assertEquals(5.0, best.fishWeight)
    }

    @Test
    fun initLoadsPlacesAndFindsFavoritePlace() {
        every { userDatastore.getUser } returns flowOf(testUser)
        coEvery { getUserCatchesUseCase() } returns flowOf(catches)
        every { offlineRepository.getAllUserMarkersList() } returns flowOf(places)

        val viewModel = createViewModel()

        val loadedPlaces = viewModel.currentPlaces.value
        assertNotNull(loadedPlaces)
        assertEquals(3, loadedPlaces.size)

        val favorite = viewModel.favoritePlace.value
        assertNotNull(favorite)
        assertEquals("m2", favorite.id)
        assertEquals(7, favorite.catchesCount)
    }

    @Test
    fun emptyCatchesResultsInNullBestCatch() {
        every { userDatastore.getUser } returns flowOf(testUser)
        coEvery { getUserCatchesUseCase() } returns flowOf(emptyList())
        every { offlineRepository.getAllUserMarkersList() } returns flowOf(places)

        val viewModel = createViewModel()

        val loadedCatches = viewModel.currentCatches.value
        assertNotNull(loadedCatches)
        assertTrue(loadedCatches.isEmpty())
        assertNull(viewModel.bestCatch.value)
    }

    @Test
    fun emptyPlacesResultsInNullFavoritePlace() {
        every { userDatastore.getUser } returns flowOf(testUser)
        coEvery { getUserCatchesUseCase() } returns flowOf(catches)
        every { offlineRepository.getAllUserMarkersList() } returns flowOf(emptyList<UserMapMarker>())

        val viewModel = createViewModel()

        val loadedPlaces = viewModel.currentPlaces.value
        assertNotNull(loadedPlaces)
        assertTrue(loadedPlaces.isEmpty())
        assertNull(viewModel.favoritePlace.value)
    }

    @Test
    fun logoutDelegatesToUserRepository() {
        every { userDatastore.getUser } returns flowOf(testUser)
        coEvery { getUserCatchesUseCase() } returns flowOf(catches)
        every { offlineRepository.getAllUserMarkersList() } returns flowOf(places)
        val viewModel = createViewModel()

        kotlinx.coroutines.test.runTest {
            viewModel.logoutCurrentUser()
        }

        coVerify { userRepository.logoutCurrentUser() }
    }
}
