package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.month_apr
import fishing.shared.generated.resources.month_aug
import fishing.shared.generated.resources.month_dec
import fishing.shared.generated.resources.month_feb
import fishing.shared.generated.resources.month_jan
import fishing.shared.generated.resources.month_jul
import fishing.shared.generated.resources.month_jun
import fishing.shared.generated.resources.month_mar
import fishing.shared.generated.resources.month_may
import fishing.shared.generated.resources.month_nov
import fishing.shared.generated.resources.month_oct
import fishing.shared.generated.resources.month_sep
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val monthResources: List<StringResource> = listOf(
    Res.string.month_jan, Res.string.month_feb, Res.string.month_mar,
    Res.string.month_apr, Res.string.month_may, Res.string.month_jun,
    Res.string.month_jul, Res.string.month_aug, Res.string.month_sep,
    Res.string.month_oct, Res.string.month_nov, Res.string.month_dec,
)

fun monthShortLabelResource(yearMonthKey: String): StringResource? {
    val parts = yearMonthKey.split("-")
    if (parts.size != 2) return null
    val month = parts[1].toIntOrNull() ?: return null
    return monthResources.getOrNull(month - 1)
}

@Composable
fun monthShortLabel(yearMonthKey: String): String {
    val resource = monthShortLabelResource(yearMonthKey) ?: return yearMonthKey
    return stringResource(resource)
}
