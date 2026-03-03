package com.mobileprism.fishing.ui.home.new_catch.weather

import com.mobileprism.fishing.ui.utils.enums.StringOperation
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Drizzle(
    override val stringRes: StringResource,
    override val iconPrefix: String = "09"
) : StringOperation, WeatherIconPrefix {
    LightIntensityDrizzle(Res.string.light_intensity_drizzle),
    JustDrizzle(Res.string.drizzle),
    HeavyIntensityDrizzle(Res.string.heavy_intensity_drizzle),
    LightIntensityDrizzleRain(Res.string.light_intensity_drizzle_rain),
    DrizzleRain(Res.string.drizzle_rain),
    HeavyIntensityDrizzleRain(Res.string.heavy_intensity_drizzle_rain),
    ShowerRainAndDrizzle(Res.string.shower_rain_and_drizzle),
    HeavyShowerRainAndDrizzle(Res.string.heavy_shower_rain_and_drizzle),
    ShowerDrizzle(Res.string.shower_drizzle);

    override val getNameRes: StringResource = Res.string.drizzle
}
