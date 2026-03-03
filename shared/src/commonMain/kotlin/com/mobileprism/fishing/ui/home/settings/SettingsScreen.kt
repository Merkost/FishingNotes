package com.mobileprism.fishing.ui.home.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.map.LocationPermissionDialog
import com.mobileprism.fishing.ui.utils.rememberLocationPermissionGranted
import com.mobileprism.fishing.ui.utils.rememberPermissionsController
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.ItemsSelection
import com.mobileprism.fishing.ui.home.views.SettingsDivider
import com.mobileprism.fishing.ui.home.views.SettingsGroup
import com.mobileprism.fishing.ui.home.views.SettingsMenuLink
import com.mobileprism.fishing.ui.home.views.SettingsSwitch
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.ui.home.weather.stringRes
import com.mobileprism.fishing.ui.utils.isDynamicColorSupported
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.utils.enums.DarkModeValues
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(backPress: () -> Unit, navController: NavController) {
    val userPreferences: UserPreferences = koinInject()
    val weatherPreferences: WeatherPreferences = koinInject()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = { SettingsTopAppBar(backPress, scrollBehavior) },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LocationPermissionBanner(userPreferences)
            GeneralSettingsGroup(userPreferences)
            WeatherSettingsGroup(weatherPreferences)
            AboutSettingsGroup(navController)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(
    backPress: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        title = { Text(text = stringResource(Res.string.settings)) },
        navigationIcon = {
            IconButton(onClick = backPress) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun LocationPermissionBanner(userPreferences: UserPreferences) {
    val permissionsController = rememberPermissionsController()
    val locationPermissionGranted by rememberLocationPermissionGranted(permissionsController)
    var isPermissionDialogOpen by remember { mutableStateOf(false) }

    if (isPermissionDialogOpen) {
        LocationPermissionDialog(userPreferences = userPreferences) {
            isPermissionDialogOpen = false
        }
    }

    AnimatedVisibility(visible = !locationPermissionGranted) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isPermissionDialogOpen = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.location_permission),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(Res.string.provide_location_permission),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneralSettingsGroup(userPreferences: UserPreferences) {
    val coroutineScope = rememberCoroutineScope()

    val appTheme by userPreferences.appTheme.collectAsState(AppThemeValues.Blue)
    val use12hTimeFormat by userPreferences.use12hTimeFormat.collectAsState(false)
    val useFastFabAdd by userPreferences.useFabFastAdd.collectAsState(false)
    val useZoomButtons by userPreferences.useMapZoomButons.collectAsState(false)
    val darkMode by userPreferences.darkMode.collectAsState(DarkModeValues.System)

    var isAppThemeExpanded by remember { mutableStateOf(false) }
    var isDarkModeDialogOpen by remember { mutableStateOf(false) }

    if (isDarkModeDialogOpen) {
        GetDarkModeDialog(
            currentValue = darkMode,
            onSelected = { value ->
                coroutineScope.launch { userPreferences.saveDarkMode(value) }
                isDarkModeDialogOpen = false
            },
            onDismiss = { isDarkModeDialogOpen = false }
        )
    }

    SettingsGroup(title = stringResource(Res.string.settings_main)) {
        SettingsMenuLink(
            title = stringResource(Res.string.app_theme),
            subtitle = stringResource(appTheme.titleRes),
            icon = Icons.Default.ColorLens,
            onClick = { isAppThemeExpanded = !isAppThemeExpanded }
        )
        AnimatedVisibility(visible = isAppThemeExpanded) {
            val themeEntries = if (isDynamicColorSupported()) {
                AppThemeValues.entries
            } else {
                AppThemeValues.entries.filter { it != AppThemeValues.Dynamic }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                themeEntries.forEach { theme ->
                    ThemeColorCircle(
                        theme = theme,
                        selected = appTheme == theme,
                        onClick = {
                            coroutineScope.launch {
                                userPreferences.saveAppTheme(theme)
                            }
                        }
                    )
                }
            }
        }
        SettingsDivider()
        SettingsMenuLink(
            title = stringResource(Res.string.dark_mode),
            subtitle = stringResource(darkMode.titleRes),
            icon = Icons.Default.DarkMode,
            onClick = { isDarkModeDialogOpen = true }
        )
        SettingsDivider()
        SettingsSwitch(
            title = stringResource(Res.string.time_format),
            subtitle = stringResource(Res.string.use_12h),
            icon = Icons.Default.AccessTime,
            checked = use12hTimeFormat,
            onCheckedChange = { use12h ->
                coroutineScope.launch { userPreferences.saveTimeFormatStatus(use12h) }
            }
        )
        SettingsDivider()
        SettingsSwitch(
            title = stringResource(Res.string.map_zoom_buttons),
            subtitle = stringResource(Res.string.map_zoom_buttons_description),
            icon = Icons.Default.ZoomIn,
            checked = useZoomButtons,
            onCheckedChange = { value ->
                coroutineScope.launch { userPreferences.saveMapZoomButtons(value) }
            }
        )
        SettingsDivider()
        SettingsSwitch(
            title = stringResource(Res.string.fab_fast_add),
            subtitle = stringResource(Res.string.fast_fab_description),
            icon = Icons.Default.LocationCity,
            checked = useFastFabAdd,
            onCheckedChange = { value ->
                coroutineScope.launch { userPreferences.saveFabFastAdd(value) }
            }
        )
    }
}

@Composable
private fun ThemeColorCircle(
    theme: AppThemeValues,
    selected: Boolean,
    onClick: () -> Unit
) {
    val circleModifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .then(
            if (theme.color != null) {
                Modifier.background(theme.color)
            } else {
                Modifier.background(
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFFEF5350),
                            Color(0xFFAB47BC),
                            Color(0xFF42A5F5),
                            Color(0xFF66BB6A),
                            Color(0xFFFFCA28),
                            Color(0xFFEF5350),
                        )
                    )
                )
            }
        )
        .then(
            if (selected) {
                Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            } else {
                Modifier
            }
        )
        .clickable(onClick = onClick)

    Box(
        modifier = circleModifier,
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun WeatherSettingsGroup(weatherPreferences: WeatherPreferences) {
    val isPressureDialogOpen = remember { mutableStateOf(false) }
    val isTemperatureDialogOpen = remember { mutableStateOf(false) }
    val isWindSpeedDialogOpen = remember { mutableStateOf(false) }

    val temperatureUnit by weatherPreferences.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val pressureUnit by weatherPreferences.getPressureUnit.collectAsState(PressureValues.mmHg)
    val windSpeedUnit by weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

    GetPressureUnit(isPressureDialogOpen, weatherPreferences)
    GetTemperatureUnit(isTemperatureDialogOpen, weatherPreferences)
    GetWindSpeedUnit(isWindSpeedDialogOpen, weatherPreferences)

    SettingsGroup(title = stringResource(Res.string.settings_weather)) {
        SettingsMenuLink(
            title = stringResource(Res.string.temperature_unit),
            subtitle = stringResource(temperatureUnit.stringRes),
            icon = Icons.Default.Thermostat,
            onClick = { isTemperatureDialogOpen.value = true }
        )
        SettingsDivider()
        SettingsMenuLink(
            title = stringResource(Res.string.pressure_unit),
            subtitle = stringResource(pressureUnit.stringRes),
            icon = Icons.Default.Compress,
            onClick = { isPressureDialogOpen.value = true }
        )
        SettingsDivider()
        SettingsMenuLink(
            title = stringResource(Res.string.wind_speed_unit),
            subtitle = stringResource(windSpeedUnit.stringRes),
            icon = Icons.Default.Air,
            onClick = { isWindSpeedDialogOpen.value = true }
        )
    }
}

@Composable
private fun AboutSettingsGroup(navController: NavController) {
    SettingsGroup(title = stringResource(Res.string.settings_about)) {
        SettingsMenuLink(
            title = stringResource(Res.string.settings_about),
            icon = Icons.Default.Info,
            onClick = { navController.navigate(MainDestinations.AboutApp) }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GetTemperatureUnit(
    isTemperatureDialogOpen: MutableState<Boolean>,
    weatherPreferences: WeatherPreferences,
) {
    val temperatureUnit =
        weatherPreferences.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val radioOptions = TemperatureValues.values().asList()
    val coroutineScope = rememberCoroutineScope()

    val onSelectedValue: (temperatureValue: TemperatureValues) -> Unit = { newValue ->
        coroutineScope.launch {
            weatherPreferences.saveTemperatureUnit(newValue)
            delay(200)
            isTemperatureDialogOpen.value = false
        }
    }

    if (isTemperatureDialogOpen.value) {
        DefaultDialog(
            primaryText = stringResource(Res.string.choose_temperature_unit),
            onDismiss = { isTemperatureDialogOpen.value = false }) {
            ItemsSelection(
                radioOptions = radioOptions,
                currentOption = temperatureUnit,
                labelProvider = { stringResource(it.stringRes) }
            ) {
                onSelectedValue(it)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GetPressureUnit(
    pressureDialogOpen: MutableState<Boolean>,
    weatherPreferences: WeatherPreferences,
) {
    val pressureUnit = weatherPreferences.getPressureUnit.collectAsState(PressureValues.mmHg)
    val radioOptions = PressureValues.values().asList()
    val coroutineScope = rememberCoroutineScope()

    val onSelectedValue: (pressureUnit: PressureValues) -> Unit = { newValue ->
        coroutineScope.launch {
            weatherPreferences.savePressureUnit(newValue)
            delay(200)
            pressureDialogOpen.value = false
        }
    }

    if (pressureDialogOpen.value) {
        DefaultDialog(
            primaryText = stringResource(Res.string.choose_pressure_unit),
            onDismiss = { pressureDialogOpen.value = false }) {

            ItemsSelection(
                radioOptions = radioOptions,
                currentOption = pressureUnit,
                labelProvider = { stringResource(it.stringRes) }
            ) {
                onSelectedValue(it)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GetWindSpeedUnit(
    isWindSpeedDialogOpen: MutableState<Boolean>,
    weatherPreferences: WeatherPreferences,
) {
    val windSpeedUnit = weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)
    val radioOptions = WindSpeedValues.values().asList()
    val coroutineScope = rememberCoroutineScope()

    val onSelectedValue: (windSpeedValues: WindSpeedValues) -> Unit = { newValue ->
        coroutineScope.launch {
            weatherPreferences.saveWindSpeedUnit(newValue)
            delay(200)
            isWindSpeedDialogOpen.value = false
        }
    }

    if (isWindSpeedDialogOpen.value) {
        DefaultDialog(
            primaryText = stringResource(Res.string.choose_wind_speed_unit),
            onDismiss = { isWindSpeedDialogOpen.value = false }) {
            ItemsSelection(
                radioOptions = radioOptions,
                currentOption = windSpeedUnit,
                labelProvider = { stringResource(it.stringRes) }
            ) {
                onSelectedValue(it)
            }
        }
    }
}

@Composable
private fun GetDarkModeDialog(
    currentValue: DarkModeValues,
    onSelected: (DarkModeValues) -> Unit,
    onDismiss: () -> Unit,
) {
    DefaultDialog(
        primaryText = stringResource(Res.string.dark_mode),
        onDismiss = onDismiss
    ) {
        Column {
            DarkModeValues.entries.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = option == currentValue,
                            onClick = { onSelected(option) }
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = option == currentValue,
                        onClick = { onSelected(option) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(option.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
