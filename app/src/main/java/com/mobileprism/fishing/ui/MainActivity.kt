package com.mobileprism.fishing.ui

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.MobileAds.setAppMuted
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.entity.common.User
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.theme.FishingNotesTheme
import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.Logger
import com.mobileprism.fishing.viewmodels.MainViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.*

class MainActivity : ComponentActivity() {

    private val logger: Logger by inject()
    private val appUpdateManager: AppUpdateManager = get()
    private val auth: FirebaseAuth = get()

    private lateinit var installStateUpdatedListener: InstallStateUpdatedListener
    private lateinit var googleSignInClient: GoogleSignInClient

    private val registeredActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
        }

    companion object {
        const val splashFadeDurationMillis = 300
        const val UPDATE_REQUEST_CODE = 984165687
    }

    @OptIn(
        ExperimentalComposeUiApi::class, ExperimentalPermissionsApi::class,
        ExperimentalAnimationApi::class, InternalCoroutinesApi::class,
        ExperimentalPagerApi::class, ExperimentalMaterialApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainViewModel = getViewModel()

        val userStateFlow = viewModel.mutableStateFlow

        val userPreferences: UserPreferences = get()
        val appTheme = mutableStateOf<AppThemeValues?>(null)

        lifecycleScope.launchWhenStarted {
            userPreferences.appTheme.collect {
                appTheme.value = it
            }
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
                                FishingNotesTheme(appTheme.value) {
                                    DistributionScreen(viewModel.user)
                                }
                            }
                        }
                    }
                    .start()
            }
        }

        if (Build.VERSION.SDK_INT >= 31) {
            setContent {
                FishingNotesTheme(appTheme.value) {
                    DistributionScreen(viewModel.user)
                }
            }
        }



        MobileAds.initialize(this) {}

        /*
            Kostya's Pixel XL = 7254B9BDD30F1D2EACA4C4EAD6B31F2C
            Oleg = F70916713215C0BC73564CDFEC4D3ECB

        */
        val testDeviceIds =
            Arrays.asList("7254B9BDD30F1D2EACA4C4EAD6B31F2C", "F70916713215C0BC73564CDFEC4D3ECB")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
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
                    appUpdateInfo, AppUpdateType.FLEXIBLE,
                    this, UPDATE_REQUEST_CODE
                )
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_CANCELED -> {
                    SnackbarManager.showMessage(R.string.update_canceled)
                }
                RESULT_OK -> {
                    SnackbarManager.showMessage(R.string.update_downloading)
                }
                else -> {
                    SnackbarManager.showMessage(R.string.update_failed)
                    checkForUpdates()
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @ExperimentalPermissionsApi
    @ExperimentalPagerApi
    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi
    @InternalCoroutinesApi
    @Composable
    private fun DistributionScreen(user: User?) {
        if (user != null) FishingNotesApp()
        else Navigation()
    }

    // This app draws behind the system bars, so we want to handle fitting system windows
    // WindowCompat.setDecorFitsSystemWindows(window, false)

    //light тема
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


    @OptIn(ExperimentalComposeUiApi::class)
    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @ExperimentalPermissionsApi
    @Composable
    fun Navigation() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "login_screen"
        ) {
            composable("login_screen") {
                LoginScreen(navController = navController)
            }
            // Main Screen
            composable(MainDestinations.HOME_ROUTE) {
                FishingNotesApp()
            }
        }
    }

    fun startGoogleLogin() {
        // Configure GOOGLE sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent: Intent = googleSignInClient.signInIntent
        registeredActivity.launch(signInIntent)
    }

    private fun onActivityResult(result: ActivityResult) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val exception = task.exception
        when {
            task.isSuccessful -> {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    handleError(e)
                }
            }
            else -> {
                handleError(exception)
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

            val bundle = bundleOf()
            bundle.putString(FirebaseAnalytics.Param.SCORE, error.message)
            Firebase.analytics.logEvent("signin_error", bundle)

            //Toast.makeText(this,  error.message, Toast.LENGTH_LONG).show()
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
