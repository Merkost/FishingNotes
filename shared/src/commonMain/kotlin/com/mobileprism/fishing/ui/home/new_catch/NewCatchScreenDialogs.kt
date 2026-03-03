package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.views.DefaultDialog


@Composable
fun AddNewCatchErrorDialog(onClose: () -> Unit, onRetry: () -> Unit) {
    DefaultDialog(
        primaryText = stringResource(Res.string.error_occured),
        secondaryText = stringResource(Res.string.new_catch_error_description),
        onDismiss = onClose,
        negativeButtonText = stringResource(Res.string.cancel),
        onNegativeClick = onClose,
        positiveButtonText = stringResource(Res.string.Try_again),
        onPositiveClick = onRetry
    )
}
