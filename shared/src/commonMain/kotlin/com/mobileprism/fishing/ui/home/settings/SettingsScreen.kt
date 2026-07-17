package com.mobileprism.fishing.ui.home.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.advertising.rememberPrivacyOptionsLauncher
import com.mobileprism.fishing.ui.home.map.LocationPermissionDialog
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import com.mobileprism.fishing.ui.home.views.AppLargeTopBar
import com.mobileprism.fishing.ui.home.views.BannerTone
import com.mobileprism.fishing.ui.home.views.ColorSwatchRow
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.ExpandableSettingsSection
import com.mobileprism.fishing.ui.home.views.InlineBannerCard
import com.mobileprism.fishing.ui.home.views.ModalLoadingDialog
import com.mobileprism.fishing.ui.home.views.SettingsDivider
import com.mobileprism.fishing.ui.home.views.SettingsGroup
import com.mobileprism.fishing.ui.home.views.SettingsNavLink
import com.mobileprism.fishing.ui.home.views.SettingsSelectionDialog
import com.mobileprism.fishing.ui.home.views.SettingsSwitch
import com.mobileprism.fishing.ui.viewmodels.DeleteAccountState
import com.mobileprism.fishing.ui.viewmodels.UserViewModel
import com.mobileprism.fishing.ui.home.weather.stringRes
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.utils.enums.DarkModeValues
import com.mobileprism.fishing.ui.utils.isDynamicColorSupported
import com.mobileprism.fishing.ui.utils.rememberAppSettingsOpener
import com.mobileprism.fishing.ui.utils.rememberLocationPermissionGranted
import com.mobileprism.fishing.ui.utils.rememberPermissionsController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(backPress: () -> Unit, navController: NavController) {
    val userPreferences: UserPreferences = koinInject()
    val weatherPreferences: WeatherPreferences = koinInject()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            AppLargeTopBar(
                title = stringResource(Res.string.settings),
                scrollBehavior = scrollBehavior,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = backPress,
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
        ) {
            LocationPermissionBanner(userPreferences)
            GeneralSettingsGroup(userPreferences)
            WeatherSettingsGroup(weatherPreferences)
            AboutSettingsGroup(navController)
            AccountSettingsGroup()
            Spacer(modifier = Modifier.height(Spacing.sm))
        }
    }
}

