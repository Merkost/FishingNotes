package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.domain.entity.common.SyncState
import com.mobileprism.fishing.model.datasource.local.entity.SyncStatus
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.pending_upload
import fishing.shared.generated.resources.sync_conflict
import fishing.shared.generated.resources.sync_error
import fishing.shared.generated.resources.sync_pending
import org.jetbrains.compose.resources.stringResource

@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = syncState !is SyncState.Synced,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = when (syncState) {
                is SyncState.Pending -> FishingTheme.colorScheme.primaryContainer
                is SyncState.Error -> FishingTheme.colorScheme.errorContainer
                is SyncState.Conflict -> FishingTheme.colorScheme.errorContainer
                is SyncState.Synced -> FishingTheme.colorScheme.surface
            },
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (syncState) {
                    is SyncState.Pending -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = FishingTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(Res.string.sync_pending),
                            style = FishingTheme.typography.labelMedium,
                            color = FishingTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    is SyncState.Error -> {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = FishingTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(Res.string.sync_error),
                            style = FishingTheme.typography.labelMedium,
                            color = FishingTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    is SyncState.Conflict -> {
                        Icon(
                            imageVector = Icons.Default.SyncProblem,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = FishingTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(Res.string.sync_conflict),
                            style = FishingTheme.typography.labelMedium,
                            color = FishingTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    is SyncState.Synced -> { }
                }
            }
        }
    }
}

@Composable
fun SyncStatusIcon(
    syncStatus: Int,
    modifier: Modifier = Modifier
) {
    when (syncStatus) {
        SyncStatus.PENDING_CREATE, SyncStatus.PENDING_UPDATE -> {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = stringResource(Res.string.pending_upload),
                modifier = modifier.size(16.dp),
                tint = FishingTheme.colorScheme.primary
            )
        }
        SyncStatus.PENDING_DELETE -> {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = stringResource(Res.string.pending_upload),
                modifier = modifier.size(16.dp),
                tint = FishingTheme.colorScheme.outline
            )
        }
        SyncStatus.CONFLICT -> {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = stringResource(Res.string.sync_conflict),
                modifier = modifier.size(16.dp),
                tint = FishingTheme.colorScheme.error
            )
        }
        SyncStatus.SYNCED -> {
            // No icon for synced state
        }
    }
}
