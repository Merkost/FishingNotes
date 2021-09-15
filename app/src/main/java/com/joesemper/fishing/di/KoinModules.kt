package com.joesemper.fishing.di

import com.joesemper.fishing.domain.*
import com.joesemper.fishing.model.auth.AuthManager
import com.joesemper.fishing.model.auth.FirebaseAuthManagerImpl
import com.joesemper.fishing.model.datasource.*
import com.joesemper.fishing.model.repository.*
import com.joesemper.fishing.ui.LoginActivity
import com.joesemper.fishing.ui.MainActivity
import com.joesemper.fishing.ui.SplashActivity
import com.joesemper.fishing.ui.fragments.*
import com.joesemper.fishing.utils.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {
    single<DatabaseProvider> { CloudFireStoreDatabaseImpl(get()) }
    single<AuthManager> { FirebaseAuthManagerImpl(androidContext()) }
    single<PhotoStorage> { CloudPhotoStorageImpl() }
    single<UserContentRepository> { UserContentRepositoryImpl(get()) }
    single { Logger() }

}

val mainActivity = module {
    scope(named<MainActivity>()) {
        viewModel { MainViewModel() }
    }
}

val splashScreen = module {
    scope(named<SplashActivity>()) {
        viewModel { SplashViewModel(get()) }
    }
}

val loginScreen = module {
    scope(named<LoginActivity>()) {
        viewModel { LoginViewModel(get(), get()) }
        scoped<UserRepository> { UserRepositoryImpl(get(), get()) }
    }
}

val mapScreen = module {
    scope(named<MapFragment>()) {
        viewModel { MapViewModel(get()) }
    }
}

val markerFragment = module {
    scope(named<MarkerDetailsDialogFragment>()) {
        viewModel { MarkerDetailsViewModel(get()) }
    }
}

val userFragment = module {
    scope(named<UserFragment>()) {
        viewModel { UserViewModel(get(), get()) }
        scoped<UserRepository> { UserRepositoryImpl(get(), get()) }
    }
}

val newCatchFragment = module {
    scope(named<NewCatchFragment>()) {
        viewModel { NewCatchViewModel(get()) }
    }
}

val userCatchFragment = module {
    scope(named<UserCatchFragment>()) {
        viewModel { UserCatchViewModel(get(),get()) }
        scoped<UserRepository> { UserRepositoryImpl(get(), get()) }
    }
}

val catchesInnerFragment = module {
    scope(named<UserCatchesInnerFragment>()) {
        viewModel { UserPlaceCatchesViewModel(get()) }
    }
}

val weatherScreen = module {
    scope(named<WeatherFragment>()) {
        viewModel { WeatherViewModel(get()) }
        scoped<WeatherProvider> { WeatherRetrofitImplementation() }
        scoped<WeatherRepository> { WeatherRepositoryImpl(get(), get()) }
    }
}

val notesFragment = module {
    scope(named<NotesFragment>()) {
        viewModel { NotesViewModel(get()) }
    }
}
val userPlaceFragment = module {
    scope(named<UserPlaceFragment>()) {
        viewModel { UserPlaceViewModel(get()) }
    }
}
val catchesFragment = module {
        viewModel { UserCatchesViewModel(get()) }
}

val placesFragment = module {
    viewModel { UserPlacesViewModel(get()) }
}
