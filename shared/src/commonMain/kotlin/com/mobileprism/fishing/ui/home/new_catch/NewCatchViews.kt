package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.mobileprism.fishing.utils.ValidationUtils
import com.mobileprism.fishing.utils.roundTo

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

@Composable
fun FishAmountAndWeightView(
    modifier: Modifier = Modifier,
    amountState: MutableState<String>,
    weightState: MutableState<String>
) {
    val currentAmount = amountState.value.toIntOrNull()
    val currentWeight = weightState.value.toDoubleOrNull()
    val isAmountError = currentAmount != null && !ValidationUtils.isAmountValid(currentAmount)
    val isWeightError = currentWeight != null && !ValidationUtils.isWeightValid(currentWeight)

    Row(modifier = modifier) {
        Column(Modifier.weight(1F)) {
            OutlinedTextField(
                value = amountState.value,
                onValueChange = {
                    if (it.isEmpty()) amountState.value = it
                    else {
                        val parsed = it.toIntOrNull()
                        when {
                            parsed == null -> {} // keep old value
                            parsed > ValidationUtils.MAX_FISH_AMOUNT -> amountState.value = ValidationUtils.MAX_FISH_AMOUNT.toString()
                            else -> amountState.value = it
                        }
                    }
                },
                isError = amountState.value.isEmpty() || isAmountError,
                supportingText = if (isAmountError) {
                    { Text(stringResource(Res.string.amount_out_of_range)) }
                } else null,
                label = { Text(text = stringResource(Res.string.amount)) },
                trailingIcon = { Text(stringResource(Res.string.pc)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        val current = amountState.value.toIntOrNull() ?: 0
                        if (current >= 1)
                            amountState.value = (current - 1).toString()
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(Res.string.decrease)
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        val current = amountState.value.toIntOrNull() ?: 0
                        if (current < ValidationUtils.MAX_FISH_AMOUNT)
                            amountState.value = (current + 1).toString()
                    },
                    enabled = (amountState.value.toIntOrNull() ?: 0) < ValidationUtils.MAX_FISH_AMOUNT,
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(Res.string.increase)
                    )
                }
            }

        }
        Spacer(modifier = Modifier.size(6.dp))
        Column(Modifier.weight(1F)) {
            OutlinedTextField(
                value = weightState.value,
                onValueChange = {
                    if (it.isEmpty()) weightState.value = it
                    else {
                        val parsed = it.toDoubleOrNull()
                        when {
                            parsed == null -> {} // keep old value
                            parsed > ValidationUtils.MAX_FISH_WEIGHT_KG -> weightState.value = ValidationUtils.MAX_FISH_WEIGHT_KG.toInt().toString()
                            else -> weightState.value = it
                        }
                    }
                },
                isError = isWeightError,
                supportingText = if (isWeightError) {
                    { Text(stringResource(Res.string.weight_out_of_range)) }
                } else null,
                label = { Text(text = stringResource(Res.string.weight)) },
                trailingIcon = {
                    Text(stringResource(Res.string.kg))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        val current = weightState.value.toDoubleOrNull() ?: 0.0
                        if (current >= 0.1)
                            weightState.value = (current - 0.1).roundTo(1).toString()
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(Res.string.decrease)
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        val current = weightState.value.toDoubleOrNull() ?: 0.0
                        if (current < ValidationUtils.MAX_FISH_WEIGHT_KG)
                            weightState.value = (current + 0.1).roundTo(1).toString()
                    },
                    enabled = (weightState.value.toDoubleOrNull() ?: 0.0) < ValidationUtils.MAX_FISH_WEIGHT_KG,
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(Res.string.increase)
                    )
                }
            }
        }
    }
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

