package com.joesemper.fishing.compose.ui.home.new_catch

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.catch_screen.AddPhotoDialog
import com.joesemper.fishing.domain.NewCatchViewModel
import com.joesemper.fishing.utils.time.TimeConstants.MILLISECONDS_IN_DAY
import org.koin.androidx.compose.getViewModel
import java.util.*

sealed class BottomSheetNewCatchScreen() {
    object EditPhotosScreen : BottomSheetNewCatchScreen()
}

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun NewCatchModalBottomSheetContent(
    currentScreen: BottomSheetNewCatchScreen,
    viewModel: NewCatchViewModel,
    onCloseBottomSheet: () -> Unit,
) {
    when (currentScreen) {
        BottomSheetNewCatchScreen.EditPhotosScreen -> {
            AddPhotoDialog(
                photos = viewModel.images,
                onSavePhotosClick = { newPhotos ->
                    viewModel.images.apply {
                        clear()
                        addAll(newPhotos)
                    }
                },
                onCloseBottomSheet = onCloseBottomSheet
            )
        }
    }
}

@Composable
fun ErrorDialog(errorDialog: MutableState<Boolean>) {
    val viewModel: NewCatchViewModel = getViewModel()
    AlertDialog(
        title = { Text(stringResource(R.string.error_occured)) },
        text = { Text(stringResource(R.string.new_catch_error_description)) },
        onDismissRequest = { errorDialog.value = false },
        confirmButton = {
            OutlinedButton(
                onClick = { viewModel.createNewUserCatch() },
                content = { Text(stringResource(R.string.Try_again)) })
        }, dismissButton = {
            OutlinedButton(
                onClick = { errorDialog.value = false },
                content = { Text(stringResource(R.string.Cancel)) })
        }
    )
}

@Composable
fun TimePicker(
    date: MutableState<Long>,
    timeSetState: MutableState<Boolean>,
    context: Context
) {
    val calendar = Calendar.getInstance()

    TimePickerDialog(
        context,
        android.R.style.Theme_Material_Dialog_Alert,
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            date.value = calendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE), true
    ).show()
    timeSetState.value = false
}

@Composable
fun DatePicker(
    date: MutableState<Long>,
    dateSetState: MutableState<Boolean>,
    context: Context
) {
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        context,
        android.R.style.Theme_Material_Dialog_Alert,
        { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            date.value = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = Date().time
        datePicker.minDate = Date().time - (MILLISECONDS_IN_DAY * 5)
        show()
    }
    dateSetState.value = false
}