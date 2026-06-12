package com.mobileprism.fishing.ui.utils.format

import androidx.compose.runtime.Composable
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.value_with_unit
import fishing.shared.generated.resources.value_with_unit_compact
import org.jetbrains.compose.resources.stringResource

object UnitFormatter {

    @Composable
    fun valueWithUnit(value: String, unit: String): String =
        stringResource(Res.string.value_with_unit, value, unit)

    @Composable
    fun valueWithUnitCompact(value: String, unit: String): String =
        stringResource(Res.string.value_with_unit_compact, value, unit)
}
