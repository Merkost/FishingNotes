package com.mobileprism.fishing.ui.login

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datasource.firebase.createLauncherActivityForGoogleAuth
import com.mobileprism.fishing.model.datasource.firebase.getGoogleLoginAuth
import com.mobileprism.fishing.ui.custom.FishingTextButton
import com.mobileprism.fishing.ui.custom.LoginWithGoogleButton
import com.mobileprism.fishing.ui.home.views.HeaderText
import com.mobileprism.fishing.ui.custom.ModalLoading
import com.mobileprism.fishing.ui.home.views.SecondaryText
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.ui.utils.noRippleClickable
import com.mobileprism.fishing.ui.viewmodels.login.StartViewModel
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import org.koin.androidx.compose.get

object LoginDestinations {
    const val START = "start_screen"
    const val LOGIN = "login_screen"
    const val FORGOT_PASSWORD = "forgot_password_screen"
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
    val viewModel: StartViewModel = get()

    val startForResult = createLauncherActivityForGoogleAuth(
        context = context,
        onComplete = viewModel::continueWithGoogle,
        onError = { viewModel.googleAuthError(it) }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BaseViewState.Error -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_occured),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

    AnimatedVisibility(visible = uiState is BaseViewState.Loading) { ModalLoading() }

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

                FishingTextButton(
                    onClick = toRegistration,
                    content = {
                        Text(
                            text = "Register with email",
                            style = MaterialTheme.typography.body1.copy(color = MaterialTheme.customColors.secondaryTextColor)
                        )
                    },
                )
            }


//            DefaultButtonOutlined(
//                modifier = Modifier,
//                text = stringResource(id = R.string.skip),
//                icon = Icons.Default.ArrowForward,
//                onClick = { viewModel.skipAuthorization() }
//            )

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .noRippleClickable { toLoginScreen() },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.have_an_account),
                    style = MaterialTheme.typography.body1.copy(color = MaterialTheme.customColors.secondaryTextColor)
                )

                Text(
                    text = stringResource(R.string.sign_in),
                    style = MaterialTheme.typography.body1.copy(
                        color = MaterialTheme.colors.primaryVariant,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}
