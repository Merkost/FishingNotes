package com.mobileprism.fishing.compose.ui.home.new_catch.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.home.new_catch.WayOfFishingView
import com.mobileprism.fishing.compose.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.domain.NewCatchMasterViewModel

@Composable
fun NewCatchNote(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val (subtitle, note, subtitleRod, rod) = createRefs()

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitleRod) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            icon = R.drawable.ic_fishing_rod,
            text = stringResource(R.string.way_of_fishing)
        )

        WayOfFishingView(
            modifier = Modifier.constrainAs(rod) {
                top.linkTo(subtitleRod.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            rodState = viewModel.rod.collectAsState(),
            biteState = viewModel.bait.collectAsState(),
            lureState = viewModel.lure.collectAsState(),
            onRodChange = { viewModel.setRod(it) },
            onBiteChange = { viewModel.setBait(it) },
            onLureChange = { viewModel.setLure(it) }
        )

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(rod.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            icon = R.drawable.ic_baseline_edit_note_24,
            text = stringResource(id = R.string.note)
        )

        OutlinedTextField(
            modifier = Modifier
                .constrainAs(note) {
                    top.linkTo(subtitle.bottom, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                    width = Dimension.fillToConstraints
                },
            singleLine = false,
            maxLines = 5,
            label = { Text(text = stringResource(id = R.string.note)) },
            value = viewModel.description.collectAsState().value,
            onValueChange = { viewModel.setNote(it) }
        )

    }
}