package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.RestoreRepository
import com.mobileprism.fishing.domain.repository.app.FishRepository
import com.mobileprism.fishing.model.datasource.AuthRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.FishRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.RestoreRepositoryImpl
import org.koin.dsl.module

val fishingRepository = module {
    single<AuthRepository> {
        AuthRepositoryRetrofitImpl(
            firebaseAnalytics = get(),
            authApiService = get(),
        )
    }
    single<RestoreRepository> {
        RestoreRepositoryImpl(
            firebaseAnalytics = get(),
            restoreApiService = get(),
        )
    }

    single<FishRepository> {
        FishRepositoryRetrofitImpl(
            firebaseAnalytics = get(),
            fishApiService = get()
        )
    }
}