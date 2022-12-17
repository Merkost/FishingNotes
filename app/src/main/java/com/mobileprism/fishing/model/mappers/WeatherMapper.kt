package com.mobileprism.fishing.model.mappers

import com.mobileprism.fishing.domain.entity.weather.*
import com.mobileprism.fishing.model.entity.weather.Forecastday
import com.mobileprism.fishing.model.entity.weather.Hour
import com.mobileprism.fishing.model.entity.weather.WeatherApiForecast
import com.mobileprism.fishing.model.entity.FishingWeather

fun convertWeatherApiForecast(forecast: WeatherApiForecast): WeatherForecast {
    return WeatherForecast(
        latitude = forecast.location.lat.toString(),
        longitude = forecast.location.lon.toString(),
        timezoneOffset = 0,
        hourly = mapHourly(forecast.forecast.forecastday.first().hour),
        daily = mapDaily(forecast.forecast.forecastday),
        current = mapCurrent(forecast.forecast.forecastday.first())
    )
}

private fun mapHourly(hourly: List<Hour>): List<Hourly> {
    return hourly.map {
        Hourly(
            date = it.time_epoch.toLong(),
            temperature = it.temp_c.toFloat(),
            pressure = it.pressure_mb.toInt(),
            humidity = it.humidity,
            clouds = it.cloud,
            windSpeed = it.wind_mph.toFloat(),
            windDeg = it.wind_degree,
            weather = listOf(
                Weather(
                    description = it.condition.text
                )
            ),
            probabilityOfPrecipitation = maxOf(
                it.chance_of_rain.toFloat(),
                it.chance_of_snow.toFloat()
            )
        )
    }
}

private fun mapDaily(daily: List<Forecastday>): List<Daily> {
    return daily.map {
        Daily(
            date = it.date_epoch.toLong(),
            sunrise = 0,
            sunset = 0,
            moonPhase = it.astro.moon_illumination.toFloat() / 100,
            pressure = it.hour[it.hour.size / 2].pressure_mb.toInt(),
            humidity = it.day.avghumidity.toInt(),
            windSpeed = it.day.maxwind_mph.toFloat(),
            windDeg = it.hour[it.hour.size / 2].wind_degree,
            weather = listOf(
                Weather(
                    description = it.day.condition.text
                )
            ),
            temperature = Temperature(
                day = it.day.avgtemp_c.toFloat(),
                min = it.day.mintemp_c.toFloat(),
                max = it.day.maxtemp_c.toFloat(),
                night = it.hour[it.hour.size / 4].temp_c.toFloat(),
                evening = it.hour[(it.hour.size / 4) * 3].temp_c.toFloat(),
                morning = it.hour[it.hour.size / 3].temp_c.toFloat()
            ),
            probabilityOfPrecipitation = maxOf(
                it.hour[it.hour.size / 2].chance_of_rain.toFloat(),
                it.hour[it.hour.size / 2].chance_of_snow.toFloat()
            ),
            clouds = it.hour[it.hour.size / 2].cloud
        )
    }
}

private fun mapCurrent(current: Forecastday): Current {
    return Current(
        date = current.date_epoch.toLong(),
        sunrise = 0,
        sunset = 0,
        temperature = current.day.maxtemp_c.toFloat(),
        pressure = current.hour.first().pressure_mb.toInt(),
        humidity = current.hour.first().humidity,
        windSpeed = current.hour.first().wind_mph.toFloat(),
        windDeg = current.hour.first().wind_degree,
        weather = listOf(
            Weather(
                description = current.day.condition.text
            )
        )
    )
}