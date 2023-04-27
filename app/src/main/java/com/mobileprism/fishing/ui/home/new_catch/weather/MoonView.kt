package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.mappers.getMoonIconByPhase
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField

@Composable
fun NewCatchMoonView(
    modifier: Modifier = Modifier,
    moonPhase: Float,
) {
    FishingOutlinedTextField(
        textFieldModifier = modifier.fillMaxWidth(),
        readOnly = true,
        value = (moonPhase * 100).toInt().toString(),
        leadingIcon = {
            Icon(
                painter = painterResource(
                    id = getMoonIconByPhase(moonPhase)
                ),
                contentDescription = "",
                tint = MaterialTheme.colors.primary
            )
        },
        onValueChange = { },
        trailingIcon = {
            Text(text = stringResource(R.string.percent))
        },
        placeholder = stringResource(R.string.moon_phase),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}