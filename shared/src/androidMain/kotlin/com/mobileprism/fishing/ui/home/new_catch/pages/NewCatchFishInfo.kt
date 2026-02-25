package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.new_catch.FishAmountAndWeightViewItem
import com.mobileprism.fishing.ui.home.new_catch.FishSpecies
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel

@Composable
fun NewCatchFishInfo(viewModel: NewCatchMasterViewModel, navController: NavController) {

    val state by viewModel.fishAndWeightSate.collectAsState()

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (subtitleFish, fish, amountAndWeight) = createRefs()

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitleFish) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            icon = R.drawable.ic_fish,
            text = stringResource(R.string.fish_catch)
        )

        FishSpecies(
            modifier = Modifier.constrainAs(fish) {
                top.linkTo(subtitleFish.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            name = state.fish,
            onNameChange = viewModel::setFishType
        )

        FishAmountAndWeightViewItem(
            modifier = Modifier.constrainAs(amountAndWeight) {
                top.linkTo(fish.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            amountState = state.fishAmount,
            weightState = state.fishWeight,
            onAmountChange = viewModel::setFishAmount,
            onWeightChange = viewModel::setFishWeight
        )
    }
}