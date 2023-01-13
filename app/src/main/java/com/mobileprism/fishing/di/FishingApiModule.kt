package com.mobileprism.fishing.di

import com.mobileprism.fishing.model.api.AuthApiService
import com.mobileprism.fishing.model.api.FishApiService
import com.mobileprism.fishing.model.api.RestoreApiService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val fishingApiModule = module {

    single<AuthApiService> { createAuthApiService(get(named("fishing_retrofit"))) }
    single<RestoreApiService> { createRestoreApiService(get(named("fishing_retrofit"))) }
    single<FishApiService> { createFishApiService(get(named("fishing_retrofit"))) }
}

private fun createAuthApiService(retrofit: Retrofit): AuthApiService {
    return retrofit.create(AuthApiService::class.java)
}

private fun createRestoreApiService(retrofit: Retrofit): RestoreApiService {
    return retrofit.create(RestoreApiService::class.java)
}

private fun createFishApiService(retrofit: Retrofit): FishApiService {
    return retrofit.create(FishApiService::class.java)
}