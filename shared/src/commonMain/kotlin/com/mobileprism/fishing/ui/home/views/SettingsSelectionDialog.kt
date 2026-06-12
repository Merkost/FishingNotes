package com.mobileprism.fishing.ui.home.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> SettingsSelectionDialog(
    title: String,
    options: List<T>,
    current: T,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    onDismiss: () -> Unit,
) {
    val currentState: State<T?> = remember(current) { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = title, style = AppTextStyle.Title) },
        text = {
            ItemsSelection(
                radioOptions = options,
                currentOption = currentState,
                labelProvider = optionLabel,
                onSelectedItem = {
                    onSelect(it)
                    onDismiss()
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                AppText(text = stringResource(Res.string.cancel), style = AppTextStyle.Body)
            }
        },
    )
}
