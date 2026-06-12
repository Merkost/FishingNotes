package com.mobileprism.fishing.ui.utils.format

import androidx.compose.runtime.Composable
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.month_short_1
import fishing.shared.generated.resources.month_short_10
import fishing.shared.generated.resources.month_short_11
import fishing.shared.generated.resources.month_short_12
import fishing.shared.generated.resources.month_short_2
import fishing.shared.generated.resources.month_short_3
import fishing.shared.generated.resources.month_short_4
import fishing.shared.generated.resources.month_short_5
import fishing.shared.generated.resources.month_short_6
import fishing.shared.generated.resources.month_short_7
import fishing.shared.generated.resources.month_short_8
import fishing.shared.generated.resources.month_short_9
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object DateLabels {

    fun monthNumberFromKey(key: String): Int? {
        val parts = key.split("-")
        if (parts.size != 2) return null
        val month = parts[1].toIntOrNull() ?: return null
        return if (month in 1..12) month else null
    }

    private val shortMonthResources: List<StringResource> = listOf(
        Res.string.month_short_1, Res.string.month_short_2, Res.string.month_short_3,
        Res.string.month_short_4, Res.string.month_short_5, Res.string.month_short_6,
        Res.string.month_short_7, Res.string.month_short_8, Res.string.month_short_9,
        Res.string.month_short_10, Res.string.month_short_11, Res.string.month_short_12,
    )

    @Composable
    fun shortMonthFromKey(key: String): String {
        val month = monthNumberFromKey(key) ?: return key
        return stringResource(shortMonthResources[month - 1])
    }
}
