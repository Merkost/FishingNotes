package com.joesemper.fishing.model.repository.app

import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.entity.solunar.Solunar
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface SolunarRepository {
    fun getSolunar(latitude: Double, longitude: Double): Flow<RetrofitWrapper<Solunar>>
}