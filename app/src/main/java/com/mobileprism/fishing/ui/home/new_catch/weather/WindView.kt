package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.custom.DefaultDialog
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField
import com.mobileprism.fishing.ui.home.views.WindIconItem
import com.mobileprism.fishing.ui.home.weather.WindSpeedValues
import com.mobileprism.fishing.ui.utils.toDoubleExOrNull
import com.mobileprism.fishing.utils.Constants
import org.koin.androidx.compose.get

@Composable
fun NewCatchWindView(
    modifier: Modifier = Modifier,
    wind: String,
    windDeg: Int,
    onWindChange: (String) -> Unit,
    onWindDirChange: (Float) -> Unit,
) {
    val weatherSettings: WeatherPreferences = get()
    val windSpeedUnit by weatherSettings.getWindSpeedUnit.collectAsState(WindSpeedValues.kmph)

    var windDirDialogState by remember { mutableStateOf(false) }

    if (windDirDialogState) {
        PickWindDirDialog(
            onDirectionSelected = {
                onWindDirChange(it)
                windDirDialogState = false
            },
            onDismiss = { windDirDialogState = false }
        )
    }

    FishingOutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        readOnly = false,
        value = wind,
        leadingIcon = {
            IconButton(onClick = { windDirDialogState = true }) {
                Icon(
                    modifier = Modifier.rotate(windDeg.toFloat()),
                    painter = painterResource(id = R.drawable.ic_baseline_navigation_24),
                    contentDescription = "",
                    tint = MaterialTheme.colors.primary,
                )
            }

        },
        trailingIcon = { Text(text = stringResource(windSpeedUnit.stringRes)) },
        onValueChange = { onWindChange(it) },
        placeholder = stringResource(R.string.wind),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}

@Composable
fun PickWindDirDialog(onDirectionSelected: (Float) -> Unit, onDismiss: () -> Unit) {
    DefaultDialog(
        primaryText = stringResource(R.string.choose_wind_direction),
        content = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisAlignment = FlowMainAxisAlignment.Center,
                crossAxisAlignment = FlowCrossAxisAlignment.Center,
            ) {
                (0..7).forEach {
                    WindIconItem(
                        rotation = it * Constants.WIND_ROTATION,
                        onIconSelected = { onDirectionSelected(it * Constants.WIND_ROTATION) }
                    )
                }
            }
        }, onDismiss = onDismiss
    )
}