@Composable
private fun LocationPermissionBanner(userPreferences: UserPreferences) {
    val permissionsController = rememberPermissionsController()
    val locationPermissionGranted by rememberLocationPermissionGranted(permissionsController)
    val shouldShowPermissions by userPreferences.shouldShowLocationPermission.collectAsState(true)
    val openAppSettings = rememberAppSettingsOpener()
    var isPermissionDialogOpen by remember { mutableStateOf(false) }

    if (isPermissionDialogOpen) {
        LocationPermissionDialog(
            userPreferences = userPreferences,
            onCloseCallback = { isPermissionDialogOpen = false },
        )
    }

    if (!locationPermissionGranted) {
        InlineBannerCard(
            tone = if (shouldShowPermissions) BannerTone.Warning else BannerTone.Error,
            icon = Icons.Default.LocationOn,
            title = stringResource(
                if (shouldShowPermissions) {
                    Res.string.location_permission_banner_title
                } else {
                    Res.string.location_permission_blocked_title
                }
            ),
            body = stringResource(
                if (shouldShowPermissions) {
                    Res.string.location_permission_banner_body
                } else {
                    Res.string.location_permission_blocked_body
                }
            ),
            actionLabel = stringResource(
                if (shouldShowPermissions) {
                    Res.string.location_permission_banner_action
                } else {
                    Res.string.goto_app_settings
                }
            ),
            onClick = {
                if (shouldShowPermissions) {
                    isPermissionDialogOpen = true
                } else {
                    openAppSettings()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
        )
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
        SettingsSelectionDialog(
            title = stringResource(Res.string.dark_mode),
            options = DarkModeValues.entries.toList(),
            currentValue = darkMode,
            label = { stringResource(it.titleRes) },
            onSelect = { value ->
                coroutineScope.launch { userPreferences.saveDarkMode(value) }
                isDarkModeDialogOpen = false
            },
            onDismiss = { isDarkModeDialogOpen = false },
        )
    }

    val themeEntries = if (isDynamicColorSupported()) {
        AppThemeValues.entries.toList()
    } else {
        AppThemeValues.entries.filter { it != AppThemeValues.Dynamic }
    }

    SettingsGroup(title = stringResource(Res.string.settings_main)) {
        ExpandableSettingsSection(
            title = stringResource(Res.string.app_theme),
            subtitle = stringResource(appTheme.titleRes),
            icon = Icons.Default.ColorLens,
            expanded = isAppThemeExpanded,
            onToggle = { isAppThemeExpanded = !isAppThemeExpanded },
        ) {
            ColorSwatchRow(
                options = themeEntries,
                selected = appTheme,
                colorOf = { it.color },
                contentDescriptionOf = { stringResource(it.titleRes) },
                onSelect = { theme ->
                    coroutineScope.launch { userPreferences.saveAppTheme(theme) }
                },
            )
        }
        SettingsDivider()
        SettingsNavLink(
            title = stringResource(Res.string.dark_mode),
            subtitle = stringResource(darkMode.titleRes),
            icon = Icons.Default.DarkMode,
            onClick = { isDarkModeDialogOpen = true },
        )
        SettingsDivider()
        SettingsSwitch(
            title = stringResource(Res.string.time_format),
            subtitle = stringResource(Res.string.use_12h),
            icon = Icons.Default.AccessTime,
            checked = use12hTimeFormat,
            onCheckedChange = { use12h ->
                coroutineScope.launch { userPreferences.saveTimeFormatStatus(use12h) }
            },
        )
        SettingsDivider()
        SettingsSwitch(
            title = stringResource(Res.string.map_zoom_buttons),
            subtitle = stringResource(Res.string.map_zoom_buttons_description),
            icon = Icons.Default.ZoomIn,
            checked = useZoomButtons,
            onCheckedChange = { value ->
                coroutineScope.launch { userPreferences.saveMapZoomButtons(value) }
            },
        )
        SettingsDivider()
        SettingsSwitch(
            title = stringResource(Res.string.fab_fast_add),
            subtitle = stringResource(Res.string.fast_fab_description),
            icon = Icons.Default.LocationCity,
            checked = useFastFabAdd,
            onCheckedChange = { value ->
                coroutineScope.launch { userPreferences.saveFabFastAdd(value) }
            },
        )
    }
}

@Composable
private fun WeatherSettingsGroup(weatherPreferences: WeatherPreferences) {
    val coroutineScope = rememberCoroutineScope()

    var isPressureDialogOpen by remember { mutableStateOf(false) }
    var isTemperatureDialogOpen by remember { mutableStateOf(false) }
    var isWindSpeedDialogOpen by remember { mutableStateOf(false) }

    val temperatureUnit by weatherPreferences.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val pressureUnit by weatherPreferences.getPressureUnit.collectAsState(PressureValues.mmHg)
    val windSpeedUnit by weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

    if (isTemperatureDialogOpen) {
        SettingsSelectionDialog(
            title = stringResource(Res.string.choose_temperature_unit),
            options = TemperatureValues.entries.toList(),
            currentValue = temperatureUnit,
            label = { stringResource(it.stringRes) },
            onSelect = { value ->
                coroutineScope.launch { weatherPreferences.saveTemperatureUnit(value) }
                isTemperatureDialogOpen = false
            },
            onDismiss = { isTemperatureDialogOpen = false },
        )
    }
    if (isPressureDialogOpen) {
        SettingsSelectionDialog(
            title = stringResource(Res.string.choose_pressure_unit),
            options = PressureValues.entries.toList(),
            currentValue = pressureUnit,
            label = { stringResource(it.stringRes) },
            onSelect = { value ->
                coroutineScope.launch { weatherPreferences.savePressureUnit(value) }
                isPressureDialogOpen = false
            },
            onDismiss = { isPressureDialogOpen = false },
        )
    }
    if (isWindSpeedDialogOpen) {
        SettingsSelectionDialog(
            title = stringResource(Res.string.choose_wind_speed_unit),
            options = WindSpeedValues.entries.toList(),
            currentValue = windSpeedUnit,
            label = { stringResource(it.stringRes) },
            onSelect = { value ->
                coroutineScope.launch { weatherPreferences.saveWindSpeedUnit(value) }
                isWindSpeedDialogOpen = false
            },
            onDismiss = { isWindSpeedDialogOpen = false },
        )
    }

    SettingsGroup(title = stringResource(Res.string.settings_weather)) {
        SettingsNavLink(
            title = stringResource(Res.string.temperature_unit),
            subtitle = stringResource(temperatureUnit.stringRes),
            icon = Icons.Default.Thermostat,
            onClick = { isTemperatureDialogOpen = true },
        )
        SettingsDivider()
        SettingsNavLink(
            title = stringResource(Res.string.pressure_unit),
            subtitle = stringResource(pressureUnit.stringRes),
            icon = Icons.Default.Compress,
            onClick = { isPressureDialogOpen = true },
        )
        SettingsDivider()
        SettingsNavLink(
            title = stringResource(Res.string.wind_speed_unit),
            subtitle = stringResource(windSpeedUnit.stringRes),
            icon = Icons.Default.Air,
            onClick = { isWindSpeedDialogOpen = true },
        )
    }
}

@Composable
private fun AboutSettingsGroup(navController: NavController) {
    val uriHandler = LocalUriHandler.current
    SettingsGroup(title = stringResource(Res.string.settings_about)) {
        SettingsNavLink(
            title = stringResource(Res.string.about_this_app),
            icon = Icons.Default.Info,
            onClick = { navController.navigate(MainDestinations.AboutApp) },
        )
        SettingsDivider()
        SettingsNavLink(
            title = stringResource(Res.string.privacy_policy),
            icon = Icons.Default.PrivacyTip,
            onClick = { uriHandler.openUri("https://merkost.github.io/FishingNotes/privacy.html") },
        )
        SettingsDivider()
        SettingsNavLink(
            title = stringResource(Res.string.terms_of_service),
            icon = Icons.Default.Description,
            onClick = { uriHandler.openUri("https://merkost.github.io/FishingNotes/terms.html") },
        )
        rememberPrivacyOptionsLauncher()?.let { showPrivacyOptions ->
            SettingsDivider()
            SettingsNavLink(
                title = stringResource(Res.string.ads_privacy_options),
                icon = Icons.Default.AdsClick,
                onClick = showPrivacyOptions,
            )
        }
    }
}

@Composable
private fun AccountSettingsGroup() {
    val viewModel = koinViewModel<UserViewModel>()
    val deleteAccountState by viewModel.deleteAccountState.collectAsState()
    var isConfirmDialogOpen by remember { mutableStateOf(false) }

    if (isConfirmDialogOpen) {
        DefaultDialog(
            primaryText = stringResource(Res.string.delete_account_dialog_title),
            secondaryText = stringResource(Res.string.delete_account_dialog_message),
            negativeButtonText = stringResource(Res.string.cancel),
            onNegativeClick = { isConfirmDialogOpen = false },
            positiveButtonText = stringResource(Res.string.delete),
            onPositiveClick = {
                isConfirmDialogOpen = false
                viewModel.deleteAccount()
            },
            onDismiss = { isConfirmDialogOpen = false },
        )
    }

    if (deleteAccountState is DeleteAccountState.ReauthRequired) {
        DefaultDialog(
            primaryText = stringResource(Res.string.delete_account_reauth_title),
            secondaryText = stringResource(Res.string.delete_account_reauth_message),
            negativeButtonText = stringResource(Res.string.cancel),
            onNegativeClick = { viewModel.cancelDeleteAccount() },
            onDismiss = { viewModel.cancelDeleteAccount() },
            content = {
                GoogleButtonUiContainer(
                    modifier = Modifier.fillMaxWidth(),
                    onGoogleSignInResult = { googleUser ->
                        val idToken = googleUser?.idToken
                        if (idToken != null) {
                            viewModel.reauthenticateAndDeleteAccount(idToken)
                        } else {
                            viewModel.cancelDeleteAccount()
                        }
                    },
                ) {
                    AppButton(
                        text = stringResource(Res.string.sign_with_google),
                        onClick = { this@GoogleButtonUiContainer.onClick() },
                        style = AppButtonStyle.Filled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )
    }

    ModalLoadingDialog(
        visible = deleteAccountState is DeleteAccountState.InProgress,
        text = stringResource(Res.string.delete_account_deleting),
    )

    SettingsGroup(title = stringResource(Res.string.settings_account)) {
        SettingsNavLink(
            title = stringResource(Res.string.delete_account),
            subtitle = stringResource(Res.string.delete_account_subtitle),
            icon = Icons.Default.DeleteForever,
            onClick = { isConfirmDialogOpen = true },
        )
    }
}
