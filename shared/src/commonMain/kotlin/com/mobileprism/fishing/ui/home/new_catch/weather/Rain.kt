package com.mobileprism.fishing.ui.home.new_catch.weather

import com.mobileprism.fishing.ui.utils.enums.StringOperation
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Rain(
    override val stringRes: StringResource,
    override val iconPrefix: String = "10"
) : StringOperation, WeatherIconPrefix {
    LightRain(Res.string.light_rain),
    ModerateRain(Res.string.rain),
    HeavyIntensityRain(Res.string.heavy_intensity_rain),
    VeryHeavyRain(Res.string.very_heavy_rain),
    ExtremeRain(Res.string.extreme_rain),
    FreezingRain(Res.string.freezing_rain, "13"),
    LightIntensityShowerRain(Res.string.light_intensity_shower_rain, "09"),
    ShowerRain(Res.string.shower_rain, "09"),
    HeavyIntensityShowerRain(Res.string.heavy_intensity_shower_rain, "09"),
    RaggedShowerRain(Res.string.ragged_shower_rain, "09");

    override val getNameRes: StringResource = Res.string.rain
}
