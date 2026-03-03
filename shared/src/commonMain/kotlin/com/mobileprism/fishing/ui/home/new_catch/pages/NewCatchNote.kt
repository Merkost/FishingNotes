package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.new_catch.WayOfFishingView
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel

@Composable
fun NewCatchNote(viewModel: NewCatchMasterViewModel, navController: NavController) {

    val state by viewModel.catchInfoState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SubtitleWithIcon(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp),
            icon = Res.drawable.ic_fishing_rod,
            text = stringResource(Res.string.way_of_fishing)
        )

        WayOfFishingView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            rodState = state.rod,
            biteState = state.bait,
            lureState = state.lure,
            onRodChange = { viewModel.setRod(it) },
            onBiteChange = { viewModel.setBait(it) },
            onLureChange = { viewModel.setLure(it) }
        )

        SubtitleWithIcon(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp),
            icon = Res.drawable.ic_baseline_edit_note_24,
            text = stringResource(Res.string.note)
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            singleLine = false,
            maxLines = 5,
            label = { Text(text = stringResource(Res.string.note)) },
            value = state.note,
            onValueChange = { viewModel.setNote(it) }
        )
    }
}