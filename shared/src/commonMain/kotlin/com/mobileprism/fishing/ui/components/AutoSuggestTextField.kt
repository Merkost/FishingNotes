package com.mobileprism.fishing.ui.components

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun AutoSuggestTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    leadingIcon: @Composable (() -> Unit)? = null,
    isRequired: Boolean = false,
    isError: Boolean = false,
) {
    var showSuggestions by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }

    val filtered = remember(value, suggestions) {
        if (value.isBlank()) suggestions.take(5)
        else suggestions.filter { it.contains(value, ignoreCase = true) }.take(5)
    }

    val displayLabel = if (isRequired) "$label *" else label
    val fieldShape = if (hasFocus && filtered.isNotEmpty() && showSuggestions) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    } else {
        RoundedCornerShape(16.dp)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                showSuggestions = true
            },
            label = { Text(displayLabel) },
            leadingIcon = leadingIcon,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    hasFocus = it.isFocused
                    showSuggestions = it.isFocused
                },
            isError = isError || (isRequired && value.isBlank()),
            singleLine = true,
            shape = fieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = FishingTheme.colorScheme.surfaceContainerHigh,
                focusedContainerColor = FishingTheme.colorScheme.surfaceContainerHigh,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        DropdownMenu(
            expanded = showSuggestions && hasFocus && filtered.isNotEmpty(),
            onDismissRequest = { showSuggestions = false },
            properties = PopupProperties(focusable = false)
        ) {
            filtered.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        val matchIndex = suggestion.indexOf(value, ignoreCase = true)
                        if (value.isNotBlank() && matchIndex >= 0) {
                            Text(buildAnnotatedString {
                                append(suggestion.substring(0, matchIndex))
                                withStyle(SpanStyle(color = FishingTheme.colorScheme.primary)) {
                                    append(suggestion.substring(matchIndex, matchIndex + value.length))
                                }
                                append(suggestion.substring(matchIndex + value.length))
                            })
                        } else {
                            Text(suggestion)
                        }
                    },
                    onClick = {
                        onValueChange(suggestion)
                        showSuggestions = false
                    }
                )
            }
        }
    }
}
