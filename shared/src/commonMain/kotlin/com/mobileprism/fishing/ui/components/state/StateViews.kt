package com.mobileprism.fishing.ui.components.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import com.mobileprism.fishing.ui.home.views.AppText
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.ic_error
import fishing.shared.generated.resources.ic_fishing
import fishing.shared.generated.resources.ic_no_internet
import fishing.shared.generated.resources.ic_no_photos
import fishing.shared.generated.resources.ic_no_place_on_map
import fishing.shared.generated.resources.network_error_message
import fishing.shared.generated.resources.no_cathces_added
import fishing.shared.generated.resources.no_photos_added
import fishing.shared.generated.resources.no_places_added
import fishing.shared.generated.resources.retry
import fishing.shared.generated.resources.something_went_wrong
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyState(
    illustration: Painter,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                painter = illustration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp),
            )
            AppText(
                text = title,
                style = AppTextStyle.Title,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (description != null) {
                AppText(
                    text = description,
                    style = AppTextStyle.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (action != null) action()
        }
    }
}

@Composable
fun EmptyStateNoCatches(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) = EmptyState(
    illustration = painterResource(Res.drawable.ic_fishing),
    title = stringResource(Res.string.no_cathces_added),
    modifier = modifier,
    action = action,
)

@Composable
fun EmptyStateNoPlaces(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) = EmptyState(
    illustration = painterResource(Res.drawable.ic_no_place_on_map),
    title = stringResource(Res.string.no_places_added),
    modifier = modifier,
    action = action,
)

@Composable
fun EmptyStateNoPhotos(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) = EmptyState(
    illustration = painterResource(Res.drawable.ic_no_photos),
    title = stringResource(Res.string.no_photos_added),
    modifier = modifier,
    action = action,
)

@Composable
fun ErrorState(
    message: String,
    illustration: Painter,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                painter = illustration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp),
            )
            AppText(
                text = message,
                style = AppTextStyle.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (onRetry != null) {
                AppButton(
                    text = stringResource(Res.string.retry),
                    onClick = onRetry,
                    style = AppButtonStyle.Tonal,
                )
            }
        }
    }
}

@Composable
fun ErrorStateGeneric(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) = ErrorState(
    message = stringResource(Res.string.something_went_wrong),
    illustration = painterResource(Res.drawable.ic_error),
    modifier = modifier,
    onRetry = onRetry,
)

@Composable
fun ErrorStateNoInternet(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) = ErrorState(
    message = stringResource(Res.string.network_error_message),
    illustration = painterResource(Res.drawable.ic_no_internet),
    modifier = modifier,
    onRetry = onRetry,
)

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun InlineLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp))
    }
}

@Composable
fun ListAppendLoader(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        if (isError && onRetry != null) {
            AppButton(
                text = stringResource(Res.string.retry),
                onClick = onRetry,
                style = AppButtonStyle.Text,
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
    }
}
