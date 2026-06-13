package com.mobileprism.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userDatastore: UserDatastore,
    private val userRepository: UserRepository,
    private val savePhotos: SavePhotosUseCase,
) : ViewModel() {

    private val _bdUser = MutableStateFlow(User())

    private val _currentUser = MutableStateFlow(User())
    val currentUser = _currentUser.asStateFlow()

    private val _pendingPhotoPath = MutableStateFlow<String?>(null)
    val pendingPhotoPath = _pendingPhotoPath.asStateFlow()

    private val _isChanged = MutableStateFlow(false)
    val isChanged = _isChanged.asStateFlow()

    init {
        loadCurrentUser()
        setChangedListener()
    }

    private val _uiState = MutableStateFlow<BaseViewState<Unit>?>(null)
    val uiState = _uiState.asStateFlow()

    fun resetChanges() {
        _pendingPhotoPath.value = null
        loadCurrentUser()
    }

    fun onNameChange(name: String) {
        _currentUser.value = _currentUser.value.copy(displayName = name)
    }

    fun onLoginChange(login: String) {
        _currentUser.value = _currentUser.value.copy(login = login)
    }

    fun birthdaySelected(birthday: Long) {
        _currentUser.value = _currentUser.value.copy(birthDate = birthday)
    }

    fun onPhotoPicked(localPath: String) {
        _pendingPhotoPath.value = localPath
        _isChanged.value = true
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = userDatastore.getUser.first()
            _bdUser.value = user
            _currentUser.value = user
        }
    }

    private fun setChangedListener() {
        viewModelScope.launch {
            currentUser.collect {
                _isChanged.value =
                    (it.displayName != _bdUser.value.displayName
                            || it.login != _bdUser.value.login
                            || it.email != _bdUser.value.email
                            || it.birthDate != _bdUser.value.birthDate
                            || _pendingPhotoPath.value != null)
            }
        }
    }

    fun updateProfile() {
        _uiState.value = BaseViewState.Loading()
        viewModelScope.launch {
            try {
                val pending = _pendingPhotoPath.value
                val userToSave = if (pending != null) {
                    val uploaded = savePhotos(listOf(pending)).firstOrNull()
                    if (uploaded != null) {
                        _currentUser.value.copy(photoUrl = uploaded)
                    } else {
                        _currentUser.value
                    }
                } else {
                    _currentUser.value
                }
                userRepository.setNewProfileData(userToSave).fold(
                    onSuccess = {
                        _pendingPhotoPath.value = null
                        _uiState.value = BaseViewState.Success(Unit)
                    },
                    onFailure = {
                        _uiState.value = BaseViewState.Error(it)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BaseViewState.Error(e)
            }
        }
    }
}
