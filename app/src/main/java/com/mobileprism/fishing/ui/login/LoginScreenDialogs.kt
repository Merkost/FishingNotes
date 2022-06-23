package com.mobileprism.fishing.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.ui.home.views.DefaultButton
import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.ui.home.views.HeaderText
import com.mobileprism.fishing.ui.home.views.SecondaryTextSmall
import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
import com.mobileprism.fishing.utils.*

sealed class BottomSheetLoginScreen() {
    object LoginScreen : BottomSheetLoginScreen()
    object RegisterScreen : BottomSheetLoginScreen()
}

@Composable
fun LoginModalBottomSheetContent(
    currentScreen: BottomSheetLoginScreen,
    viewModel: LoginViewModel,
    onCloseBottomSheet: () -> Unit
) {
    when (currentScreen) {
        BottomSheetLoginScreen.LoginScreen -> {
            LoginScreenDialog(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxSize(),
                onApply = { loginPassword ->

                },
                onCloseBottomSheet = onCloseBottomSheet
            )
        }
        BottomSheetLoginScreen.RegisterScreen -> {
            RegisterScreenDialog(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxSize(),
                onApply = { viewModel.registerNewUser(it) },
                onCloseBottomSheet = onCloseBottomSheet
            )
        }
    }
}

@Composable
fun LoginScreenDialog(
    modifier: Modifier = Modifier,
    onApply: (LoginPassword) -> Unit,
    onCloseBottomSheet: () -> Unit
) {
    val login = rememberSaveable() { mutableStateOf("") }
    val password = rememberSaveable() { mutableStateOf("") }
    val showPassword = rememberSaveable() { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderText(
                text = stringResource(R.string.sign_in),
                textColor = MaterialTheme.colors.primaryVariant
            )

            IconButton(onClick = onCloseBottomSheet) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_close_24),
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.size(32.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = login.value,
            onValueChange = { login.value = it },
            label = { Text(text = stringResource(id = R.string.login)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_person_24),
                    contentDescription = null
                )
            }
        )

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text(text = stringResource(R.string.password)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_password_24),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (showPassword.value) {
                    IconButton(onClick = { showPassword.value = !showPassword.value }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                } else {
                    IconButton(onClick = { showPassword.value = !showPassword.value }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_visibility_off_24),
                            contentDescription = null
                        )
                    }

                }
            }
        )

        Spacer(modifier = Modifier.size(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DefaultButton(
                text = stringResource(id = R.string.close),
                onClick = onCloseBottomSheet
            )

            DefaultButtonFilled(
                text = stringResource(id = R.string.login),
                onClick = { onApply(LoginPassword(login = login.value, password = password.value)) }
            )
        }

    }
}

@Composable
fun RegisterScreenDialog(
    modifier: Modifier = Modifier,
    onApply: (LoginPassword) -> Unit,
    onCloseBottomSheet: () -> Unit
) {
    val context = LocalContext.current

    val login = rememberSaveable() { mutableStateOf("") }
    val password = rememberSaveable() { mutableStateOf("") }
    val repeatedPassword = rememberSaveable() { mutableStateOf("") }

    val showPassword = rememberSaveable() { mutableStateOf(false) }

    val isLoginError = rememberSaveable() { mutableStateOf(false) }
    val isPasswordError = rememberSaveable() { mutableStateOf(false) }
    val isPasswordMatchError = rememberSaveable() { mutableStateOf(false) }

    val loginErrorMassage =
        rememberSaveable() { mutableStateOf(context.getString(R.string.login_min_length) + LOGIN_MIN_LENGTH) }
    val passwordErrorMassage =
        rememberSaveable() { mutableStateOf(context.getString(R.string.password_min_length) + PASSWORD_MIN_LENGTH) }


    LaunchedEffect(key1 = login.value) {
        if (isLoginInputCorrect(login.value) || isEmailInputCorrect(login.value)) {
            isLoginError.value = false
        }
    }

    LaunchedEffect(key1 = password.value, key2 = repeatedPassword.value) {
        if (isPasswordInputCorrect(password.value)) {
            isPasswordError.value = false
        }
        if (password.value.length == repeatedPassword.value.length) {
            isPasswordMatchError.value = false
        }
    }

    fun validateLoginEmailInput() {
        isLoginError.value =
            !isLoginInputCorrect(login.value) && !isEmailInputCorrect(login.value)

        loginErrorMassage.value = if (login.value.length < LOGIN_MIN_LENGTH) {
            context.getString(R.string.login_min_length) + LOGIN_MIN_LENGTH
        } else {
            context.getString(R.string.incorrect_login_format)
        }
    }

    fun validatePasswordInput() {
        isPasswordError.value = !isPasswordInputCorrect(password.value)

        passwordErrorMassage.value = if (password.value.length < PASSWORD_MIN_LENGTH) {
            context.getString(R.string.password_min_length) + PASSWORD_MIN_LENGTH
        } else {
            context.getString(R.string.incorrect_password_format)
        }

        isPasswordMatchError.value = password.value != repeatedPassword.value
    }

    fun isInputsCorrect(): Boolean {
        return !isLoginError.value && !isPasswordError.value && !isPasswordMatchError.value
    }

    fun onApplyRegistration() {
        validateLoginEmailInput()
        validatePasswordInput()

        if (isInputsCorrect()) {
            onApply(LoginPassword(login.value, password.value))
        } else {
            showToast(context, context.getString(R.string.invalid_data_entry_format))
        }
    }

    Column(
        modifier = modifier
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderText(
                text = stringResource(R.string.create_an_account)
            )

            IconButton(onClick = onCloseBottomSheet) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_close_24),
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.size(32.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = login.value,
            onValueChange = { login.value = it },
            label = { Text(text = stringResource(R.string.login_email)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_person_24),
                    contentDescription = null
                )
            }
        )
        if (isLoginError.value) {
            SecondaryTextSmall(
                modifier = Modifier.height(8.dp),
                text = loginErrorMassage.value,
                textColor = Color.Red
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text(text = stringResource(R.string.password)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_password_24),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (showPassword.value) {
                    IconButton(onClick = { showPassword.value = !showPassword.value }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                } else {
                    IconButton(onClick = { showPassword.value = !showPassword.value }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_visibility_off_24),
                            contentDescription = null
                        )
                    }

                }
            }
        )
        if (isPasswordError.value) {
            SecondaryTextSmall(
                modifier = Modifier.height(8.dp),
                text = passwordErrorMassage.value,
                textColor = Color.Red
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = repeatedPassword.value,
            onValueChange = { repeatedPassword.value = it },
            label = { Text(text = stringResource(R.string.repeat_password)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            isError = isPasswordMatchError.value,
            singleLine = true,
            visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_password_24),
                    contentDescription = null
                )
            }
        )
        if (isPasswordMatchError.value) {
            SecondaryTextSmall(
                modifier = Modifier.height(8.dp),
                text = stringResource(R.string.passwords_must_match),
                textColor = Color.Red
            )
        } else {
            Spacer(modifier = Modifier.size(8.dp))
        }

        Spacer(modifier = Modifier.size(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DefaultButton(
                text = stringResource(id = R.string.close),
                onClick = onCloseBottomSheet
            )

            DefaultButtonFilled(
                text = stringResource(R.string.register),
                onClick = { onApplyRegistration() }
            )
        }
    }
}

