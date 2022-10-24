package com.mobileprism.fishing.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.FishingButtonFilled
import com.mobileprism.fishing.ui.home.views.HeaderText
import com.mobileprism.fishing.ui.viewmodels.login.ForgotPasswordViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun ForgotPasswordScreen(upPress: () -> Unit) {
    val viewModel: ForgotPasswordViewModel = getViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val loginInfo by viewModel.loginInfo.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) {
        DefaultAuthColumn() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderText(text = stringResource(R.string.password_recovery))

                IconButton(onClick = upPress) {
                    Icon(Icons.Default.Close, Icons.Default.Close.name)
                }
            }

            FishingOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusEvent {
                        if (it.isFocused.not())
                            viewModel.validateLogin(skipEmpty = true)
                    },
                enabled = uiState !is UiState.InProgress,
                isError = loginInfo.loginError.successful.not(),
                errorString = loginInfo.loginError.errorMessage,
                value = loginInfo.login,
                onValueChange = viewModel::setLogin,
                placeholder = stringResource(R.string.email_or_username),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, Icons.Default.Person.name)
                }
            )

            FishingButtonFilled(
                text = stringResource(R.string.reset),
                onClick = {
                    // TODO:
//                    focusManager.clearFocus()
//                    viewModel.registerNewUser()
                }
            )
        }
    }
}