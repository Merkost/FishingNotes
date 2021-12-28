package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.joesemper.fishing.R

@ExperimentalComposeUiApi
@Composable
fun DefaultDialog(
    primaryText: String,
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
    Dialog(
        onDismissRequest = onDismiss
    ) {
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

                PrimaryText(
                    modifier = Modifier.constrainAs(title) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    },
                    text = primaryText,
                )

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
                        }
                ) {
                    content?.invoke()
                }

                if (neutralButtonText.isNotEmpty()) {
                    DefaultButton(
                        modifier = Modifier.constrainAs(neutralButton) {
                            top.linkTo(positiveButton.top)
                            absoluteLeft.linkTo(parent.absoluteLeft)
                        },
                        text = neutralButtonText,
                        onClick = onNeutralClick,
                    )
                }

                DefaultButtonFilled(
                    modifier = Modifier.constrainAs(positiveButton) {
                        top.linkTo(mainContent.bottom, 16.dp)
                        bottom.linkTo(parent.bottom)
                        absoluteRight.linkTo(parent.absoluteRight)
                    },
                    text = positiveButtonText,
                    onClick = onPositiveClick,
                )

                if (negativeButtonText.isNotEmpty()) {
                    DefaultButton(
                        modifier = Modifier.constrainAs(negativeButton) {
                            top.linkTo(positiveButton.top)
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
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fish_loading))
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