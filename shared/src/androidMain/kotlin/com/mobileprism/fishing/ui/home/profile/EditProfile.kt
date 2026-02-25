package com.mobileprism.fishing.ui.home.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.utils.showError
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.time.toDate
import com.mobileprism.fishing.viewmodels.EditProfileViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: EditProfileViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isChanged by viewModel.isChanged.collectAsState()
    val scrollState = rememberScrollState()

    var resetDialog by remember { mutableStateOf(false) }
    if (resetDialog) ResetDialog(
        onDismiss = { resetDialog = false },
        onReset = viewModel::resetChanges
    )

    var datePickerShown by remember { mutableStateOf(false) }
    if (datePickerShown) {
        DatePickerDialog(context,
            initialDate = currentUser.birthDate.takeIf { it != 0L } ?: Calendar.getInstance()
                .apply { this.add(Calendar.YEAR, -18) }.timeInMillis,
            maxDate = Calendar.getInstance().apply { this.add(Calendar.YEAR, -18) }.timeInMillis,
            onDismiss = { datePickerShown = false }, onDateChange = viewModel::birthdaySelected)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BaseViewState.Error -> showError(context.applicationContext, state.error?.message)
            is BaseViewState.Success -> onBack()
            else -> {}
        }
    }

    val isLoading = uiState is BaseViewState.Loading
    if (isLoading) ModalLoadingDialog(
        visible = true,
        text = context.getString(R.string.loading)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            EditProfileTopAppBar(
                isChanged = isChanged,
                onReset = { resetDialog = true },
                onBack = onBack
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = isChanged) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = viewModel::updateProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        enabled = !isLoading,
                    ) {
                        Text(text = stringResource(id = R.string.save))
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Profile photo
            UserImage(
                modifier = Modifier.fillMaxWidth(),
                user = currentUser,
                imgSize = 120.dp,
                icon = Icons.Default.Edit,
                onIconClick = {}
            )

            // Name
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentUser.displayName,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(id = R.string.name_hint)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
            )

            // Username
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentUser.login,
                onValueChange = viewModel::onLoginChange,
                label = { Text(stringResource(id = R.string.username)) },
                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                singleLine = true,
            )

            // Email (read-only)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = currentUser.email,
                onValueChange = {},
                label = { Text(stringResource(id = R.string.email_hint)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                readOnly = true,
                singleLine = true,
            )

            // Birthday
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerShown = true },
                value = if (currentUser.birthDate != 0L) currentUser.birthDate.toDate() else "",
                onValueChange = {},
                label = { Text(stringResource(id = R.string.birthday_hint)) },
                leadingIcon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
                placeholder = { Text(stringResource(id = R.string.birthday_set)) },
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ResetDialog(onDismiss: () -> Unit, onReset: () -> Unit) {
    DefaultDialog(
        primaryText = stringResource(id = R.string.reset_dialog),
        secondaryText = stringResource(id = R.string.reset_dialog_secondary),
        positiveButtonText = stringResource(id = R.string.yes),
        negativeButtonText = stringResource(id = R.string.no),
        onPositiveClick = { onReset(); onDismiss() },
        onNegativeClick = onDismiss,
        onDismiss = onDismiss
    )
}

@Composable
fun EditProfileTopAppBar(
    isChanged: Boolean,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    DefaultAppBar(
        title = stringResource(id = R.string.profile_edit),
        onNavClick = onBack,
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        AnimatedVisibility(isChanged) {
            IconButton(onClick = onReset) {
                Icon(Icons.Default.RestartAlt, contentDescription = stringResource(R.string.reset_dialog))
            }
        }
    }
}
