package com.joesemper.fishing.di

import com.joesemper.fishing.domain.*
import com.joesemper.fishing.model.auth.AuthManager

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
    single<UserRepository> { FirebaseUserRepositoryImpl(androidContext()) }
    single<UserContentRepository> { CloudFireStoreDatabase(get()) }
    single<PhotoStorage> { CloudPhotoStorage() }
    single<WeatherRepository> { WeatherRepositoryRetrofitImpl() }
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
        viewModel { LoginViewModel(get()) }
    }
}

val mapScreen = module {
    scope(named<MapFragment>()) {
        viewModel { MapViewModel(get()) }
    }
}

val userFragment = module {
    viewModel { UserViewModel(get(), get()) }

}

val newCatchScreen = module {
        viewModel { NewCatchViewModel(get(), get()) }
}

val newPlaceScreen = module {
    viewModel { NewPlaceViewModel(get()) }
}

val userCatchScreen = module {
    viewModel { UserCatchViewModel(get(), get()) }
}

val weatherScreen = module {
    scope(named<WeatherFragment>()) {
        viewModel { WeatherViewModel(get(), get()) }
    }
}

//val notesFragment = module {
//    scope(named<NotesFragment>()) {
//        viewModel { NotesViewModel(get()) }
//    }
//}

val userPlaceScreen = module {
    viewModel { UserPlaceViewModel(get(), get()) }
}
val catchesFragment = module {
    viewModel { UserCatchesViewModel(get()) }
}

val placesFragment = module {
    viewModel { UserPlacesViewModel(get()) }
}
