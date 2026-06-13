package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.mobileprism.fishing.ui.theme.Spacing
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.no
import fishing.shared.generated.resources.yes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DefaultDialog(
    primaryText: String? = null,
    secondaryText: String? = null,
    textAlign: TextAlign = TextAlign.Start,
    neutralButtonText: String = "",
    onNeutralClick: (() -> Unit)? = null,
    negativeButtonText: String = stringResource(Res.string.no),
    onNegativeClick: (() -> Unit)? = null,
    positiveButtonText: String = stringResource(Res.string.yes),
    onPositiveClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    content: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = primaryText?.let {
            {
                AppText(
                    modifier = Modifier.fillMaxWidth(),
                    text = primaryText,
                    style = AppTextStyle.Title,
                    textAlign = textAlign,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = when (textAlign) {
                    TextAlign.Start -> Alignment.Start
                    TextAlign.End -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }
            ) {
                if (secondaryText != null) {
                    AppText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        text = secondaryText,
                        style = AppTextStyle.BodySmall,
                        textAlign = textAlign,
                    )
                }

                content?.let {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        it()
                    }
                }
            }
        },
        confirmButton = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                onNeutralClick?.let {
                    AppButton(
                        text = neutralButtonText,
                        onClick = onNeutralClick,
                        style = AppButtonStyle.Text,
                    )
                }
                onNegativeClick?.let {
                    AppButton(
                        text = negativeButtonText,
                        onClick = onNegativeClick,
                        style = AppButtonStyle.Text,
                    )
                }
                onPositiveClick?.let {
                    AppButton(
                        text = positiveButtonText,
                        onClick = onPositiveClick,
                        style = AppButtonStyle.Filled,
                    )
                }
            }
        }
    )
}

@Composable
fun ModalLoadingDialog(
    visible: Boolean,
    text: String,
    progress: Float? = null
) {
    if (visible) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .wrapContentWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp)
                    )
                    AppText(
                        text = text,
                        style = AppTextStyle.Title,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (progress != null) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
