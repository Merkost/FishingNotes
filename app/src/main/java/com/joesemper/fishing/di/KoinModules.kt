package com.joesemper.fishing.di

import com.joesemper.fishing.model.db.datasource.CloudFireStoreDatabaseImpl
import com.joesemper.fishing.model.db.datasource.DatabaseProvider
import com.joesemper.fishing.model.splash.datasource.UsersRepository
import com.joesemper.fishing.model.splash.datasource.UsersRepositoryImpl
import com.joesemper.fishing.model.weather.datasource.WeatherRepository
import com.joesemper.fishing.model.weather.datasource.WeatherRetrofitImplementation
import com.joesemper.fishing.view.splash.SplashActivity
import com.joesemper.fishing.view.weather.WeatherFragment
import com.joesemper.fishing.viewmodel.splash.SplashViewModel
import com.joesemper.fishing.viewmodel.weather.WeatherViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {
    single<DatabaseProvider> { CloudFireStoreDatabaseImpl() }
}

val splashScreen = module {
    scope(named<SplashActivity>()) {
        viewModel { SplashViewModel(get()) }
        scoped<UsersRepository> { UsersRepositoryImpl(get()) }
    }
}

val weatherScreen = module {
    scope(named<WeatherFragment>()) {
        viewModel { WeatherViewModel(get()) }
        scoped<WeatherRepository> { WeatherRetrofitImplementation() }
    }

}


