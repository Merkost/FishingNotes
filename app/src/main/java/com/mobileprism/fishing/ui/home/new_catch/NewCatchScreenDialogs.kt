package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.views.DefaultDialog


@Composable
fun AddNewCatchErrorDialog(onClose: () -> Unit, onRetry: () -> Unit) {
    DefaultDialog(
        primaryText = stringResource(R.string.error_occured),
        secondaryText = stringResource(R.string.new_catch_error_description),
        onDismiss = onClose,
        negativeButtonText = stringResource(R.string.Cancel),
        onNegativeClick = onClose,
        positiveButtonText = stringResource(R.string.Try_again),
        onPositiveClick = onRetry
    )
}
