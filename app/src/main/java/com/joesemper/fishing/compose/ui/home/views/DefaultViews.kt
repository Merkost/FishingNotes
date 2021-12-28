package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.catch_screen.EditNoteDialog
import com.joesemper.fishing.compose.ui.home.views.DefaultCardClickable
import com.joesemper.fishing.compose.ui.home.views.PrimaryText
import com.joesemper.fishing.compose.ui.home.views.SubtitleWithIcon
import com.joesemper.fishing.compose.ui.theme.supportTextColor

@ExperimentalComposeUiApi
@Composable
fun DefaultNoteView(
    modifier: Modifier = Modifier,
    note: String,
    onSaveNoteChange: (String) -> Unit
) {

    val dialogState = remember { mutableStateOf(false) }

    if (dialogState.value) {
        EditNoteDialog(
            note = note,
            dialogState = dialogState,
            onSaveNoteChange = onSaveNoteChange
        )
    }

    DefaultCardClickable(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = {
            dialogState.value = true
        }
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

            if (note.isBlank()) {
                NoContentView(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .constrainAs(text) {
                            top.linkTo(subtitle.bottom, 16.dp)
                            absoluteLeft.linkTo(subtitle.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                            width = Dimension.fillToConstraints
                        },
                    text = stringResource(id = R.string.no_description),
                    icon = painterResource(id = R.drawable.ic_no_note)
                )
            } else {
                PrimaryText(
                    modifier = Modifier.constrainAs(text) {
                        top.linkTo(subtitle.bottom, 8.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                        absoluteLeft.linkTo(subtitle.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                        width = Dimension.fillToConstraints
                    },
                    text = note
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

