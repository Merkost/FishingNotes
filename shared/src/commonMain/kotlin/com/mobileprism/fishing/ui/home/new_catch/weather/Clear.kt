package com.mobileprism.fishing.ui.home.new_catch.weather

import com.mobileprism.fishing.ui.utils.enums.StringOperation
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Clear(
    override val stringRes: StringResource,
    override val iconPrefix: String = "01"
) : StringOperation, WeatherIconPrefix {
    ClearSky(Res.string.clear_sky);

    override val getNameRes: StringResource = Res.string.clear_sky
}
