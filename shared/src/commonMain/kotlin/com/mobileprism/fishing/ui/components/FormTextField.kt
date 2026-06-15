package com.mobileprism.fishing.ui.components

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: Painter? = null,
    readOnly: Boolean = false,
    isError: Boolean = false,
    singleLine: Boolean = true,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Next,
    ),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = leadingIcon?.let {
            { Icon(painter = it, contentDescription = null) }
        },
        readOnly = readOnly,
        isError = isError,
        singleLine = singleLine,
        supportingText = supportingText?.let { { Text(it) } },
        shape = FishingTheme.shapes.small,
        keyboardOptions = keyboardOptions,
    )
}

@Composable
fun PickerField(
    value: String,
    label: String,
    leadingIcon: Painter?,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) onClick()
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder, color = FishingTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = leadingIcon?.let {
            { Icon(painter = it, contentDescription = null) }
        },
        readOnly = true,
        singleLine = true,
        shape = FishingTheme.shapes.small,
        interactionSource = interactionSource,
    )
}
