package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.app.FishRepository
import com.mobileprism.fishing.model.datasource.fake.FakeAuthRepositoryRetrofitImpl
import com.mobileprism.fishing.model.datasource.fake.FakeFishRepositoryRetrofitImpl
import org.koin.dsl.module

val fishingFakeRepositoryModule = module {
    single<AuthRepository> {
        FakeAuthRepositoryRetrofitImpl()
    }

    single<FishRepository> {
        FakeFishRepositoryRetrofitImpl()
    }
}
