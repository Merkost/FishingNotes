package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.theme.supportTextColor
import com.joesemper.fishing.model.entity.common.Note

@ExperimentalComposeUiApi
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
            val (subtitle, text) = createRefs()

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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = icon,
            contentDescription = null,
            tint = supportTextColor
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = supportTextColor
        )
    }
}

@Composable
fun WeatherIconItem(
    iconResource: Int,
    iconTint: Color = Color.Unspecified,
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
        Icon(painterResource(iconResource), "", tint = iconTint)
    }
}

