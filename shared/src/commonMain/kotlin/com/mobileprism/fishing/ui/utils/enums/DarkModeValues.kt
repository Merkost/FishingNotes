package com.mobileprism.fishing.ui.utils.enums

import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class DarkModeValues(val titleRes: StringResource) {
    System(Res.string.dark_mode_system),
    Light(Res.string.dark_mode_light),
    Dark(Res.string.dark_mode_dark);
}
