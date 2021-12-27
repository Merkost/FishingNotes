package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.catch_screen.EditNoteDialog
import com.joesemper.fishing.compose.ui.home.views.DefaultCardClickable
import com.joesemper.fishing.compose.ui.home.views.PrimaryText
import com.joesemper.fishing.compose.ui.home.views.SecondaryText
import com.joesemper.fishing.compose.ui.home.views.SubtitleWithIcon

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
                SecondaryText(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .constrainAs(text) {
                            top.linkTo(subtitle.bottom, 16.dp)
                            absoluteLeft.linkTo(subtitle.absoluteLeft)
                            absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                            width = Dimension.fillToConstraints
                        },
                    text = stringResource(id = R.string.no_description)
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

