package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.notes.user_catches.UserCatchesScreen
import com.joesemper.fishing.compose.ui.home.notes.user_places.UserPlacesScreen
import com.joesemper.fishing.compose.ui.home.weather.WeatherForADay
import com.joesemper.fishing.compose.ui.home.weather.WeatherForAWeek
import com.joesemper.fishing.model.entity.weather.WeatherForecast

typealias ComposableFun = @Composable (navController: NavController) -> Unit

sealed class TabItem(var icon: Int, var title: String, var screen: ComposableFun) {

    @ExperimentalAnimationApi
    object Places : TabItem(R.drawable.ic_baseline_location_on_24, "Places", { navController ->
        UserPlacesScreen(navController = navController)
    })

    @ExperimentalAnimationApi
    object Catches : TabItem(R.drawable.ic_add_catch, "Catches", { navController ->
        UserCatchesScreen(navController = navController)
    })

    class ForADay(weatherForecast: WeatherForecast) :
        TabItem(R.drawable.ic_baseline_today_24, "For a day", { navController ->
            WeatherForADay(weather = weatherForecast)
        })

    class ForAWeek(weatherForecast: WeatherForecast) :
        TabItem(R.drawable.calendar_week, "For a week", { navController ->
            WeatherForAWeek(weather = weatherForecast)
        })
}
