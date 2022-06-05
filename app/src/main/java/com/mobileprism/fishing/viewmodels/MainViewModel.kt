package com.mobileprism.fishing.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.di.repositoryModuleFirebase
import com.mobileprism.fishing.di.repositoryModuleLocal
import com.mobileprism.fishing.domain.entity.common.LoginType
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules

class MainViewModel(
    private val repository: FirebaseUserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val isUserLoggedState = mutableStateOf<Boolean>(false)
    val appTheme = mutableStateOf<AppThemeValues?>(null)

    val mutableStateFlow: MutableStateFlow<BaseViewState<User?>> =
        MutableStateFlow(BaseViewState.Loading(null))

    init {
        loadCurrentUser()
        loadCurrentTheme()
    }

    private fun loadCurrentTheme() {
        viewModelScope.launch {
            userPreferences.appTheme.collect {
                appTheme.value = it
            }
        }
    }


    private fun loadCurrentUser() {
        viewModelScope.launch {
            mutableStateFlow.value = BaseViewState.Loading()
            val userFromDatastore = repository.datastoreNullableUser.collect { user ->
                when (user?.loginType) {
                    LoginType.LOCAL -> {
                        loadKoinModules(repositoryModuleLocal)
                        isUserLoggedState.value = true
                        mutableStateFlow.value = BaseViewState.Success(user)
                    }
                    LoginType.GOOGLE -> {
                        repository.currentUser
                            .catch { error -> handleError(error) }
                            .collectLatest { user ->
                                user?.let {
                                    loadKoinModules(repositoryModuleFirebase)
                                    onSuccess(user)
                                }
                            }
                    }
                    null -> {
                        mutableStateFlow.value = BaseViewState.Success(null)
                        isUserLoggedState.value = false
                    }
                }
            }
        }
    }

    private fun onSuccess(user: User) {
        viewModelScope.launch {
            repository.setUserListener(user)
            isUserLoggedState.value = true
            mutableStateFlow.value = BaseViewState.Success(user)
        }
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = BaseViewState.Error(error)
    }
}