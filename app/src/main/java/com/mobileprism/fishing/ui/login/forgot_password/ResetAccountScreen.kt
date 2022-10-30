package com.mobileprism.fishing.ui.login.forgot_password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.custom.FishingPasswordTextField
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.FishingButtonFilled
import com.mobileprism.fishing.ui.home.views.HeaderText
import com.mobileprism.fishing.ui.login.DefaultAuthColumn
import com.mobileprism.fishing.ui.viewmodels.restore.ResetAccountViewModel
import com.mobileprism.fishing.ui.viewmodels.restore.UserLogin
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.showError
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ResetAccountScreen(userLogin: UserLogin, onNext: () -> Unit, upPress: () -> Unit) {

    val viewModel: ResetAccountViewModel = getViewModel(parameters = { parametersOf(userLogin) })
    val resetInfo = viewModel.resetInfo.collectAsState()
    val uiState = viewModel.uiState.collectAsState()

    val showPassword = rememberSaveable() { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(uiState.value) {
        when(uiState.value) {
            UiState.Error -> {
                context.applicationContext.showError(BaseViewState.Error())
            }
            UiState.InProgress -> {
            }
            UiState.Success -> {
                onNext()
            }
            null -> {}
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        DefaultAuthColumn() {
            AuthTopBar(onBackClick = upPress) {
                HeaderText(text = stringResource(R.string.new_password))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {



                FishingPasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusEvent {
                            if (it.isFocused.not() && resetInfo.value.password.isNotEmpty()) viewModel.validatePasswordInput()
                        },
                    password = resetInfo.value.password,
                    onValueChange = viewModel::onPasswordSet,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    isError = resetInfo.value.passwordError.successful.not(),
                    errorString = resetInfo.value.passwordError.errorMessage,
                    enabled = uiState.value !is UiState.InProgress,
                    showPassword = showPassword.value,
                    onShowPasswordChanged = { showPassword.value = !showPassword.value },
                )

                FishingPasswordTextField(
                    modifier = Modifier.fillMaxWidth(),
                    password = resetInfo.value.repeatPassword,
                    onValueChange = viewModel::onRepeatPasswordSet,
                    placeholder = stringResource(R.string.repeat_password),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.saveNewPassword()
                        }
                    ),
                    isError = resetInfo.value.repeatPasswordError.successful.not(),
                    errorString = resetInfo.value.repeatPasswordError.errorMessage,
                    enabled = uiState.value !is UiState.InProgress,
                    showPassword = showPassword.value
                )
            }

            AnimatedVisibility(visible = uiState.value is UiState.InProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Crossfade(uiState.value is UiState.InProgress) {
                    when (it) {
                        true -> {
                            DefaultButtonOutlined(
                                text = stringResource(R.string.cancel),
                                onClick = viewModel::cancelSaveNewPassword
                            )
                        }
                        else -> {
                            Spacer(modifier = Modifier.size(4.dp))
                        }
                    }
                }

                FishingButtonFilled(
                    text = stringResource(R.string.register),
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.saveNewPassword()
                    }
                )
            }

        }
    }
}

@Composable
fun AuthTopBar(onBackClick: (() -> Unit)? = null, text: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f, false)) {
            text()
        }

        onBackClick?.let {
            IconButton(onClick = it) {
                Icon(Icons.Default.Close, Icons.Default.Close.name)
            }
        }

    }
}
