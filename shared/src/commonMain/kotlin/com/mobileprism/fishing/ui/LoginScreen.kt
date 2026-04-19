package com.mobileprism.fishing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mobileprism.fishing.ui.viewmodels.LoginUiState
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.ic_google_logo
import fishing.shared.generated.resources.ic_launcher
import fishing.shared.generated.resources.icon
import fishing.shared.generated.resources.login_headline
import fishing.shared.generated.resources.login_trust_copy
import fishing.shared.generated.resources.sign_with_google
import fishing.shared.generated.resources.signing_in
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import androidx.compose.foundation.Image

private val OnboardingBlendGradient = listOf(Color(0xFFFFE082), Color(0xFFE65100))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val loginViewModel: LoginViewModel = koinInject()
    val uiState by loginViewModel.uiState.collectAsState()
    val signing = uiState is LoginUiState.Signing
    val errorMessage = (uiState as? LoginUiState.Error)?.message

    Scaffold(contentWindowInsets = WindowInsets(0)) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(OnboardingBlendGradient))
                .padding(paddingValues)
                .systemBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_launcher),
                    contentDescription = stringResource(Res.string.icon),
                    modifier = Modifier.size(96.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.login_headline),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(48.dp))

                GoogleButtonUiContainer(
                    onGoogleSignInResult = { googleUser ->
                        val idToken = googleUser?.idToken
                        if (idToken != null) {
                            loginViewModel.firebaseSignInWithGoogle(idToken)
                        } else {
                            loginViewModel.onGoogleSignInCancelled()
                        }
                    },
                ) {
                    GoogleSignInButton(
                        signing = signing,
                        onClick = { if (!signing) this@GoogleButtonUiContainer.onClick() },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.login_trust_copy),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0.95f),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(signing: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (signing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.ic_google_logo),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = if (signing) stringResource(Res.string.signing_in) else stringResource(Res.string.sign_with_google),
            )
        }
    }
}
