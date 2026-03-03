package com.mobileprism.fishing.ui.home.new_catch.weather

import com.mobileprism.fishing.ui.utils.enums.StringOperation
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Thunderstorm(
    override val stringRes: StringResource,
    override val iconPrefix: String = "11"
) : StringOperation, WeatherIconPrefix {
    ThunderstormWithLightRain(Res.string.thunderstorm_with_light_rain),
    ThunderstormWithRain(Res.string.thunderstorm_with_rain),
    ThunderstormWithHeavyRain(Res.string.thunderstorm_with_heavy_rain),
    LightThunderstorm(Res.string.light_thunderstorm),
    JustThunderstorm(Res.string.thunderstorm),
    HeavyThunderstorm(Res.string.heavy_thunderstorm),
    RaggedThunderstorm(Res.string.ragged_thunderstorm),
    ThunderstormWithLightDrizzle(Res.string.thunderstorm_with_light_drizzle),
    ThunderstormWithDrizzle(Res.string.thunderstorm_with_drizzle),
    ThunderstormWithHeavyDrizzle(Res.string.thunderstorm_with_heavy_drizzle);

    override val getNameRes: StringResource = Res.string.thunderstorm
}
