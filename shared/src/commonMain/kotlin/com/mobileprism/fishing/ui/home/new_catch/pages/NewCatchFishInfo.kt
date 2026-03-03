package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.new_catch.FishAmountAndWeightViewItem
import com.mobileprism.fishing.ui.home.new_catch.FishSpecies
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel

@Composable
fun NewCatchFishInfo(viewModel: NewCatchMasterViewModel, navController: NavController) {

    val state by viewModel.fishAndWeightState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SubtitleWithIcon(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp),
            icon = Res.drawable.ic_fish,
            text = stringResource(Res.string.fish_catch)
        )

        FishSpecies(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            name = state.fish,
            onNameChange = viewModel::setFishType
        )

        FishAmountAndWeightViewItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            amountState = state.fishAmount,
            weightState = state.fishWeight,
            onAmountChange = viewModel::setFishAmount,
            onWeightChange = viewModel::setFishWeight
        )
    }
}