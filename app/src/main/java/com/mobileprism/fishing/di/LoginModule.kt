package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.domain.use_cases.validation.ValidationUseCase
import com.mobileprism.fishing.model.auth.AuthManagerImpl
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import com.mobileprism.fishing.ui.viewmodels.restore.ConfirmAccountViewModel
import com.mobileprism.fishing.ui.viewmodels.restore.SearchAccountViewModel
import com.mobileprism.fishing.ui.viewmodels.login.LoginViewModel
import com.mobileprism.fishing.ui.viewmodels.login.RegisterViewModel
import com.mobileprism.fishing.ui.viewmodels.restore.ResetAccountViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    single { RepositoryCollections() }

    factory<ValidationUseCase> {
        ValidationUseCase(androidContext())
    }

    /*single<FirebaseUserRepository> {
        FirebaseUserRepositoryImpl(
            userDatastore = get(),
            dbCollections = get(),
            firebaseAnalytics = get(),
            context = androidContext(),
            fireBaseAuth = get()
        )
    }*/

    single<AuthManager> {
        AuthManagerImpl(
            userDatastore = get(),
            authRepository = get(),
        )
    }

    viewModel {
        RegisterViewModel(
            authManager = get(),
            connectionManager = get(),
            validationUseCase = get()
        )
    }

    viewModel() {
        LoginViewModel(
            authManager = get(),
            validationUseCase = get()
        )
    }

    viewModel() {
        SearchAccountViewModel(
            validationUseCase = get(),
            restoreRepository = get()
        )
    }

    viewModel() {
        ConfirmAccountViewModel(
            restoreRepository = get()
        )
    }

    viewModel() {
        ResetAccountViewModel(
            userLogin = it.get(),
            validationUseCase = get(),
            restoreRepository = get()
        )
    }
}