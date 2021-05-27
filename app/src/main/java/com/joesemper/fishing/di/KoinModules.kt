package com.joesemper.fishing.di

import com.joesemper.fishing.model.weather.datasource.WeatherDataSource
import com.joesemper.fishing.model.weather.datasource.WeatherRetrofitImplementation
import com.joesemper.fishing.model.weather.entity.WeatherForecast
import com.joesemper.fishing.model.weather.entity.WeatherState
import com.joesemper.fishing.view.weather.WeatherFragment
import com.joesemper.fishing.viewmodel.weather.IWeatherInteractor
import com.joesemper.fishing.viewmodel.weather.WeatherInteractor
import com.joesemper.fishing.viewmodel.weather.WeatherViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val weatherScreen = module {
    scope(named<WeatherFragment>()) {
        viewModel { WeatherViewModel(get()) }
        scoped<IWeatherInteractor<WeatherState>> { WeatherInteractor(get()) }
        scoped<WeatherDataSource<WeatherForecast>> { WeatherRetrofitImplementation() }
    }

}


