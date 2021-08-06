package com.joesemper.fishing.di

import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.data.auth.FirebaseAuthManagerImpl
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.MainActivity
import com.joesemper.fishing.SplashActivity
import com.joesemper.fishing.data.datasource.*
import com.joesemper.fishing.data.repository.*
import com.joesemper.fishing.fragments.*
import com.joesemper.fishing.viewmodels.*
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

val markerFragment = module {
    scope(named<MarkerDetailsDialogFragment>()) {
        viewModel { MarkerDetailsViewModel(get()) }
        scoped<MarkerRepository> { MarkerRepositoryImpl(get()) }
    }
}

val userFragment = module {
    scope(named<UserFragment>()) {
        viewModel { UserViewModel(get()) }
        scoped<UserRepository> { UserRepositoryImpl(get()) }
    }
}

val newCatchFragment = module {
    scope(named<NewCatchFragment>()) {
        viewModel { NewCatchViewModel(get()) }
        scoped<NewCatchRepository> { NewCatchRepositoryImpl(get()) }
    }
}

val userCatchFragment = module {
    scope(named<UserCatchFragment>()) {
        viewModel { UserCatchViewModel(get()) }
        scoped<UserCatchRepository> { UserCatchRepositoryImpl(get()) }
    }
}

val catchesInnerFragment = module {
    scope(named<UserCatchesInnerFragment>()) {
        viewModel { UserCatchesViewModel(get()) }
        scoped<CatchesRepository> { CatchesRepositoryImpl(get()) }
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
        scoped<WeatherProvider> { WeatherRetrofitImplementation() }
        scoped<WeatherRepository> { WeatherRepositoryImpl(get(), get()) }
    }

}


