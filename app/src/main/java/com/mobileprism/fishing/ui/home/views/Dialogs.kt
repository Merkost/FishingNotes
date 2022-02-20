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

@ExperimentalComposeUiApi
@Composable
fun DefaultDialog(
    primaryText: String? = null,
    secondaryText: String? = null,
    neutralButtonText: String = "",
    onNeutralClick: (() -> Unit) = { },
    negativeButtonText: String = "",
    onNegativeClick: () -> Unit = { },
    positiveButtonText: String = "",
    onPositiveClick: () -> Unit = { },
    onDismiss: () -> Unit = { },
    content: @Composable() (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        DefaultCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize()
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                val (title, subtitle, mainContent, neutralButton, negativeButton, positiveButton) = createRefs()

                primaryText?.let {
                    PrimaryText(
                        modifier = Modifier.constrainAs(title) {
                            top.linkTo(parent.top)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                        },
                        text = primaryText,
                    )
                }

                if (secondaryText != null) {
                    PrimaryTextSmall(
                        modifier = Modifier.constrainAs(subtitle) {
                            top.linkTo(title.bottom, 2.dp)
                            absoluteLeft.linkTo(title.absoluteLeft)
                        },
                        text = secondaryText, textAlign = TextAlign.Start
                    )
                } else {
                    Spacer(modifier = Modifier
                        .size(1.dp)
                        .constrainAs(subtitle) {
                            top.linkTo(title.bottom, 2.dp)
                            absoluteLeft.linkTo(title.absoluteLeft)
                        })
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .constrainAs(mainContent) {
                            top.linkTo(subtitle.bottom, 16.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight)
                            width = Dimension.fillToConstraints
                        },
                    contentAlignment = Alignment.Center
                ) {
                    content?.invoke()
                }

                if (neutralButtonText.isNotEmpty()) {
                    DefaultButtonSecondaryLight(
                        modifier = Modifier.constrainAs(neutralButton) {
                            top.linkTo(mainContent.bottom, 16.dp)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                        },
                        text = neutralButtonText,
                        onClick = onNeutralClick,
                    )
                }

                if (positiveButtonText.isNotEmpty()) {
                    DefaultButtonFilled(
                        modifier = Modifier.constrainAs(positiveButton) {
                            top.linkTo(mainContent.bottom, 16.dp)
                            bottom.linkTo(parent.bottom)
                            absoluteRight.linkTo(parent.absoluteRight)
                        },
                        text = positiveButtonText,
                        onClick = onPositiveClick,
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .size(0.dp)
                            .constrainAs(positiveButton) {
                                top.linkTo(mainContent.bottom, 16.dp)
                                bottom.linkTo(parent.bottom)
                                absoluteRight.linkTo(parent.absoluteRight)
                            },
                    )
                }

                if (negativeButtonText.isNotEmpty()) {
                    DefaultButton(
                        modifier = Modifier.constrainAs(negativeButton) {
                            top.linkTo(mainContent.bottom, 16.dp)
                            absoluteRight.linkTo(positiveButton.absoluteLeft, 8.dp)
                        },
                        text = negativeButtonText,
                        onClick = onNegativeClick,
                    )
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
    dialogSate: MutableState<Boolean>,
    text: String
) {
    if (dialogSate.value) {
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