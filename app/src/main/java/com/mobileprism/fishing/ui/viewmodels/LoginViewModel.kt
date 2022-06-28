package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.di.repositoryModuleFirebase
import com.mobileprism.fishing.di.repositoryModuleLocal
import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.domain.entity.common.LoginType
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.getUUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules

class LoginViewModel(
    private val firebaseRepository: FirebaseUserRepository,
    private val userDatastore: UserDatastore
) : ViewModel() {

    private val _uiState: MutableStateFlow<BaseViewState<User?>> =
        MutableStateFlow(BaseViewState.Success<User?>(null))
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun registerNewUser(loginPassword: LoginPassword) {
        _uiState.value = BaseViewState.Loading(null)
    }

    fun loginUser(loginPassword: LoginPassword) {
        _uiState.value = BaseViewState.Loading(null)
    }

    fun skipAuthorization() {
//        viewModelScope.launch {
//            repository.addOfflineUser()
//        }
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
