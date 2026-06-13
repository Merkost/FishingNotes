package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobileprism.fishing.ui.utils.AnimatedResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.new_catch.weather.SelectedWeather
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.WindIconItem
import com.mobileprism.fishing.utils.Constants.WIND_ROTATION

@ExperimentalComposeUiApi
@Composable
fun PickWeatherIconDialog(onWeatherSelected: (SelectedWeather) -> Unit, onDismiss: () -> Unit) {
    DefaultDialog(
        stringResource(Res.string.choose_weather),
        content = {
            WeatherTypesSheet() { onWeatherSelected(it) }
        },
        onDismiss = onDismiss
    )
}


@ExperimentalComposeUiApi
@Composable
fun PickWindDirDialog(onDirectionSelected: (Float) -> Unit, onDismiss: () -> Unit) {
    DefaultDialog(
        primaryText = stringResource(Res.string.choose_wind_direction),
        content = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center
            ) {
                (0..7).forEach {
                    WindIconItem(
                        rotation = it * WIND_ROTATION,
                        onIconSelected = { onDirectionSelected(it * WIND_ROTATION) }
                    )
                }
            }
        }, onDismiss = onDismiss
    )
}

@Composable
fun NewCatchNoPlaceDialog(
    navController: NavController
) {
    DefaultDialog(
        primaryText = stringResource(Res.string.no_places_added),
        secondaryText = stringResource(Res.string.add_location_dialog),
        negativeButtonText = stringResource(Res.string.cancel),
        onNegativeClick = { navController.popBackStack() },
        positiveButtonText = stringResource(Res.string.add),
        onPositiveClick = { onAddNewPlaceClick(navController) },
        onDismiss = { navController.popBackStack() },
        content = {
            LottieNoPlaces(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    )
}

@Composable
fun LottieNoPlaces(modifier: Modifier) {
    AnimatedResource("no_loaction", modifier)
}

private fun onAddNewPlaceClick(navController: NavController) {
    navController.navigate(MainDestinations.Map(isAddingNewPlace = true))
}

@Composable
fun WayOfFishingView(
    modifier: Modifier = Modifier,
    rodState: String,
    biteState: String,
    lureState: String,
    onRodChange: (String) -> Unit,
    onBiteChange: (String) -> Unit,
    onLureChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = rodState,
            onValueChange = { onRodChange(it) },
            label = { Text(text = stringResource(Res.string.fish_rod)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = biteState,
            onValueChange = { onBiteChange(it) },
            label = { Text(text = stringResource(Res.string.bait)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = lureState,
            onValueChange = { onLureChange(it) },
            label = { Text(text = stringResource(Res.string.lure)) }
        )
    }
}

