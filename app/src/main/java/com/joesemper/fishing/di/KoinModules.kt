package com.joesemper.fishing.di

import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.data.auth.FirebaseAuthManagerImpl
import com.joesemper.fishing.data.datasource.CloudFireStoreDatabaseImpl
import com.joesemper.fishing.data.datasource.DatabaseProvider
import com.joesemper.fishing.data.datasource.CloudPhotoStorageImpl
import com.joesemper.fishing.data.datasource.PhotoStorage
import com.joesemper.fishing.data.repository.GroupsRepository
import com.joesemper.fishing.data.repository.GroupsRepositoryImpl
import com.joesemper.fishing.data.repository.MapRepository
import com.joesemper.fishing.data.repository.MapRepositoryImpl
import com.joesemper.fishing.data.repository.CatchesRepository
import com.joesemper.fishing.data.repository.CatchesRepositoryImpl
import com.joesemper.fishing.data.repository.NewCatchRepository
import com.joesemper.fishing.data.repository.NewCatchRepositoryImpl
import com.joesemper.fishing.data.repository.MarkerRepository
import com.joesemper.fishing.data.repository.MarkerRepositoryImpl
import com.joesemper.fishing.data.repository.WeatherRepository
import com.joesemper.fishing.data.repository.WeatherRetrofitImplementation
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.MainActivity
import com.joesemper.fishing.SplashActivity
import com.joesemper.fishing.fragments.GroupsFragment
import com.joesemper.fishing.fragments.MapFragment
import com.joesemper.fishing.fragments.WeatherFragment
import com.joesemper.fishing.viewmodels.GroupsViewModel
import com.joesemper.fishing.viewmodels.MainViewModel
import com.joesemper.fishing.viewmodels.MapViewModel
import com.joesemper.fishing.fragments.NewCatchFragment
import com.joesemper.fishing.viewmodels.NewCatchViewModel
import com.joesemper.fishing.viewmodels.UserCatchesViewModel
import com.joesemper.fishing.fragments.MarkerDetailsDialogFragment
import com.joesemper.fishing.viewmodels.MarkerDetailsViewModel
import com.joesemper.fishing.fragments.UserCatchesInnerFragment
import com.joesemper.fishing.viewmodels.SplashViewModel
import com.joesemper.fishing.viewmodels.WeatherViewModel
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

val newCatchFragment = module {
    scope(named<NewCatchFragment>()) {
        viewModel { NewCatchViewModel(get()) }
        scoped<NewCatchRepository> { NewCatchRepositoryImpl(get()) }
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
        scoped<WeatherRepository> { WeatherRetrofitImplementation() }
    }

}


