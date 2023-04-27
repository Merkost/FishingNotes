package com.mobileprism.fishing.ui.home.new_catch.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField
import com.mobileprism.fishing.utils.roundTo

@Composable
fun FishAmountAndWeightView(
    modifier: Modifier = Modifier,
    amountState: MutableState<String>,
    weightState: MutableState<String>
) {
    Row(modifier = modifier) {
        Column(Modifier.weight(1F)) {
            FishingOutlinedTextField(
                value = amountState.value,
                onValueChange = {
                    if (it.isEmpty()) amountState.value = it
                    else {
                        amountState.value = when (it.toIntOrNull()) {
                            null -> amountState.value //old value
                            else -> it   //new value
                        }
                    }
                },
                isError = amountState.value.isEmpty(),
                placeholder = stringResource(R.string.amount),
                trailingIcon = { Text(stringResource(R.string.pc)) },
                textFieldModifier = Modifier.fillMaxWidth(),
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
                        if (amountState.value.toInt() >= 1 && amountState.value.isNotBlank())
                            amountState.value = ((amountState.value.toInt() - 1).toString())
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        if (amountState.value.isEmpty()) amountState.value = 1.toString()
                        else amountState.value =
                            ((amountState.value.toInt() + 1).toString())
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
            }

        }
        Spacer(modifier = Modifier.size(6.dp))
        Column(Modifier.weight(1F)) {
            FishingOutlinedTextField(
                value = weightState.value,
                onValueChange = {
                    if (it.isEmpty()) weightState.value = it
                    else {
                        weightState.value = when (it.toDoubleOrNull()) {
                            null -> weightState.value //old value
                            else -> it   //new value
                        }
                    }
                },
                placeholder = stringResource(R.string.weight),
                trailingIcon = {
                    Text(stringResource(R.string.kg))
                },
                textFieldModifier = Modifier.fillMaxWidth(),
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
                        if (weightState.value.toDouble() >= 0.1 && weightState.value.isNotBlank())
                            weightState.value =
                                ((weightState.value.toDouble() - 0.1).roundTo(1).toString())
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_minus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                OutlinedButton(
                    onClick = {
                        if (weightState.value.isEmpty()) weightState.value =
                            0.1f.roundTo(1).toString()
                        else weightState.value =
                            ((weightState.value.toDouble() + 0.1).roundTo(1).toString())
                    },
                    Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_plus),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}