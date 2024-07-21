package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.airbnb.lottie.compose.*
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.utils.time.toDate

@Composable
fun DefaultNoteView(
    modifier: Modifier = Modifier,
    note: Note,
    onClick: () -> Unit,
) {

    DefaultCardClickable(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = onClick
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            val (subtitle, text, noteDate) = createRefs()

            SubtitleWithIcon(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(parent.top, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                },
                icon = R.drawable.ic_baseline_sticky_note_2_24,
                text = stringResource(id = R.string.note)
            )

            if (note.description.isEmpty()) {
                NoContentView(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .constrainAs(text) {
                            top.linkTo(subtitle.bottom, 8.dp)
                            absoluteLeft.linkTo(subtitle.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                            width = Dimension.fillToConstraints
                        },
                    text = stringResource(id = R.string.no_description),
                    icon = painterResource(id = R.drawable.ic_no_note)
                )
            } else {
                SimpleUnderlineTextField(
                    modifier = Modifier.constrainAs(text) {
                        top.linkTo(subtitle.bottom, 8.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                        absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                        width = Dimension.fillToConstraints
                    },
                    singleLine = false,
                    text = note.description,
                    onClick = onClick
                )
                SupportText(modifier = Modifier.constrainAs(noteDate) {
                    top.linkTo(text.bottom, 4.dp)
                    bottom.linkTo(parent.bottom, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                }, text = note.dateCreated.toDate())
            }
        }
    }
}

@Composable
fun NoContentView(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.customColors.secondaryIconColor
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.customColors.secondaryTextColor
        )
    }
}

@Composable
fun WeatherIconItem(
    iconResource: Int,
    iconTint: Color = Color.Unspecified,
    requiredSize: Dp = 50.dp,
    onIconSelected: (() -> Unit)? = null
) {
    val clickableModifier = onIconSelected?.let { Modifier.clickable { onIconSelected() } } ?: Modifier
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
            .requiredSize(requiredSize)
            .then(clickableModifier)
    ) {
        Icon(painterResource(iconResource), "", tint = iconTint)
    }
}

@Composable
fun WindIconItem(
    iconTint: Color = MaterialTheme.colors.primaryVariant,
    rotation: Float,
    onIconSelected: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
            .requiredSize(50.dp)
            .clickable(onClick = onIconSelected)
    ) {
        Icon(
            modifier = Modifier.rotate(rotation),
            painter = painterResource(R.drawable.ic_baseline_navigation_24),
            contentDescription = null,
            tint = iconTint
        )
    }
}

@Composable
fun NoInternetView(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.error))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition,
            progress,
            modifier = modifier
        )
        SupportText(text = stringResource(id = R.string.network_error_message))
    }
}

@Composable
fun ErrorView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = R.drawable.ic_error), contentDescription = null
        )
        SupportText(text = "Something went wrong!")
    }
}

