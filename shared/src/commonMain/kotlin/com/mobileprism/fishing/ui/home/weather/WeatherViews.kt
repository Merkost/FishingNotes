package com.mobileprism.fishing.ui.home.weather

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.domain.entity.weather.Temperature
import com.mobileprism.fishing.domain.entity.weather.Weather
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.mappers.getMoonIconByPhase
import com.mobileprism.fishing.model.mappers.getWeatherIconByName
import com.mobileprism.fishing.ui.home.views.AppText
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.home.views.WeatherMetric
import com.mobileprism.fishing.ui.home.views.WeatherStatGrid
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.utils.time.calculateDaylightTime
import com.mobileprism.fishing.utils.time.toTime
import org.koin.compose.koinInject

@Composable
fun PrimaryWeatherItemView(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    temperature: Float,
    textTint: Color = FishingTheme.colorScheme.tertiary,
    iconTint: Color = Color.Unspecified,
    temperatureUnit: TemperatureValues,
    weather: Weather
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = childModifier
                    .size(64.dp)
                    .padding(top = 8.dp),
                painter = painterResource(getWeatherIconByName(weather.icon)),
                contentDescription = stringResource(Res.string.weather),
                tint = iconTint
            )

            AppText(
                text = weather.description.replaceFirstChar { it.uppercase() },
                modifier = childModifier
                    .width(150.dp)
                    .padding(top = 4.dp),
                style = AppTextStyle.Title,
                color = textTint,
                textAlign = TextAlign.Center
            )
        }

        AppText(
            text = temperatureText(temperatureUnit, temperature),
            modifier = childModifier.padding(bottom = 8.dp),
            style = AppTextStyle.Display,
            color = textTint
        )
    }
}

@Composable
fun WeatherAppBarText(
    modifier: Modifier = Modifier,
    textColor: Color,
    text: String
) {
    Text(
        modifier = modifier.padding(horizontal = 4.dp),
        style = FishingTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = textColor,
        maxLines = 1,
        softWrap = true,
        text = text,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun WeatherLocationIconButton(
    color: Color = FishingTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            painter = painterResource(Res.drawable.ic_baseline_location_on_24),
            tint = color,
            contentDescription = stringResource(Res.string.select_location)
        )
    }

}

@Composable
fun WeatherHeaderText(
    modifier: Modifier = Modifier,
    color: Color = FishingTheme.colorScheme.onSurface,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        style = FishingTheme.typography.bodyLarge,
        color = color
    )
}

@Composable
fun WeatherPrimaryText(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = FishingTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        color = textColor,
        style = FishingTheme.typography.titleLarge
    )
}

@Composable
fun DayTemperatureView(
    modifier: Modifier = Modifier,
    temperature: Temperature,
    temperatureUnit: TemperatureValues
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        @Composable
        fun TemperatureColumn(label: String, value: Float) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppText(
                    text = label,
                    style = AppTextStyle.Body,
                    color = FishingTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                AppText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = temperatureText(temperatureUnit, value),
                    style = AppTextStyle.Title
                )
            }
        }
        TemperatureColumn(stringResource(Res.string.morning), temperature.morning)
        TemperatureColumn(stringResource(Res.string.day), temperature.day)
        TemperatureColumn(stringResource(Res.string.Evening), temperature.evening)
        TemperatureColumn(stringResource(Res.string.night), temperature.night)
    }
}

@Composable
fun SunriseSunsetView(
    modifier: Modifier = Modifier,
    sunrise: Long,
    sunset: Long,
) {

    val preferences: UserPreferences = koinInject()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp, start = 32.dp, end = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.ic_sunrise_morning_svgrepo_com),
                contentDescription = stringResource(Res.string.sunrise_sunset)
            )
            AppText(
                modifier = Modifier.padding(top = 8.dp),
                text = sunrise.toTime(is12hTimeFormat),
                style = AppTextStyle.Title
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppText(
                text = stringResource(Res.string.daylight_hours),
                style = AppTextStyle.Body,
                color = FishingTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            AppText(
                modifier = Modifier.padding(top = 8.dp),
                text = calculateDaylightTime(
                    sunrise = sunrise,
                    sunset = sunset,
                    hoursLabel = stringResource(Res.string.hours),
                    minutesLabel = stringResource(Res.string.minutes)
                ),
                style = AppTextStyle.Title
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.ic_sunset_svgrepo_com),
                contentDescription = stringResource(Res.string.sunrise_sunset)
            )
            AppText(
                modifier = Modifier.padding(top = 8.dp),
                text = sunset.toTime(is12hTimeFormat),
                style = AppTextStyle.Title
            )
        }
    }
}

@Composable
fun DailyWeatherValuesView(
    modifier: Modifier = Modifier,
    forecast: Daily,
    pressureUnit: PressureValues,
    windSpeedUnit: WindSpeedValues
) {
    val metrics: List<@Composable () -> Unit> = listOf(
        {
            WeatherMetric(
                label = stringResource(Res.string.pressure),
                icon = Res.drawable.ic_gauge,
                value = pressureText(pressureUnit, forecast.pressure),
                iconTint = FishingTheme.colorScheme.tertiary,
            )
        },
        {
            WeatherMetric(
                label = stringResource(Res.string.wind),
                icon = Res.drawable.ic_wind,
                value = windSpeedText(windSpeedUnit, forecast.windSpeed.toDouble()),
                iconTint = FishingTheme.colorScheme.tertiary,
            )
        },
        {
            WeatherMetric(
                label = stringResource(Res.string.humidity),
                icon = Res.drawable.ic_baseline_opacity_24,
                value = percentText(forecast.humidity),
                iconTint = FishingTheme.colorScheme.tertiary,
            )
        },
        {
            WeatherMetric(
                label = stringResource(Res.string.precipitation),
                icon = Res.drawable.ic_baseline_umbrella_24,
                value = percentText(probabilityToPercent(forecast.probabilityOfPrecipitation)),
                iconTint = FishingTheme.colorScheme.tertiary,
            )
        },
    )
    WeatherStatGrid(
        modifier = modifier.fillMaxWidth(),
        metrics = metrics,
    )
}

@Composable
fun MoonPhaseView(
    modifier: Modifier = Modifier,
    moonPhase: Float
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AppText(
            text = stringResource(Res.string.moon_phase),
            modifier = Modifier.padding(horizontal = 8.dp),
            style = AppTextStyle.Body,
            color = FishingTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Icon(
            modifier = Modifier
                .size(32.dp)
                .padding(horizontal = 4.dp),
            painter = painterResource(getMoonIconByPhase(moonPhase)),
            contentDescription = stringResource(Res.string.moon_phase)
        )
        AppText(
            text = stringResource(moonPhaseStringRes(moonPhaseName(moonPhase))),
            style = AppTextStyle.Title
        )
    }
}
