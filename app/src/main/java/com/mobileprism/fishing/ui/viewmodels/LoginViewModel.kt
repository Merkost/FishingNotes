package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState: MutableStateFlow<BaseViewState<User?>> =
        MutableStateFlow(BaseViewState.Success<User?>(null))
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun registerNewUser(loginPassword: LoginPassword) {
        _uiState.value = BaseViewState.Loading(null)
    }

    fun skipAuthorization() {
        viewModelScope.launch {
            repository.addOfflineUser()
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            firebaseRepository.currentUser
                .catch { error -> handleError(error) }
                .collectLatest { user -> user?.let { onSuccess(it) } }
        }
    }

    private fun onSuccess(user: User) {
        viewModelScope.launch {
            loadKoinModules(repositoryModuleFirebase)
            firebaseRepository.addNewUser(user).collect { progress ->
                when (progress) {
                    is Progress.Complete -> {
                        userDatastore.saveUser(user)
                        _uiState.value = BaseViewState.Success(user)
                    }
                    is Progress.Loading -> {
                        _uiState.value = BaseViewState.Loading(null)
                    }
                    is Progress.Error -> {
                        _uiState.value = BaseViewState.Error(progress.error)
                    }
                }
            }
        }
    }

    private fun handleError(error: Throwable) {
        _uiState.value = BaseViewState.Error(error)
    }

    fun continueWithoutLogin() {
        val user = createNewLocalUser()
        loadKoinModules(repositoryModuleLocal)
        viewModelScope.launch {
            userDatastore.saveUser(user)
            _uiState.value = BaseViewState.Success(user)
        }
    }

    fun continueWithGoogle() {
        loadCurrentUser()
    }

    private fun createNewLocalUser(): User {
        return User(uid = getUUID(), loginType = LoginType.LOCAL)
    }

}
