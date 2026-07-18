package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import coil3.compose.SubcomposeAsyncImage
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.components.FormTextField
import com.mobileprism.fishing.ui.components.PickerField
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.AppTopBar
import com.mobileprism.fishing.ui.home.views.AvatarWithBadge
import com.mobileprism.fishing.ui.home.views.BannerTone
import com.mobileprism.fishing.ui.home.views.BottomActionBar
import com.mobileprism.fishing.ui.home.views.DatePickerDialog
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.InlineBannerCard
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.rememberMediaPickerLauncher
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.time.toDate
import com.mobileprism.fishing.viewmodels.EditProfileViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(onBack: () -> Unit, navController: NavController) {
    val viewModel: EditProfileViewModel = koinViewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val pendingPhotoPath by viewModel.pendingPhotoPath.collectAsState()
    val isChanged by viewModel.isChanged.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isAnonymous by viewModel.isAnonymous.collectAsState()
    val scrollState = rememberScrollState()

    val eighteenYearsAgoMillis = remember {
        Clock.System.now()
            .minus(18 * 365, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }

    var resetDialog by remember { mutableStateOf(false) }
    if (resetDialog) ResetDialog(
        onDismiss = { resetDialog = false },
        onReset = viewModel::resetChanges
    )

    var datePickerShown by remember { mutableStateOf(false) }
    if (datePickerShown) {
        DatePickerDialog(
            initialDate = currentUser.birthDate.takeIf { it != 0L } ?: eighteenYearsAgoMillis,
            maxDate = eighteenYearsAgoMillis,
            onDismiss = { datePickerShown = false },
            onDateChange = viewModel::birthdaySelected,
        )
    }

    val mediaPicker = rememberMediaPickerLauncher(
        maxPhotos = 1,
        onResult = { paths -> paths.firstOrNull()?.let(viewModel::onPhotoPicked) }
    )

    LaunchedEffect(uiState) {
        when (uiState) {
            is BaseViewState.Error -> SnackbarManager.showMessage(Res.string.error_occured)
            is BaseViewState.Success -> onBack()
            else -> {}
        }
    }

    val isLoading = uiState is BaseViewState.Loading

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.profile_edit),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack,
                actions = {
                    AnimatedVisibility(visible = isChanged) {
                        IconButton(onClick = { resetDialog = true }) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = stringResource(Res.string.reset_dialog)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomActionBar(
                primaryText = stringResource(Res.string.save),
                onClick = viewModel::updateProfile,
                loading = isLoading,
                visible = isChanged,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            AvatarWithBadge(
                modifier = Modifier.padding(vertical = Spacing.xl),
                contentDescription = stringResource(Res.string.edit_profile_photo),
                onEdit = { mediaPicker.launchGallery() },
            ) {
                val imagePath = pendingPhotoPath ?: currentUser.photoUrl
                SubcomposeAsyncImage(
                    model = imagePath,
                    contentDescription = stringResource(Res.string.user_photo),
                    contentScale = ContentScale.Crop,
                    error = {
                        Image(
                            modifier = Modifier.padding(8.dp),
                            painter = painterResource(Res.drawable.ic_fisher),
                            contentDescription = stringResource(Res.string.fisher)
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            FormTextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentUser.displayName,
                onValueChange = viewModel::onNameChange,
                label = stringResource(Res.string.name_hint),
                leadingIcon = rememberVectorPainter(Icons.Default.Person),
            )

            FormTextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentUser.login,
                onValueChange = viewModel::onLoginChange,
                label = stringResource(Res.string.username),
                leadingIcon = rememberVectorPainter(Icons.Default.AlternateEmail),
            )

            if (!isAnonymous) {
                FormTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = currentUser.email,
                    onValueChange = {},
                    label = stringResource(Res.string.email_hint),
                    leadingIcon = rememberVectorPainter(Icons.Default.Email),
                    readOnly = true,
                )
            }

            PickerField(
                modifier = Modifier.fillMaxWidth(),
                value = if (currentUser.birthDate != 0L) currentUser.birthDate.toDate() else "",
                label = stringResource(Res.string.birthday_hint),
                placeholder = stringResource(Res.string.birthday_set),
                leadingIcon = rememberVectorPainter(Icons.Default.EditCalendar),
                onClick = { datePickerShown = true },
            )

            if (isAnonymous) {
                InlineBannerCard(
                    tone = BannerTone.Info,
                    icon = Icons.Default.CloudUpload,
                    title = stringResource(Res.string.editprofile_guest_note),
                    actionLabel = stringResource(Res.string.link_action),
                    onClick = { navController.navigate(MainDestinations.LinkAccount) },
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg),
                )
            }
        }
    }
}

@Composable
fun ResetDialog(onDismiss: () -> Unit, onReset: () -> Unit) {
    DefaultDialog(
        primaryText = stringResource(Res.string.reset_dialog),
        secondaryText = stringResource(Res.string.reset_dialog_secondary),
        positiveButtonText = stringResource(Res.string.yes),
        negativeButtonText = stringResource(Res.string.no),
        onPositiveClick = { onReset(); onDismiss() },
        onNegativeClick = onDismiss,
        onDismiss = onDismiss
    )
}
