package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mobileprism.fishing.R


@Composable
fun AddNewCatchErrorDialog(onClose: () -> Unit, onRetry: () -> Unit) {
    AlertDialog(
        title = { Text(stringResource(R.string.error_occured)) },
        text = { Text(stringResource(R.string.new_catch_error_description)) },
        onDismissRequest = { onClose() },
        confirmButton = {
            OutlinedButton(
                onClick = onRetry,
                content = { Text(stringResource(R.string.Try_again)) })
        }, dismissButton = {
            OutlinedButton(
                onClick = { onClose() },
                content = { Text(stringResource(R.string.Cancel)) })
        }
    )
}
