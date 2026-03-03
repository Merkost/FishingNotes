package com.mobileprism.fishing.ui.home.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.mobileprism.fishing.ui.utils.AnimatedResource
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
import com.mobileprism.fishing.ui.home.views.BigText
import com.mobileprism.fishing.ui.home.views.PrimaryText
import com.mobileprism.fishing.ui.home.views.SecondaryText
import com.mobileprism.fishing.utils.time.calculateDaylightTime
import com.mobileprism.fishing.utils.time.toTime
import org.koin.compose.koinInject

@Composable
fun PrimaryWeatherItemView(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    temperature: Float,
    textTint: Color = MaterialTheme.colorScheme.tertiary,
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

            PrimaryText(
                modifier = childModifier
                    .width(150.dp)
                    .padding(top = 4.dp),
                text = weather.description.replaceFirstChar { it.uppercase() },
                textColor = textTint,
                textAlign = TextAlign.Center
            )
        }

        BigText(
            modifier = childModifier.padding(bottom = 8.dp),
            text = temperatureUnit.getTemperature(temperature)
                    + stringResource(temperatureUnit.stringRes),
            textColor = textTint
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
        style = MaterialTheme.typography.titleLarge,
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
    color: Color = MaterialTheme.colorScheme.onSurface,
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
    color: Color = MaterialTheme.colorScheme.onSurface,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = color
    )
}

@Composable
fun WeatherPrimaryText(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        color = textColor,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
fun WeatherLoading(modifier: Modifier) {
    AnimatedResource("empty_status", modifier)
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
                SecondaryText(text = label)
                PrimaryText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = temperatureUnit.getTemperature(value)
                            + stringResource(temperatureUnit.stringRes)
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
            PrimaryText(
                modifier = Modifier.padding(top = 8.dp),
                text = sunrise.toTime(is12hTimeFormat)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SecondaryText(text = stringResource(Res.string.daylight_hours))
            PrimaryText(
                modifier = Modifier.padding(top = 8.dp),
                text = calculateDaylightTime(
                    sunrise = sunrise,
                    sunset = sunset,
                    hoursLabel = stringResource(Res.string.hours),
                    minutesLabel = stringResource(Res.string.minutes)
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.ic_sunset_svgrepo_com),
                contentDescription = stringResource(Res.string.sunrise_sunset)
            )
            PrimaryText(
                modifier = Modifier.padding(top = 8.dp),
                text = sunset.toTime(is12hTimeFormat)
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp)
    ) {
        // Row 1: Pressure and Wind
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecondaryText(text = stringResource(Res.string.pressure))
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.ic_gauge),
                        contentDescription = stringResource(Res.string.pressure),
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary)
                    )
                    PrimaryText(
                        modifier = Modifier.padding(start = 2.dp),
                        text = pressureUnit.getPressureFromHpa(
                            forecast.pressure) + " " + stringResource(pressureUnit.stringRes),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecondaryText(text = stringResource(Res.string.wind))
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.ic_wind),
                        contentDescription = stringResource(Res.string.wind),
                    )
                    PrimaryText(
                        modifier = Modifier.padding(start = 2.dp),
                        text = windSpeedUnit.getDefaultWindSpeed(forecast.windSpeed.toDouble())
                                + " " + stringResource(windSpeedUnit.stringRes),
                    )
                    Icon(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .rotate(forecast.windDeg.toFloat()),
                        painter = painterResource(Res.drawable.ic_baseline_navigation_24),
                        contentDescription = stringResource(Res.string.wind),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Row 2: Humidity and Precipitation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecondaryText(text = stringResource(Res.string.humidity))
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.ic_baseline_opacity_24),
                        contentDescription = stringResource(Res.string.humidity),
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary)
                    )
                    PrimaryText(
                        modifier = Modifier.padding(start = 2.dp),
                        text = forecast.humidity.toString() + " " + stringResource(Res.string.percent)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecondaryText(text = stringResource(Res.string.precipitation))
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.ic_baseline_umbrella_24),
                        contentDescription = stringResource(Res.string.precipitation),
                    )
                    PrimaryText(
                        modifier = Modifier.padding(start = 2.dp),
                        text = (forecast.probabilityOfPrecipitation * 100).toInt().toString()
                                + " " + stringResource(Res.string.percent)
                    )
                }
            }
        }
    }
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
        SecondaryText(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = stringResource(Res.string.moon_phase)
        )
        Icon(
            modifier = Modifier
                .size(32.dp)
                .padding(horizontal = 4.dp),
            painter = painterResource(getMoonIconByPhase(moonPhase)),
            contentDescription = stringResource(Res.string.moon_phase)
        )
        PrimaryText(
            text = (moonPhase * 100).toInt().toString()
                    + " " + stringResource(Res.string.percent)
        )
    }
}










