package com.mobileprism.fishing.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.MobileAds.setAppMuted
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.mobileprism.fishing.BuildInfo
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.advertising.AdsConsentManager
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.theme.FishingNotesTheme
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.viewmodels.MainViewModel
import com.mobileprism.fishing.viewmodels.RoutingDecision
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
        val userPreferences: UserPreferences by inject()
        val appTheme = mutableStateOf<AppThemeValues?>(null)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userPreferences.appTheme.collect { appTheme.value = it }
            }
        }

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.routing.value == RoutingDecision.Splash
                        || appTheme.value == null
            }
            setOnExitAnimationListener { splashScreenViewProvider ->
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
                    .withEndAction {
                        splashScreenViewProvider.remove()
                        if (Build.VERSION.SDK_INT < 31) {
                            setContent {
                                CompositionLocalProvider(
                                    LocalAnalytics provides analyticsTracker
                                ) {
                                    FishingNotesTheme(appTheme.value) {
                                        FishingNotesApp()
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
                        FishingNotesApp()
                    }
                }
            }
        }

        val buildInfo: BuildInfo by inject()
        AdsConsentManager.gatherConsent(this, debugGeographyEea = buildInfo.isDebug) {
            initializeMobileAds()
        }

        checkForUpdates()
    }

    private fun initializeMobileAds() {
        MobileAds.initialize(this)
        setAppMuted(true)
    }

    private fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        installStateUpdatedListener = createUpdateListener()

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
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
        }
    }

    private fun createUpdateListener() =
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
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

    override fun onStop() {
        super.onStop()
        removeInstallStateUpdateListener()
    }
}
