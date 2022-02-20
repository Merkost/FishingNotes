package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.mobileprism.fishing.domain.NewCatchMasterViewModel

@Composable
fun NewCatchFishInfo(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (subtitleFish, subtitleRod, fish, amountAndWeight, rod) = createRefs()

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
            name = viewModel.fishType.collectAsState(),
            onNameChange = viewModel::setFishType
        )

        FishAmountAndWeightViewItem(
            modifier = Modifier.constrainAs(amountAndWeight) {
                top.linkTo(fish.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                width = Dimension.fillToConstraints
            },
            amountState = viewModel.fishAmount.collectAsState(),
            weightState = viewModel.fishWeight.collectAsState(),
            onAmountChange = viewModel::setFishAmount,
            onWeightChange = viewModel::setFishWeight
        )

//        SubtitleWithIcon(
//            modifier = Modifier.constrainAs(subtitleRod) {
//                top.linkTo(amountAndWeight.bottom, 16.dp)
//                absoluteLeft.linkTo(parent.absoluteLeft)
//            },
//            icon = R.drawable.ic_fishing_rod,
//            text = stringResource(R.string.way_of_fishing)
//        )
//
//        WayOfFishingView(
//            modifier = Modifier.constrainAs(rod) {
//                top.linkTo(subtitleRod.bottom, 16.dp)
//                absoluteLeft.linkTo(parent.absoluteLeft)
//                absoluteRight.linkTo(parent.absoluteRight)
//            },
//            rodState = viewModel.rod.collectAsState(),
//            biteState = viewModel.bait.collectAsState(),
//            lureState = viewModel.lure.collectAsState(),
//            onRodChange = { viewModel.setRod(it) },
//            onBiteChange = { viewModel.setBait(it) },
//            onLureChange = { viewModel.setLure(it) }
//        )

    }
}