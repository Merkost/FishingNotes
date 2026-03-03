package com.mobileprism.fishing.ui.home.new_catch.weather

import com.mobileprism.fishing.ui.utils.enums.StringOperation
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Clouds(
    override val stringRes: StringResource,
    override val iconPrefix: String
) : StringOperation, WeatherIconPrefix {
    FewClouds(Res.string.few_clouds, "02"), //11-25%
    ScatteredClouds(Res.string.scattered_clouds, "03"), //25-50%
    BrokenClouds(Res.string.broken_clouds, "04"), //51-84%
    OvercastClouds(Res.string.overcast_clouds, "04"); //85-100%

    override val getNameRes: StringResource = Res.string.broken_clouds
}
