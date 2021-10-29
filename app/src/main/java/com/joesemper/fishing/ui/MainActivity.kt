package com.joesemper.fishing.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.FishingNotesApp
import com.joesemper.fishing.domain.MainViewModel
import com.joesemper.fishing.domain.SplashViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.utils.Logger
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

//    override val scope: Scope by activityScope()
    //private val viewModel: MainViewModel by viewModel()

    private val logger: Logger by inject()

    //private var _binding: ActivityMainBinding? = null
    //private val binding get() = _binding!!

    companion object {
        fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

//    override fun hideNav() {
//        binding.bottomNav.visibility = View.GONE
//    }
//
//    override fun showNav() {
//        binding.bottomNav.visibility = View.VISIBLE
//    }

    @ExperimentalPermissionsApi
    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This app draws behind the system bars, so we want to handle fitting system windows
        // WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FishingNotesApp()
        }

        //light тема
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

    }

    @Composable
    fun LoginScreen(navController: NavController) {

    }

    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @ExperimentalPermissionsApi
    @Composable
    fun Navigation() {
        val navController = rememberNavController()
        NavHost(navController = navController,
            startDestination = "splash_screen") {

            composable("splash_screen") {
                SplashScreen(navController = navController)
            }

            composable("login_screen") {
                LoginScreen(navController = navController)
            }
            // Main Screen
            composable("main_screen") {
                FishingNotesApp()
            }
        }
    }

    @Composable
    fun SplashScreen(navController: NavController) {
        val viewModel: SplashViewModel by viewModel()
        val userState = viewModel.subscribe().collectAsState()

        LaunchedEffect(userState) {
            when (userState.value) {
                is BaseViewState.Success<*> -> onSuccess((userState.value as BaseViewState.Success<*>).data as User?, navController)
                is BaseViewState.Loading -> { }
                is BaseViewState.Error -> { } //showErrorSnackbar
            }
        }

        val scale = remember {
            Animatable(0f)
        }

        // AnimationEffect
        LaunchedEffect(key1 = true) {
            scale.animateTo(
                targetValue = 0.2f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = {
                        OvershootInterpolator(4f).getInterpolation(it)
                    })
            )
            delay(1000)
            navController.navigate("main_screen")
        }

        // Image
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(id = R.drawable.ic_fishing),
                contentDescription = "Logo",
                modifier = Modifier.scale(scale.value))
        }
    }

    private fun onSuccess(user: User?, navController: NavController) {
        if (user != null) {
            navController.navigate("main_screen")
        } else {
            navController.navigate("login_screen")
        }
    }

//    private fun subscribeOnViewModel() {
//        lifecycleScope.launchWhenStarted {
//            viewModel.subscribe().collect { viewState ->
//                when (viewState) {
//                    is BaseViewState.Success<*> -> {
//                        onSuccess()
//                    }
//                    is BaseViewState.Error -> {
//                        onError(viewState.error)
//                    }
//                    is BaseViewState.Loading -> {
//                    }
//                }
//            }
//        }
//    }
//
//    private fun onSuccess() {
//
//    }
//
//    private fun onError(error: Throwable) {
//        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
//        logger.log(error.message)
//    }


    fun notReadyYetToast() {
        Toast.makeText(
            this,
            "This feature is still in development. Please, try it later",
            Toast.LENGTH_SHORT
        ).show()
    }

}
