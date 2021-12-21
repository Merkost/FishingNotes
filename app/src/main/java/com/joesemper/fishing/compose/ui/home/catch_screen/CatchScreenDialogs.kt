package com.joesemper.fishing.compose.ui.home.catch_screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.DefaultDialog
import com.joesemper.fishing.compose.ui.home.FishAmountAndWeightView
import com.joesemper.fishing.compose.ui.home.SimpleOutlinedTextField
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.content.UserCatch

@ExperimentalComposeUiApi
@Composable
fun FishTypeAmountAndWeightDialog(
    catch: UserCatch,
    dialogState: MutableState<Boolean>,
    viewModel: UserCatchViewModel
) {

    val fishType = remember { mutableStateOf(catch.fishType) }
    val fishAmount = remember { mutableStateOf(catch.fishAmount.toString()) }
    val fishWeight = remember { mutableStateOf(catch.fishWeight.toString()) }

    DefaultDialog(
        primaryText = stringResource(id = R.string.user_catch),
        positiveButtonText = stringResource(id = R.string.save),
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = { dialogState.value = false },
        onPositiveClick = {
            viewModel.updateCatch(
                data = mapOf(
                    "fishType" to fishType.value,
                    "fishAmount" to fishAmount.value.toInt(),
                    "fishWeight" to fishWeight.value.toDouble()
                )
            )
            dialogState.value = false
        },
        onDismiss = { dialogState.value = false },
    ) {

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SimpleOutlinedTextField(
                textState = fishType,
                label = stringResource(id = R.string.fish_species)
            )
            FishAmountAndWeightView(
                amountState = fishAmount,
                weightState = fishWeight
            )

        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun EditWayOfFishingDialog(
    catch: UserCatch,
    dialogState: MutableState<Boolean>,
    viewModel: UserCatchViewModel
) {
    val rodState = remember { mutableStateOf(catch.fishingRodType) }
    val baitState = remember { mutableStateOf(catch.fishingBait) }
    val lureState = remember { mutableStateOf(catch.fishingLure) }

    DefaultDialog(
        primaryText = stringResource(id = R.string.way_of_fishing),
        positiveButtonText = stringResource(id = R.string.save),
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = { dialogState.value = false },
        onPositiveClick = {
            viewModel.updateCatch(
                data = mapOf(
                    "fishingRodType" to rodState.value,
                    "fishingBait" to baitState.value,
                    "fishingLure" to lureState.value
                )
            )
            dialogState.value = false
        },
        onDismiss = { dialogState.value = false },
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SimpleOutlinedTextField(
                textState = rodState,
                label = stringResource(id = R.string.fish_rod),
                singleLine = false
            )
            SimpleOutlinedTextField(
                textState = baitState,
                label = stringResource(id = R.string.bait),
                singleLine = false
            )
            SimpleOutlinedTextField(
                textState = lureState,
                label = stringResource(id = R.string.lure),
                singleLine = false
            )

        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun EditNoteDialog(
    note: String,
    dialogState: MutableState<Boolean>,
    onSaveNoteChange: (String) -> Unit
) {
    val noteState = remember { mutableStateOf(note) }

    DefaultDialog(
        primaryText = stringResource(id = R.string.note),
        positiveButtonText = stringResource(id = R.string.save),
        negativeButtonText = stringResource(id = R.string.cancel),
        onNegativeClick = { dialogState.value = false },
        onPositiveClick = {
            onSaveNoteChange(noteState.value)
            dialogState.value = false
        },
        onDismiss = { dialogState.value = false },
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SimpleOutlinedTextField(
                textState = noteState,
                label = stringResource(id = R.string.note),
                singleLine = false
            )

        }
    }
}