package com.mobileprism.fishing.ui.home.new_catch.weather

import com.mobileprism.fishing.ui.utils.enums.StringOperation
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Snow(
    override val stringRes: StringResource,
    override val iconPrefix: String = "13"
) : StringOperation, WeatherIconPrefix {
    LightSnow(Res.string.light_snow),
    JustSnow(Res.string.snow),
    HeavySnow(Res.string.heavy_snow),
    Sleet(Res.string.sleet),
    LightShowerSleet(Res.string.light_shower_sleet),
    ShowerSleet(Res.string.shower_sleet),
    LightRainAndSnow(Res.string.light_rain_and_snow),
    RainAndSnow(Res.string.rain_and_snow),
    LightShowerSnow(Res.string.light_shower_snow),
    ShowerSnow(Res.string.shower_snow),
    HeavyShowerSnow(Res.string.heavy_shower_snow);

    override val getNameRes: StringResource = Res.string.snow
}
