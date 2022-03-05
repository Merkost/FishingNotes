package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.utils.enums.StringOperation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> ItemsSelection(
    modifier: Modifier = Modifier,
    radioOptions: List<T>,
    currentOption: State<T?>,
    onSelectedItem: (T) -> Unit,
) where T : StringOperation {

    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(currentOption.value)
    }

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        radioOptions.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            onOptionSelected(option)
                            onSelectedItem(option)
                        }
                    )
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    modifier = Modifier.padding(all = Dp(value = 8F)),
                    onClick = {
                        onOptionSelected(option)
                        onSelectedItem(option)
                    }
                )
                Text(
                    text = stringResource(option.stringRes),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}