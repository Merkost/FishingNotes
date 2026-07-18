package com.mobileprism.fishing.ui

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import com.mobileprism.fishing.ui.home.views.BannerTone
import com.mobileprism.fishing.ui.home.views.InlineBannerCard
import com.mobileprism.fishing.ui.theme.BrandGradients
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.format.errorToMessage
import com.mobileprism.fishing.ui.utils.motion.slideUpFadeIn
import com.mobileprism.fishing.ui.viewmodels.LoginUiState
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.ic_google_logo
import fishing.shared.generated.resources.ic_launcher
import fishing.shared.generated.resources.login_headline
import fishing.shared.generated.resources.login_subtitle
import fishing.shared.generated.resources.login_trust_copy
import fishing.shared.generated.resources.retry
import fishing.shared.generated.resources.sign_in_generic_error
import fishing.shared.generated.resources.sign_with_google
import fishing.shared.generated.resources.signing_in
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LoginScreen() {
    val loginViewModel: LoginViewModel = koinInject()
    val uiState by loginViewModel.uiState.collectAsState()
    val signing = uiState is LoginUiState.Signing
    val errorStringRes = (uiState as? LoginUiState.Error)?.let { state ->
        val message = state.message
        if (message.isNullOrBlank()) {
            Res.string.sign_in_generic_error
        } else {
            errorToMessage(RuntimeException(message))
        }
    }

    Scaffold(contentWindowInsets = WindowInsets(0)) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandGradients.surfaceVertical(FishingTheme.colorScheme))
                .padding(paddingValues)
                .systemBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(FishingTheme.shapes.extraLarge)
                        .slideUpFadeIn(),
                )

                Text(
                    text = stringResource(Res.string.login_headline),
                    style = FishingTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FishingTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = Spacing.sm)
                        .slideUpFadeIn(),
                )

                Text(
                    text = stringResource(Res.string.login_subtitle),
                    style = FishingTheme.typography.bodyLarge,
                    color = FishingTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = Spacing.md)
                        .slideUpFadeIn(),
                )

                GoogleButtonUiContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.xl)
                        .slideUpFadeIn(),
                    onGoogleSignInResult = { googleUser ->
                        val idToken = googleUser?.idToken
                        if (idToken != null) {
                            loginViewModel.firebaseSignInWithGoogle(idToken)
                        } else {
                            loginViewModel.onGoogleSignInFailed()
                        }
                    },
                ) {
                    AppButton(
                        text = if (signing) {
                            stringResource(Res.string.signing_in)
                        } else {
                            stringResource(Res.string.sign_with_google)
                        },
                        loading = signing,
                        enabled = !signing,
                        leadingIcon = if (!signing) painterResource(Res.drawable.ic_google_logo) else null,
                        onClick = { this@GoogleButtonUiContainer.onClick() },
                        style = AppButtonStyle.Filled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Text(
                    text = stringResource(Res.string.login_trust_copy),
                    style = FishingTheme.typography.bodySmall,
                    color = FishingTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = Spacing.lg)
                        .slideUpFadeIn(),
                )

                AnimatedVisibility(visible = errorStringRes != null) {
                    if (errorStringRes != null) {
                        InlineBannerCard(
                            tone = BannerTone.Error,
                            icon = Icons.Outlined.Warning,
                            title = stringResource(errorStringRes),
                            actionLabel = stringResource(Res.string.retry),
                            onClick = { loginViewModel.onGoogleSignInCancelled() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.lg),
                        )
                    }
                }
            }
        }
    }
}
