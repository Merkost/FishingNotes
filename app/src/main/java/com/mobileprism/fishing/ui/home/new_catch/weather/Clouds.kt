package com.mobileprism.fishing.ui.home.new_catch.weather

import com.mobileprism.fishing.R

enum class Clouds(val iconRes: Int) {
    FewClouds(R.drawable.weather_partly_cloudy), //11-25%
    ScatteredClouds(R.drawable.ic_weather_broken_clouds), //25-50%
    BrokenClouds(R.drawable.ic_weather_broken_clouds), //51-84%
    OvercastClouds(R.drawable.weather_cloudy), //85-100%
}