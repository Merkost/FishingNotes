package com.mobileprism.fishing.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import com.mobileprism.fishing.ui.home.views.AppText
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.home.views.AppTopBar
import com.mobileprism.fishing.ui.home.views.BannerTone
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.InlineBannerCard
import com.mobileprism.fishing.ui.home.views.ModalLoadingDialog
import com.mobileprism.fishing.ui.theme.BrandGradients
import com.mobileprism.fishing.ui.theme.FishingTheme
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.viewmodels.LinkAccountViewModel
import com.mobileprism.fishing.ui.viewmodels.LinkState
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkAccountScreen(onBack: () -> Unit, onLinked: () -> Unit) {
    val vm: LinkAccountViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is LinkState.Success) {
            SnackbarManager.showMessage(Res.string.link_success)
            onLinked()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandGradients.surfaceVertical(FishingTheme.colorScheme))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_launcher),
                contentDescription = null,
                modifier = Modifier.padding(top = Spacing.xxl).size(120.dp).clip(FishingTheme.shapes.extraLarge),
            )
            Text(
                text = stringResource(Res.string.link_account_title),
                style = FishingTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.lg),
            )
            Text(
                text = stringResource(Res.string.link_account_subtitle),
                style = FishingTheme.typography.bodyLarge,
                color = FishingTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            Column(
                modifier = Modifier.padding(top = Spacing.xl).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                BenefitRow(Icons.Outlined.CloudDone, stringResource(Res.string.link_benefit_backup))
                BenefitRow(Icons.Outlined.Devices, stringResource(Res.string.link_benefit_sync))
                BenefitRow(Icons.Outlined.Person, stringResource(Res.string.link_benefit_profile))
            }

            GoogleButtonUiContainer(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                onGoogleSignInResult = { googleUser ->
                    val idToken = googleUser?.idToken
                    if (idToken != null) vm.linkWithGoogle(idToken) else vm.onSignInCancelled()
                },
            ) {
                AppButton(
                    text = stringResource(Res.string.sign_with_google),
                    leadingIcon = painterResource(Res.drawable.ic_google_logo),
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
                modifier = Modifier.padding(top = Spacing.lg),
            )

            AnimatedVisibility(visible = state is LinkState.Error) {
                InlineBannerCard(
                    tone = BannerTone.Error,
                    icon = Icons.Outlined.Warning,
                    title = stringResource(Res.string.sign_in_generic_error),
                    actionLabel = stringResource(Res.string.retry),
                    onClick = { vm.retry() },
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg),
                )
            }

            AppButton(
                text = stringResource(Res.string.link_maybe_later),
                onClick = onBack,
                style = AppButtonStyle.Text,
                modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.xxl),
            )
        }
    }

    ModalLoadingDialog(
        visible = state is LinkState.Linking,
        text = stringResource(Res.string.linking_in_progress),
    )

    val mergeState = state
    if (mergeState is LinkState.MergeConfirm) {
        DefaultDialog(
            primaryText = stringResource(Res.string.merge_confirm_title),
            secondaryText = stringResource(Res.string.merge_confirm_message),
            positiveButtonText = stringResource(Res.string.merge_confirm_positive),
            onPositiveClick = { vm.confirmMerge() },
            negativeButtonText = stringResource(Res.string.cancel),
            onNegativeClick = { vm.dismissMerge() },
            onDismiss = { vm.dismissMerge() },
        )
    }
    ModalLoadingDialog(
        visible = mergeState is LinkState.Merging,
        text = stringResource(Res.string.merge_in_progress) + "\n" + stringResource(Res.string.merge_progress_detail),
        progress = (mergeState as? LinkState.Merging)?.progress,
    )
    if (mergeState is LinkState.MergeSuccess) {
        val message = if (mergeState.alreadyPresent > 0) {
            stringResource(
                Res.string.merge_done_message_deduped,
                mergeState.catchesAdded + mergeState.markersAdded,
                mergeState.alreadyPresent,
            )
        } else {
            stringResource(Res.string.merge_done_message, mergeState.catchesAdded, mergeState.markersAdded)
        }
        DefaultDialog(
            primaryText = stringResource(Res.string.merge_done_title),
            secondaryText = message,
            positiveButtonText = stringResource(Res.string.merge_view_log),
            onPositiveClick = { onLinked() },
            onDismiss = { onLinked() },
        )
    }
}

@Composable
private fun BenefitRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = FishingTheme.colorScheme.primary)
        AppText(text = text, style = AppTextStyle.Body, modifier = Modifier.padding(start = Spacing.md))
    }
}
