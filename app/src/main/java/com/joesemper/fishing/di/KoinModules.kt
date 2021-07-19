package com.joesemper.fishing.di

import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.data.auth.FirebaseAuthManagerImpl
import com.joesemper.fishing.data.datasource.CloudFireStoreDatabaseImpl
import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.datasource.CloudPhotoStorageImpl
import com.joesemper.fishing.data.datasource.PhotoStorage
import com.joesemper.fishing.data.repository.groups.GroupsRepository
import com.joesemper.fishing.data.repository.groups.GroupsRepositoryImpl
import com.joesemper.fishing.data.repository.map.MapRepository
import com.joesemper.fishing.data.repository.map.MapRepositoryImpl
import com.joesemper.fishing.data.repository.weather.WeatherRepository
import com.joesemper.fishing.data.repository.weather.WeatherRetrofitImplementation
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.presentation.MainActivity
import com.joesemper.fishing.presentation.splash.SplashActivity
import com.joesemper.fishing.presentation.groups.GroupsFragment
import com.joesemper.fishing.presentation.map.MapFragment
import com.joesemper.fishing.presentation.weather.WeatherFragment
import com.joesemper.fishing.presentation.groups.GroupsViewModel
import com.joesemper.fishing.presentation.main.MainViewModel
import com.joesemper.fishing.presentation.map.MapViewModel
import com.joesemper.fishing.presentation.splash.SplashViewModel
import com.joesemper.fishing.presentation.weather.WeatherViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {
    single<DatabaseProvider> { CloudFireStoreDatabaseImpl(get()) }
    single<AuthManager> { FirebaseAuthManagerImpl(androidContext()) }
    single<PhotoStorage> { CloudPhotoStorageImpl() }
    single { Logger() }
}

val mainActivity = module {
    scope(named<MainActivity>()) {
        viewModel { MainViewModel(get()) }
    }
}

val splashScreen = module {
    scope(named<SplashActivity>()) {
        viewModel { SplashViewModel(get()) }
    }
}

val mapScreen = module {
    scope(named<MapFragment>()) {
        viewModel { MapViewModel(get()) }
        scoped<MapRepository> { MapRepositoryImpl(get()) }
    }
}

val groupsScreen = module {
    scope(named<GroupsFragment>()) {
        viewModel { GroupsViewModel(get()) }
        scoped<GroupsRepository> { GroupsRepositoryImpl(get()) }
    }
}

val weatherScreen = module {
    scope(named<WeatherFragment>()) {
        viewModel { WeatherViewModel(get()) }
        scoped<WeatherRepository> { WeatherRetrofitImplementation() }
    }

}


