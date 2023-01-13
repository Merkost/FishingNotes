package com.mobileprism.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userDatastore: UserDatastore,
    private val firebaseUserRepository: FirebaseUserRepository,
) : ViewModel() {

    private val _bdUser = MutableStateFlow(UserData())

    private val _currentUser = MutableStateFlow(UserData())
    val currentUser = _currentUser.asStateFlow()

    private val _isChanged = MutableStateFlow(false)
    val isChanged = _isChanged.asStateFlow()

    init {
        loadCurrentUser()
        //setChangedListener()
    }

    private val _uiState = MutableStateFlow<BaseViewState<Unit>?>(null)
    val uiState = _uiState.asStateFlow()

    fun resetChanges() {
        loadCurrentUser()
    }

    /*fun onNameChange(name: String) {
        _currentUser.value = _currentUser.value.copy(displayName = name)
    }*/

    fun onLoginChange(login: String) {
        _currentUser.value = _currentUser.value.copy(login = login)
    }

    /*fun birthdaySelected(birthday: Long) {
        _currentUser.value = _currentUser.value.copy(birthDate = birthday)
    }*/

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = userDatastore.getUser.first()
            _bdUser.value = user
            _currentUser.value = user
        }
    }

    /*private fun setChangedListener() {
        viewModelScope.launch {
            currentUser.collect {
                *//*_isChanged.value = it != _bdUser.value*//*
                _isChanged.value =
                    (it.displayName != _bdUser.value.displayName
                            || it.login != _bdUser.value.login
                            || it.email != _bdUser.value.email
                            || it.birthDate != _bdUser.value.birthDate)
            }
        }
    }*/

    fun updateProfile() {
        _uiState.value = BaseViewState.Loading
        viewModelScope.launch {
            /*firebaseUserRepository.setNewProfileData(_currentUser.value).fold(
                onSuccess = {
                    _uiState.value = BaseViewState.Success(Unit)
                }, onFailure = {
                    _uiState.value = BaseViewState.Error(it)
                }
            )*/
        }
    }
}