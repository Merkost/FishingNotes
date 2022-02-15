package com.mobileprism.fishing.compose.ui.home.views

import android.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar.MILLISECONDS_IN_DAY
import android.icu.util.Calendar.YEAR
import android.os.Build
import android.view.LayoutInflater
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mobileprism.fishing.utils.time.TimeConstants
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.*

@Composable
fun DatePickerDialog(
    context: Context,
    maxDate: Long = Calendar.getInstance().timeInMillis,
    minDate: Long = 0L,
    initialDate: Long = maxDate,
    onDateChange: (Long) -> Unit,
    onDismiss: () -> Unit,

) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialDate
    }

    DatePickerDialog(
        context,
        R.style.Theme_Material_Dialog_Alert,
        { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            onDateChange(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = maxDate
        datePicker.minDate = minDate
        show()
    }
    onDismiss.invoke()
}

@Composable
fun WhiteCardWrapper(content: @Composable () -> Unit) {
    Card(elevation = 4.dp, shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(8.dp), backgroundColor = Color.White,
        content = content)
}

@Composable
fun TimePickerDialog(
    context: Context,
    initialTime: Long = 0L,
    onTimeChange: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialTime
    }

    TimePickerDialog(
        context,
        R.style.Theme_Material_Dialog_Alert,
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            onTimeChange(calendar.timeInMillis)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE), true
    ).show()
    onDismiss.invoke()
}

@Composable
fun DatePicker(
    dateSetState: MutableState<Boolean>,
    context: Context,
    onDateChange: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        context,
        android.R.style.Theme_Material_Dialog_Alert,
        { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            onDateChange(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = Date().time
        datePicker.minDate = Date().time - (TimeConstants.MILLISECONDS_IN_DAY * 5)
        show()
    }
    dateSetState.value = false
}
