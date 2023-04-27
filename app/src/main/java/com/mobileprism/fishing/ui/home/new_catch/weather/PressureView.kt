package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField
import com.mobileprism.fishing.ui.home.weather.PressureValues
import org.koin.androidx.compose.get

@Composable
fun NewCatchPressureView(
    modifier: Modifier = Modifier,
    pressure: String,
    onPressureChange: (String) -> Unit,
) {
    val weatherSettings: WeatherPreferences = get()
    val pressureUnit by weatherSettings.getPressureUnit.collectAsState(PressureValues.mmHg)

    FishingOutlinedTextField(
        textFieldModifier = modifier.fillMaxWidth(),
        readOnly = false,
        value = pressure,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_gauge),
                contentDescription = "",
                tint = MaterialTheme.colors.primary
            )
        },
        trailingIcon = { Text(text = stringResource(pressureUnit.stringRes)) },
        onValueChange = { onPressureChange(it) },
        placeholder = stringResource(R.string.pressure),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )

}