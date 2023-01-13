package com.mobileprism.fishing.ui.login.forgot_password

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.login.LoginDestinations
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.requiredArg
import com.mobileprism.fishing.ui.viewmodels.restore.UserLogin


object RestoreDestinations {
    const val SEARCH_AND_CONFIRM_ACCOUNT = "search_account_screen"
    const val RESET_ACCOUNT = "reset_account_screen"

}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.addForgotPasswordGraph(
    navController: NavController,
    modifier: Modifier = Modifier,
    upPress: () -> Unit,
) {
    composable(RestoreDestinations.SEARCH_AND_CONFIRM_ACCOUNT) { from ->
        val systemUiController = rememberSystemUiController()
        val surfaceColor = MaterialTheme.colors.surface
        DisposableEffect(this) {
            systemUiController.setStatusBarColor(surfaceColor)
            onDispose {}
        }

        SearchAccountScreen(upPress = upPress, onNext = {
            navController.navigate(
                RestoreDestinations.RESET_ACCOUNT,
                Arguments.USER_LOGIN to it
            )
        })
    }

    composable(RestoreDestinations.RESET_ACCOUNT) { from ->
        val systemUiController = rememberSystemUiController()
        val surfaceColor = MaterialTheme.colors.surface
        DisposableEffect(this) {
            systemUiController.setStatusBarColor(surfaceColor)
            onDispose {}
        }

        val userLogin = from.requiredArg<UserLogin>(Arguments.USER_LOGIN)
        ResetAccountScreen(userLogin = userLogin, upPress = upPress, onNext = {
            navController.popBackStack(
                LoginDestinations.FORGOT_PASSWORD,
                inclusive = true
            )
        })
    }

}
