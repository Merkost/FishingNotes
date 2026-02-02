package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.R
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DefaultDialog(
    primaryText: String? = null,
    secondaryText: String? = null,
    textAlign: TextAlign = TextAlign.Start,
    neutralButtonText: String = "",
    onNeutralClick: (() -> Unit)? = null,
    negativeButtonText: String = stringResource(id = R.string.no),
    onNegativeClick: (() -> Unit)? = null,
    positiveButtonText: String = stringResource(id = R.string.yes),
    onPositiveClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    content: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = primaryText?.let {
            {
                PrimaryText(
                    modifier = Modifier.fillMaxWidth(),
                    text = primaryText,
                    textAlign = textAlign,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = when (textAlign) {
                    TextAlign.Start -> Alignment.Start
                    TextAlign.End -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }
            ) {
                if (secondaryText != null) {
                    PrimaryTextSmall(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        text = secondaryText,
                        textAlign = textAlign,
                    )
                }

                content?.let {
                    Spacer(modifier = Modifier.height(14.dp))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onNeutralClick?.let {
                        DefaultButtonSecondaryLight(
                            text = neutralButtonText,
                            onClick = onNeutralClick
                        )
                    }
                    onNegativeClick?.let {
                        DefaultButton(
                            text = negativeButtonText,
                            onClick = onNegativeClick
                        )
                    }
                }
                onPositiveClick?.let {
                    DefaultButtonFilled(
                        text = positiveButtonText,
                        onClick = onPositiveClick
                    )
                }
            }
        }
    )
}

@Composable
fun ModalLoadingDialog(
    visible: Boolean,
    text: String
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
                    PrimaryText(
                        text = text,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
