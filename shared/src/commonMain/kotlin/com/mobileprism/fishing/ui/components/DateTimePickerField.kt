package com.mobileprism.fishing.ui.components

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.home.views.DatePickerDialog
import com.mobileprism.fishing.ui.home.views.TimePickerDialog
import com.mobileprism.fishing.utils.ValidationUtils
import com.mobileprism.fishing.utils.time.TimeConstants
import com.mobileprism.fishing.utils.time.toDate
import com.mobileprism.fishing.utils.time.toTime
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.date
import fishing.shared.generated.resources.ic_baseline_access_time_24
import fishing.shared.generated.resources.ic_baseline_event_24
import fishing.shared.generated.resources.time
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DateTimePickerField(
    modifier: Modifier = Modifier,
    dateTime: Long,
    onDateTimeChange: (Long) -> Unit,
    minDate: Long? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val clampedChange: (Long) -> Unit = { newDate ->
        onDateTimeChange(ValidationUtils.clampDate(newDate))
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = dateTime,
            minDate = minDate ?: (Clock.System.now().toEpochMilliseconds() - (TimeConstants.MILLISECONDS_IN_DAY * 5)),
            onDateChange = clampedChange,
            onDismiss = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        val localDateTime = Instant.fromEpochMilliseconds(dateTime)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        TimePickerDialog(
            initialHour = localDateTime.hour,
            initialMinute = localDateTime.minute,
            onTimeSelected = { hour, minute ->
                val updated = LocalDateTime(
                    localDateTime.year, localDateTime.month, localDateTime.day,
                    hour, minute, localDateTime.second, localDateTime.nanosecond
                ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                clampedChange(updated)
            },
            onDismiss = { showTimePicker = false },
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1.3f)
                .clickable { showDatePicker = true },
            color = FishingTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_event_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = FishingTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(Res.string.date),
                        style = FishingTheme.typography.bodySmall,
                        color = FishingTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateTime.toDate(),
                        style = FishingTheme.typography.bodyLarge,
                        color = FishingTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .weight(0.7f)
                .clickable { showTimePicker = true },
            color = FishingTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_access_time_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = FishingTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(Res.string.time),
                        style = FishingTheme.typography.bodySmall,
                        color = FishingTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateTime.toTime(),
                        style = FishingTheme.typography.bodyLarge,
                        color = FishingTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
