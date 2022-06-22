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
                onApply = { loginPassword ->

                },
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

    LaunchedEffect(key1 = password.value, key2 = repeatedPassword.value) {
        if (password.value == repeatedPassword.value) {
            isPasswordMatchError.value = false
        }
        if (isPasswordInputCorrect(password.value)) {
            isPasswordError.value = false
        }
    }

    LaunchedEffect(key1 = login.value) {
        if (isLoginInputCorrect(login.value) || isLoginInputCorrect(login.value)) {
            isLoginError.value = false
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
        if (isLoginError.value) {
            SecondaryTextSmall(
                modifier = Modifier.height(8.dp),
                text = stringResource(R.string.incorrect_login_format),
                textColor = Color.Red
            )
        } else {
            Spacer(modifier = Modifier.size(8.dp))
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
                text = stringResource(R.string.incorrect_password_format),
                textColor = Color.Red
            )
        } else {
            Spacer(modifier = Modifier.size(8.dp))
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
                onClick = {
                    if (password.value == repeatedPassword.value) {
                        val loginPassword = LoginPassword(
                            login = login.value,
                            password = password.value
                        )
                        when (checkLoginPasswordCorrectInput(loginPassword = loginPassword)) {
                            is LoginPasswordCheckResult.Success -> {
                                onApply(loginPassword)
                                showToast(context, "Success")
                            }
                            is LoginPasswordCheckResult.LoginError -> {
                                isLoginError.value = true
                            }
                            is LoginPasswordCheckResult.PasswordError -> {
                                isPasswordError.value = true
                            }
                        }
                    } else {
                        isPasswordMatchError.value = true
                    }

                }
            )
        }

    }
}

