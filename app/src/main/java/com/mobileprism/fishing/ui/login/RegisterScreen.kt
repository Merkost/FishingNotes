package com.mobileprism.fishing.ui.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.ui.home.views.HeaderText
import com.mobileprism.fishing.ui.home.views.SecondaryTextSmall
import com.mobileprism.fishing.ui.viewmodels.login.RegisterViewModel
import com.mobileprism.fishing.utils.*
import org.koin.androidx.compose.get

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun RegisterScreen(upPress: () -> Unit) {
    val loginViewModel: RegisterViewModel = get()

    val context = LocalContext.current

    val email = rememberSaveable() { mutableStateOf("") }
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


    LaunchedEffect(key1 = email.value) {
        if (isEmailInputCorrect(email.value)) {
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
            !isLoginInputCorrect(email.value) && !isEmailInputCorrect(email.value)

        loginErrorMassage.value = if (email.value.length < LOGIN_MIN_LENGTH) {
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
            // TODO:
            //onApply(EmailPassword(email.value, password.value))
        } else {
            showToast(context, context.getString(R.string.invalid_data_entry_format))
        }
    }

    Scaffold {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text(text = stringResource(R.string.email_or_username)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, Icons.Default.Person.name)
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
                leadingIcon = { Icon(Icons.Default.Password, Icons.Default.Password.name) },
                trailingIcon = {
                    if (showPassword.value) {
                        IconButton(onClick = { showPassword.value = !showPassword.value }) {
                            Icon(Icons.Default.VisibilityOff, Icons.Default.VisibilityOff.name)
                        }
                    } else {
                        IconButton(onClick = { showPassword.value = !showPassword.value }) {
                            Icon(Icons.Default.Visibility, Icons.Default.Visibility.name)
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
            }

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
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                DefaultButtonFilled(
                    text = stringResource(R.string.register),
                    onClick = { onApplyRegistration() }
                )
            }
        }
    }

}