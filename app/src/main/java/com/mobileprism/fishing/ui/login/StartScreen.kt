package com.mobileprism.fishing.ui.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.systemBarsPadding
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.custom.LoginWithGoogleButton
import com.mobileprism.fishing.ui.home.AppSnackbar
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.resources
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import com.mobileprism.fishing.ui.viewstates.LoginScreenViewState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

object LoginDestinations {
    const val START = "start_screen"
    const val LOGIN = "login_screen"
    const val REGISTER = "register_screen"

}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun StartScreen(
    onStartGoogleLogin: () -> Unit,
    toLoginScreen: () -> Unit,
    toRegistration: () -> Unit
) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = get()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var visible by remember { mutableStateOf(false) }
    var helpDialogState by remember { mutableStateOf(false) }

    val uiState by loginViewModel.uiState.collectAsState()

    val resources = resources()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginScreenViewState.LoginSuccess -> {
                sheetState.hide()
                visible = false
            }
            is LoginScreenViewState.Loading -> {
                sheetState.show()
            }
            is LoginScreenViewState.Error -> {
                scaffoldState.snackbarHostState.showSnackbar(
                    state.error.message ?: context.getString(R.string.error_occured)
                )
            }
            is LoginScreenViewState.NotLoggedIn -> {

            }
        }
    }

    LaunchedEffect(true) {
        coroutineScope.launch {
            SnackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages.first()
                    val text = resources.getText(message.messageId)
                    // Display the snackbar on the screen. `showSnackbar` is a function
                    // that suspends until the snackbar disappears from the screen
                    scaffoldState.snackbarHostState.showSnackbar(text.toString())
                    // Once the snackbar is gone or dismissed, notify the SnackbarManager
                    SnackbarManager.setMessageShown(message.id)
                }
            }
        }
    }

    if (helpDialogState) {
        LoginHelpDialog(onDismiss = { helpDialogState = false })
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp),
        sheetContent = {
            Column(
                modifier = Modifier.padding(50.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecondaryText(text = stringResource(id = R.string.loading))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }) {

        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(
                    hostState = it,
                    modifier = Modifier.systemBarsPadding(),
                    snackbar = { snackbarData -> AppSnackbar(snackbarData) }
                )
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {

                Column(
                    modifier = Modifier
                        .padding(vertical = 50.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .aspectRatio(1f),
                        painter = painterResource(id = R.drawable.ic_fishing_logo),
                        contentDescription = "app logo"
                    )
                    HeaderText(
                        text = stringResource(id = R.string.fishing_notes),
                        textStyle = MaterialTheme.typography.h6.copy(
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colors.primaryVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(2f, true),/*.background(Color.LightGray)*/
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {


                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_up),
                            style = MaterialTheme.typography.h4.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        SecondaryText(text = "It's easier to sign up now")
                    }

                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoginWithGoogleButton(
                            modifier = Modifier,
                            onClick = {
                                // FIXME: Improve this
                                onStartGoogleLogin()
                                loginViewModel.continueWithGoogle()
                            }
                        )

                        DefaultButtonSecondaryLight(text = "Register with email/username") {
                            toRegistration()
                        }
                    }


                    DefaultButtonOutlined(
                        modifier = Modifier,
                        text = stringResource(id = R.string.skip),
                        icon = Icons.Default.ArrowForward,
                        onClick = { loginViewModel.skipAuthorization() }
                    )

                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SecondaryText(text = stringResource(R.string.have_an_account))

                        Text(
                            modifier = Modifier.clickable {
                                toLoginScreen()
                            },
                            fontSize = 18.sp,
                            color = MaterialTheme.colors.primaryVariant,
                            text = stringResource(R.string.sign_in),
                            style = TextStyle(textDecoration = TextDecoration.Underline)
                        )
                    }

                }
            }
        }
    }

}

