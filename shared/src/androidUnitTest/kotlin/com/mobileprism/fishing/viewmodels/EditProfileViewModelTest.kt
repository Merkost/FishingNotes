package com.mobileprism.fishing.viewmodels

import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.testutils.user
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var userDatastore: UserDatastore
    private var profileResult: Result<Unit> = Result.success(Unit)
    private val savePhotos: SavePhotosUseCase = mockk(relaxed = true)

    private val storedUser = user(
        uid = "user-1",
        displayName = "John",
        login = "johnd",
        email = "john@test.com",
        birthDate = 0L,
    )

    private val userRepository = object : UserRepository {
        override val currentUser: Flow<User?> = flowOf(storedUser)
        override val datastoreUser: Flow<User> = flowOf(storedUser)
        override val isLoggedIn: Boolean = true
        override val cachedUser: User? = null
        var lastProfileData: User? = null

        override suspend fun logoutCurrentUser() {}
        override suspend fun addNewUser(user: User): Result<Unit> = error("Not used")
        override suspend fun setUserListener(user: User) {}
        override suspend fun setNewProfileData(user: User): Result<Unit> {
            lastProfileData = user
            return profileResult
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userDatastore = mockk()
        every { userDatastore.getUser } returns flowOf(storedUser)
        profileResult = Result.success(Unit)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsUserFromDatastore() {
        val viewModel = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        assertEquals("John", viewModel.currentUser.value.displayName)
        assertEquals("johnd", viewModel.currentUser.value.login)
        assertEquals("john@test.com", viewModel.currentUser.value.email)
    }

    @Test
    fun onNameChangeUpdatesCurrentUser() {
        val viewModel = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        viewModel.onNameChange("Jane")

        assertEquals("Jane", viewModel.currentUser.value.displayName)
    }

    @Test
    fun onLoginChangeUpdatesCurrentUser() {
        val viewModel = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        viewModel.onLoginChange("janed")

        assertEquals("janed", viewModel.currentUser.value.login)
    }

    @Test
    fun birthdaySelectedUpdatesCurrentUser() {
        val viewModel = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        viewModel.birthdaySelected(946684800000L)

        assertEquals(946684800000L, viewModel.currentUser.value.birthDate)
    }

    @Test
    fun isChangedDetectsModification() {
        val viewModel = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        assertFalse(viewModel.isChanged.value)

        viewModel.onNameChange("Different Name")

        assertTrue(viewModel.isChanged.value)
    }

    @Test
    fun resetChangesReloadsFromDatastore() {
        val viewModel = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        viewModel.onNameChange("Modified")
        assertEquals("Modified", viewModel.currentUser.value.displayName)
        assertTrue(viewModel.isChanged.value)

        viewModel.resetChanges()

        assertEquals("John", viewModel.currentUser.value.displayName)
        assertFalse(viewModel.isChanged.value)
    }

    @Test
    fun updateProfileSuccessSetsSuccessState() {
        profileResult = Result.success(Unit)

        val viewModel = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        viewModel.onNameChange("Updated Name")
        viewModel.updateProfile()

        val state = viewModel.uiState.value
        assertIs<BaseViewState.Success<Unit>>(state)
    }

    @Test
    fun updateProfileErrorSetsErrorState() {
        val exception = RuntimeException("Network error")
        profileResult = Result.failure(exception)

        val viewModel = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        viewModel.updateProfile()

        val state = viewModel.uiState.value
        assertIs<BaseViewState.Error>(state)
        assertEquals("Network error", state.error?.message)
    }

    @Test
    fun onPhotoPickedSetsPendingPathAndMarksChanged() {
        val vm = EditProfileViewModel(userDatastore, userRepository, savePhotos)

        vm.onPhotoPicked("/local/path/avatar.jpg")

        assertEquals("/local/path/avatar.jpg", vm.pendingPhotoPath.value)
        assertTrue(vm.isChanged.value)
    }

    @Test
    fun updateProfileUploadsPendingPhotoAndPersistsDownloadUrl() {
        coEvery { savePhotos(listOf("/local/path/avatar.jpg")) } returns listOf("https://cdn/avatar.jpg")

        val vm = EditProfileViewModel(userDatastore, userRepository, savePhotos)
        vm.onPhotoPicked("/local/path/avatar.jpg")
        vm.updateProfile()

        coVerify { savePhotos(listOf("/local/path/avatar.jpg")) }
        assertEquals("https://cdn/avatar.jpg", userRepository.lastProfileData?.photoUrl)
        assertIs<BaseViewState.Success<Unit>>(vm.uiState.value)
        assertEquals(null, vm.pendingPhotoPath.value)
    }

    @Test
    fun updateProfileWithoutPendingPhotoSkipsUpload() {
        profileResult = Result.success(Unit)
        val vm = EditProfileViewModel(userDatastore, userRepository, savePhotos)
        vm.onNameChange("Newname")
        vm.updateProfile()
        coVerify(exactly = 0) { savePhotos(any()) }
        assertIs<BaseViewState.Success<Unit>>(vm.uiState.value)
    }
}
