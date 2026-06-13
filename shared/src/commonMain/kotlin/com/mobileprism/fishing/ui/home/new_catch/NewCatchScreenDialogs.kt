package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.LottieWarning

@Composable
fun CancelNewCatchDialog(
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit
) {
    DefaultDialog(
        primaryText = stringResource(Res.string.cancel_new_catch_dialog),
        secondaryText = stringResource(Res.string.sure_cancel_new_catch_dialog),
        negativeButtonText = stringResource(Res.string.no),
        onNegativeClick = onDismiss,
        positiveButtonText = stringResource(Res.string.yes),
        onPositiveClick = onPositiveClick,
        onDismiss = onDismiss,
        content = {
            LottieWarning(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    )
}

