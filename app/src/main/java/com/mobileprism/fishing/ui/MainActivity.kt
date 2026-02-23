package com.mobileprism.fishing.ui

import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.MobileAds.setAppMuted
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.theme.FishingNotesTheme
import com.mobileprism.fishing.ui.utils.LocalAnalytics
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.Logger
import com.mobileprism.fishing.viewmodels.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

    private val logger: Logger by inject()
    private val appUpdateManager: AppUpdateManager by inject()
    private val auth: FirebaseAuth by inject()
    private val analyticsTracker: AnalyticsTracker by inject()

    private lateinit var installStateUpdatedListener: InstallStateUpdatedListener

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_CANCELED -> {
                SnackbarManager.showMessage(R.string.update_canceled)
            }
            Activity.RESULT_OK -> {
                SnackbarManager.showMessage(R.string.update_downloading)
            }
            else -> {
                SnackbarManager.showMessage(R.string.update_failed)
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

        lifecycleScope.launchWhenStarted {
            userPreferences.appTheme.collect { appTheme.value = it }
        }

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                userStateFlow.value is BaseViewState.Loading
                        && appTheme.value == null
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
                                CompositionLocalProvider(LocalAnalytics provides analyticsTracker) {
                                    FishingNotesTheme(appTheme.value) {
                                        DistributionScreen((viewModel.userState.value as? BaseViewState.Success)?.data)
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
                CompositionLocalProvider(LocalAnalytics provides analyticsTracker) {
                    FishingNotesTheme(appTheme.value) {
                        DistributionScreen((viewModel.userState.value as? BaseViewState.Success)?.data)
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
            //SnackbarManager.showMessage(R.string.error_occured)
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
                    SnackbarManager.showMessage(R.string.update_installed)
                    removeInstallStateUpdateListener()
                }

                InstallStatus.CANCELED -> {
                    SnackbarManager.showMessage(R.string.update_canceled)
                }

                InstallStatus.FAILED -> {
                    SnackbarManager.showMessage(R.string.update_failed)
                }

                else -> {}
            }
        }

    private fun removeInstallStateUpdateListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private fun popupSnackbarForCompleteUpdate() {
        SnackbarManager.showMessage(
            R.string.update_ready,
            SnackbarAction(R.string.reload_app) { appUpdateManager.completeUpdate() },
            duration = SnackbarDuration.Indefinite
        )
    }

    @Composable
    private fun DistributionScreen(user: User?) {
        if (user != null) FishingNotesApp()
        else Navigation()
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

    suspend fun startGoogleLogin() {
        val credentialManager = CredentialManager.create(this)
        val signInWithGoogleOption: GetSignInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(
                getString(R.string.default_web_client_id)
            ).build()

        val request = androidx.credentials.GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = this,
            )
            handleSignIn(result)
        } catch (e: GetCredentialException) {
            handleError(e)
        }

    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    handleError(Exception("Unexpected type of credential"))
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                handleError(Exception("Unexpected type of credential"))
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                when {
                    task.isSuccessful -> {
                        // Sign in success, update UI with the signed-in user's information
                    }

                    else -> {
                        handleError(task.exception)
                    }
                }
            }
    }

    private fun handleError(error: Exception?) {
        error?.let {
            analyticsTracker.logEvent(AnalyticsEvent.SignInError(error.message))
            logger.log(error.message)
        }
        SnackbarManager.showMessage(R.string.google_login_failed)
    }

    override fun onStop() {
        super.onStop()
        removeInstallStateUpdateListener()
    }
}

/**
 * A composable function that returns the [Resources]. It will be recomposed when `Configuration`
 * gets updated.
 */
@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}
