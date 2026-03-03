package com.mobileprism.fishing.ui.home.new_catch.weather

import com.mobileprism.fishing.ui.utils.enums.StringOperation
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Atmosphere(
    override val stringRes: StringResource,
    override val iconPrefix: String = "50",
) : StringOperation, WeatherIconPrefix {
    Mist(Res.string.mist),
    Smoke(Res.string.smoke),
    Haze(Res.string.haze),
    SandOrDustWhirls(Res.string.sand_or_dust_whirls),
    Fog(Res.string.fog),
    Sand(Res.string.sand),
    Dust(Res.string.dust),
    VolcanicAsh(Res.string.volcanic_ash),
    Squalls(Res.string.squalls),
    Tornado(Res.string.tornado);

    override val getNameRes: StringResource = Res.string.atmosphere
}
