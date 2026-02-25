package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R

@Composable
fun ConflictResolutionDialog(
    title: String,
    localDetails: List<Pair<String, String>>,
    serverDetails: List<Pair<String, String>>,
    onKeepLocal: () -> Unit,
    onUseServer: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.sync_conflict_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.sync_conflict_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (localDetails.isNotEmpty() || serverDetails.isNotEmpty()) {
                    val allKeys = (localDetails.map { it.first } + serverDetails.map { it.first }).distinct()
                    allKeys.forEach { key ->
                        val localVal = localDetails.find { it.first == key }?.second ?: "-"
                        val serverVal = serverDetails.find { it.first == key }?.second ?: "-"
                        if (localVal != serverVal) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$localVal → $serverVal",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onKeepLocal) {
                Text(stringResource(R.string.keep_my_version))
            }
        },
        dismissButton = {
            TextButton(onClick = onUseServer) {
                Text(stringResource(R.string.use_server_version))
            }
        }
    )
}
