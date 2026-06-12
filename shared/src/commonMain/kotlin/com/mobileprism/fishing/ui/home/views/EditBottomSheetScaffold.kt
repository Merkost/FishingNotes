package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.cancel
import fishing.shared.generated.resources.save
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditBottomSheetScaffold(
    title: String,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    leadingAction: (@Composable () -> Unit)? = null,
    saveEnabled: Boolean = true,
    saving: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingAction != null) {
                leadingAction()
            }
            AppText(
                text = title,
                style = AppTextStyle.Heading,
                modifier = Modifier.weight(1f).padding(start = if (leadingAction != null) Spacing.sm else Spacing.none),
            )
        }
        content()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppButton(
                text = stringResource(Res.string.cancel),
                onClick = onCancel,
                style = AppButtonStyle.Text,
            )
            AppButton(
                text = stringResource(Res.string.save),
                onClick = onSave,
                style = AppButtonStyle.Filled,
                enabled = saveEnabled,
                loading = saving,
            )
        }
    }
}
