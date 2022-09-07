package com.mobileprism.fishing.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.use_cases.users.SubscribeOnLoginState
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val subscribeOnLoginState: SubscribeOnLoginState,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val isUserLoggedState = MutableStateFlow<Boolean>(false)
    val appTheme = mutableStateOf<AppThemeValues?>(null)
    val googleLoginEvent = MutableSharedFlow<Unit>()

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

            subscribeOnLoginState().collectLatest {
                when (it) {
                    is LoginState.LoggedIn -> {
                        isUserLoggedState.value = true
                    }

                    is LoginState.LoginFailure -> {
                        isUserLoggedState.value = false
                        handleError(it.throwable)
                    }

                    is LoginState.NotLoggedIn -> {
                        isUserLoggedState.value = false
                    }

                    is LoginState.GoogleAuthRequest -> {
                        googleLoginEvent.emit(Unit)
                    }
                }
            }

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

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = BaseViewState.Error(error)
    }
}