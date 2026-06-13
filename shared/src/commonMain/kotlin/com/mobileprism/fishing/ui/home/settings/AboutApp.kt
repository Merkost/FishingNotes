package com.mobileprism.fishing.ui.home.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.mobileprism.fishing.ui.home.views.AppHeroHeader
import com.mobileprism.fishing.ui.home.views.AppTopBar
import com.mobileprism.fishing.ui.home.views.LabeledIconButton
import com.mobileprism.fishing.ui.home.views.LabeledIconButtonStyle
import com.mobileprism.fishing.ui.home.views.VersionLabel
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.rememberAppVersion
import com.mobileprism.fishing.ui.utils.rememberBillingLauncher
import com.mobileprism.fishing.ui.utils.rememberOpenAppStore
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutApp(upPress: () -> Unit) {
    val currentVersion = rememberAppVersion()
    val openAppStore = rememberOpenAppStore()
    val launchBilling = rememberBillingLauncher()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.settings_about),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = upPress,
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenH),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(Spacing.xxl))
            AppHeroHeader(
                title = stringResource(Res.string.app_name),
                logo = painterResource(Res.drawable.ic_launcher),
                modifier = Modifier.fillMaxWidth(),
            )
            VersionLabel(
                version = currentVersion ?: stringResource(Res.string.unknown_version),
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LabeledIconButton(
                    label = stringResource(Res.string.leave_review),
                    icon = Icons.Default.RateReview,
                    onClick = { openAppStore() },
                    style = LabeledIconButtonStyle.Filled,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (launchBilling != null) {
                    LabeledIconButton(
                        label = stringResource(Res.string.app_donation),
                        icon = Icons.Default.Savings,
                        onClick = { launchBilling() },
                        style = LabeledIconButtonStyle.Outlined,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
            Text(
                text = stringResource(Res.string.made_in_russia),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}
