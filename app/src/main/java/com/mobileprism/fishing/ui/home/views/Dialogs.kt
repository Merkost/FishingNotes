package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.R
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

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
    Dialog(onDismissRequest = onDismiss) {
        DefaultCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = when (textAlign) {
                    TextAlign.Start -> Alignment.Start
                    TextAlign.End -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }
            ) {
                primaryText?.let {
                    PrimaryText(
                        modifier = Modifier.fillMaxWidth(),
                        text = primaryText,
                        textAlign = textAlign,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))


                if (secondaryText != null) {
                    PrimaryTextSmall(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        text = secondaryText,
                        textAlign = textAlign,
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    content?.invoke()
                }

                Spacer(modifier = Modifier.height(16.dp))

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
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun LoadingDialog() {
    DefaultDialog(
        primaryText = stringResource(R.string.loading),
        onDismiss = {}
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                modifier = Modifier.size(256.dp),
                composition = composition,
                iterations = LottieConstants.IterateForever,
                isPlaying = true
            )
        }
    }
}

@Composable
fun ModalLoadingDialog(
    dialogState: MutableState<Boolean>,
    text: String
) {
    if (dialogState.value) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp)
                )
                PrimaryText(
                    text = text,
                    textColor = Color.White
                )
            }
        }
    }
}