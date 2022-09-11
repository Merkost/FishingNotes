package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.repository.FirebaseUserRepository
import com.mobileprism.fishing.model.auth.AuthManagerImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseUserRepositoryImpl
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val loginModule = module {
    single { RepositoryCollections() }

    single<FirebaseUserRepository> {
        FirebaseUserRepositoryImpl(
            userDatastore = get(),
            dbCollections = get(),
            firebaseAnalytics = get(),
            context = androidContext()
        )
    }

    single<AuthManager> {
        AuthManagerImpl(
            userDatastore = get(),
            authRepository = get(),
            firebaseUserRepository = get(),
            tokenStore = get()
        )
    }
}