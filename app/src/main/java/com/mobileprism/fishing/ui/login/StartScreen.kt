package com.mobileprism.fishing.ui.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datasource.firebase.getGoogleLoginAuth
import com.mobileprism.fishing.model.datasource.firebase.startFirebaseLogin
import com.mobileprism.fishing.ui.custom.LoginWithGoogleButton
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import com.mobileprism.fishing.ui.viewstates.LoginScreenViewState
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
    toLoginScreen: () -> Unit,
    toRegistration: () -> Unit
) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = get()
    val auth: FirebaseAuth = get()

    val onGoogleError: (Exception?) -> Unit = loginViewModel::googleAuthError

    val startForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                loginViewModel.continueWithGoogle()
                result.data?.let { intent ->
                    val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    startFirebaseLogin(context, task, auth) {
                        onGoogleError(it)
                    }
                } ?: onGoogleError(null)
            } else onGoogleError(Exception("Operation canceled by user"))
        }

    val coroutineScope = rememberCoroutineScope()


    var helpDialogState by remember { mutableStateOf(false) }
    val uiState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginScreenViewState.LoginSuccess -> {

            }
            is LoginScreenViewState.Loading -> {

            }
            is LoginScreenViewState.Error -> {
                Toast.makeText(context, state.error.message ?: context.getString(R.string.error_occured), Toast.LENGTH_SHORT).show()
            }
            is LoginScreenViewState.NotLoggedIn -> {

            }
        }
    }

    if (uiState == LoginScreenViewState.Loading) { ModalLoading() }

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
                        /*// FIXME: Improve this
                        onStartGoogleLogin()*/

                        startForResult.launch(context.getGoogleLoginAuth().signInIntent)
                    }
                )

                DefaultButtonSecondaryLight(text = "I'll use email or phone number") {
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




