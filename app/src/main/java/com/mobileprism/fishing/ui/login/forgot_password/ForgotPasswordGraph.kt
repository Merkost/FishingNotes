package com.mobileprism.fishing.ui.login.forgot_password

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mobileprism.fishing.ui.login.LoginDestinations


object RestoreDestinations {
    const val SEARCH_AND_CONFIRM_ACCOUNT = "search_account_screen"
    const val RESET_ACCOUNT = "reset_account_screen"

}

fun NavGraphBuilder.addForgotPasswordGraph(
    navController: NavController,
    modifier: Modifier = Modifier,
    upPress: () -> Unit,
) {


}



@Composable
fun ConfirmAccountScreen(upPress: () -> Unit, onNext: () -> Unit) {

}
