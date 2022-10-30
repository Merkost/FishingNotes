package com.mobileprism.fishing.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField
import com.mobileprism.fishing.ui.custom.FishingPasswordTextField
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.FishingButtonFilled
import com.mobileprism.fishing.ui.home.views.HeaderText
import com.mobileprism.fishing.ui.home.views.SecondaryTextSmall
import com.mobileprism.fishing.ui.viewmodels.login.RegisterViewModel
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.showError
import org.koin.androidx.compose.get

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun RegisterScreen(upPress: () -> Unit) {
    val viewModel: RegisterViewModel = get()

    val uiState by viewModel.uiState.collectAsState()

    val showPassword = rememberSaveable() { mutableStateOf(false) }
    val registerInfo = viewModel.registerInfo.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            UiState.Error -> {
                context.applicationContext.showError(BaseViewState.Error())
            }
            else -> {}
        }
    }

    Scaffold {
        DefaultAuthColumn() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderText(text = stringResource(R.string.create_an_account))

                IconButton(onClick = upPress) {
                    Icon(Icons.Default.Close, Icons.Default.Close.name)
                }
            }

            FishingOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusEvent {
                        if (it.isFocused.not() && registerInfo.value.email.isNotEmpty()) viewModel.validateEmailInput()
                    },
                value = registerInfo.value.email,
                onValueChange = viewModel::onEmailSet,
                placeholder = stringResource(R.string.email),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, Icons.Default.Person.name)
                },
                isError = registerInfo.value.emailError.successful.not(),
                errorString = registerInfo.value.emailError.errorMessage,
                enabled = uiState !is UiState.InProgress,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                FishingPasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusEvent {
                            if (it.isFocused.not() && registerInfo.value.password.isNotEmpty()) viewModel.validatePasswordInput()
                        },
                    password = registerInfo.value.password,
                    onValueChange = viewModel::onPasswordSet,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    isError = registerInfo.value.passwordError.successful.not(),
                    errorString = registerInfo.value.passwordError.errorMessage,
                    enabled = uiState !is UiState.InProgress,
                    showPassword = showPassword.value,
                    onShowPasswordChanged = { showPassword.value = !showPassword.value },
                )

                FishingPasswordTextField(
                    modifier = Modifier.fillMaxWidth(),
                    password = registerInfo.value.repeatPassword,
                    onValueChange = viewModel::onRepeatPasswordSet,
                    placeholder = stringResource(R.string.repeat_password),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.registerNewUser()
                        }
                    ),
                    isError = registerInfo.value.repeatPasswordError.successful.not(),
                    errorString = registerInfo.value.repeatPasswordError.errorMessage,
                    enabled = uiState !is UiState.InProgress,
                    showPassword = showPassword.value
                )
            }

            AnimatedVisibility(visible = uiState is UiState.InProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Crossfade(uiState is UiState.InProgress) {
                    when (it) {
                        true -> {
                            DefaultButtonOutlined(
                                text = stringResource(R.string.cancel),
                                onClick = viewModel::cancelRegister
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
                        viewModel.registerNewUser()
                    }
                )
            }


            Column {
                AnimatedVisibility(visible = registerInfo.value.termsError.successful.not()) {
                    Text(
                        text = registerInfo.value.termsError.errorMessage?.let { "$it  \uD83D\uDC47" }
                            ?: "",
                        color = MaterialTheme.colors.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.End)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    RulesCheckBox(
                        //modifier = Modifier.weight(1f, false),
                        checked = registerInfo.value.terms,
                        onCheckedChange = viewModel::onTermsSet
                    )
                }
            }
        }
    }

}

@Composable
fun DefaultAuthColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
fun RulesCheckBox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Switch(
            modifier = Modifier.clip(MaterialTheme.shapes.medium),
            checked = checked,
            onCheckedChange = onCheckedChange,
            interactionSource = MutableInteractionSource(),
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colors.primary,
                checkedThumbColor = MaterialTheme.colors.primaryVariant
            )
        )
        SecondaryTextSmall(
            text = "Принимаю условия пользования", modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Start
        )
    }
}


