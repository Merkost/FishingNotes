package com.mobileprism.fishing.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication.Companion.init

class MainViewModel(
    private val authManager: AuthManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val isUserLoggedState = authManager.currentUser.map { it?.loginType != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val appTheme = userPreferences.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val googleLoginEvent = MutableSharedFlow<Unit>()

    val mutableStateFlow: MutableStateFlow<BaseViewState<User?>> =
        MutableStateFlow(BaseViewState.Loading)

    init {
        //loadCurrentUser()
        //loadCurrentTheme()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {

            /*authManager.currentUser.collectLatest {
                when (it?.loginType) {
                    null -> {
                        isUserLoggedState.value = false
                    }
                    else -> {
                        isUserLoggedState.value = true
                    }

                    *//*is LoginState.LoginFailure -> {
                        isUserLoggedState.value = false
                        handleError(it.throwable)
                    }

                    is LoginState.NotLoggedIn -> {
                        isUserLoggedState.value = false
                    }

                    is LoginState.GoogleAuthInProcess -> {
                        googleLoginEvent.emit(Unit)
                    }*//*
                }
            }*/

//            subscribeOnCurrentUser().collectLatest { currentUser ->
//                isUserLoggedState.value = currentUser != null
//            }

//            val userFromDatastore = repository.datastoreNullableUser.collect { user ->
//                when (user?.loginType) {
//                    LoginType.LOCAL -> {
//                        loadKoinModules(repositoryModuleLocal)
//                        isUserLoggedState.value = true
//                        mutableStateFlow.value = BaseViewState.Success(user)
//                    }
//                    LoginType.GOOGLE -> {
//                        repository.currentUser
//                            .catch { error -> handleError(error) }
//                            .collectLatest { user ->
//                                user?.let {
//                                    loadKoinModules(repositoryModuleFirebase)
//                                    onSuccess(user)
//                                }
//                            }
//                    }
//                    null -> {
//                        mutableStateFlow.value = BaseViewState.Success(null)
//                        isUserLoggedState.value = false
//                    }
//                }
//            }
        }
    }

    private fun onSuccess(user: User) {
        viewModelScope.launch {
//            repository.setUserListener(user)
//            isUserLoggedState.value = true
//            mutableStateFlow.value = BaseViewState.Success(user)
        }
    }
}