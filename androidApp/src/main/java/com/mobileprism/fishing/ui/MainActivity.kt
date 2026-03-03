package com.mobileprism.fishing.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.MobileAds.setAppMuted
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.theme.FishingNotesTheme
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.viewmodels.MainViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : FragmentActivity() {

    private val appUpdateManager: AppUpdateManager by inject()
    private val analyticsTracker: AnalyticsTracker by inject()

    private lateinit var installStateUpdatedListener: InstallStateUpdatedListener

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_CANCELED -> {
                SnackbarManager.showMessage(Res.string.update_canceled)
            }
            Activity.RESULT_OK -> {
                SnackbarManager.showMessage(Res.string.update_downloading)
            }
            else -> {
                SnackbarManager.showMessage(Res.string.update_failed)
                checkForUpdates()
            }
        }
    }

    companion object {
        const val splashFadeDurationMillis = 300

        val TAG = MainActivity::class.java.simpleName

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        val viewModel: MainViewModel by viewModel()
        val userStateFlow = viewModel.userState
        val userPreferences: UserPreferences by inject()
        val appTheme = mutableStateOf<AppThemeValues?>(null)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userPreferences.appTheme.collect { appTheme.value = it }
            }
        }

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                userStateFlow.value is BaseViewState.Loading
                        || appTheme.value == null
            }
            setOnExitAnimationListener { splashScreenViewProvider ->
                // Get icon instance and start a fade out animation
                if (Build.VERSION.SDK_INT >= 31) {
                    splashScreenViewProvider.view
                        .animate()
                        .setDuration(splashFadeDurationMillis.toLong())
                        .alpha(0f)
                        .start()
                }

                splashScreenViewProvider.iconView
                    .animate()
                    .setDuration(splashFadeDurationMillis.toLong())
                    .alpha(0f)
                    /*.scaleX(50f)
                    .scaleY(50f)*/
                    .withEndAction {
                        splashScreenViewProvider.remove()
                        if (Build.VERSION.SDK_INT < 31) {
                            setContent {
                                CompositionLocalProvider(
                                    LocalAnalytics provides analyticsTracker
                                ) {
                                    FishingNotesTheme(appTheme.value) {
                                        DistributionScreen(viewModel)
                                    }
                                }
                            }
                        }
                    }
                    .start()
            }
        }

        if (Build.VERSION.SDK_INT >= 31) {
            setContent {
                CompositionLocalProvider(
                    LocalAnalytics provides analyticsTracker
                ) {
                    FishingNotesTheme(appTheme.value) {
                        DistributionScreen(viewModel)
                    }
                }
            }
        }

        MobileAds.initialize(this)
        setAppMuted(true)

        checkForUpdates()
    }

    private fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        installStateUpdatedListener = createUpdateListener()

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // Before starting an update, register a listener for updates.
                appUpdateManager.registerListener(installStateUpdatedListener)

                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }.addOnFailureListener {
            //SnackbarManager.showMessage(Res.string.error_occured)
        }
    }

    // Create a listener to track request state updates.
    private fun createUpdateListener() =
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
                    // Show update progress bar.
                }

                InstallStatus.DOWNLOADED -> {
                    popupSnackbarForCompleteUpdate()
                }

                InstallStatus.INSTALLED -> {
                    SnackbarManager.showMessage(Res.string.update_installed)
                    removeInstallStateUpdateListener()
                }

                InstallStatus.CANCELED -> {
                    SnackbarManager.showMessage(Res.string.update_canceled)
                }

                InstallStatus.FAILED -> {
                    SnackbarManager.showMessage(Res.string.update_failed)
                }

                else -> {}
            }
        }

    private fun removeInstallStateUpdateListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private fun popupSnackbarForCompleteUpdate() {
        SnackbarManager.showMessage(
            Res.string.update_ready,
            SnackbarAction(Res.string.reload_app) { appUpdateManager.completeUpdate() },
            duration = SnackbarDuration.Indefinite
        )
    }

    @Composable
    private fun DistributionScreen(viewModel: MainViewModel) {
        val userState by viewModel.userState.collectAsState()
        when (val state = userState) {
            is BaseViewState.Loading -> { /* splash screen is still visible */ }
            is BaseViewState.Success -> {
                if (state.data != null) FishingNotesApp()
                else Navigation()
            }
            is BaseViewState.Error -> Navigation()
        }
    }

    @Composable
    fun Navigation() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = LoginRoute
        ) {
            composable<LoginRoute> {
                LoginScreen(navController = navController)
            }
            composable<HomeGraph> {
                FishingNotesApp()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        removeInstallStateUpdateListener()
    }
}
