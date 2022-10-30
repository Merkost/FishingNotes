package com.mobileprism.fishing.ui.login.forgot_password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.custom.FishingOutlinedTextField
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.FishingButtonFilled
import com.mobileprism.fishing.ui.home.views.HeaderText
import com.mobileprism.fishing.ui.login.DefaultAuthColumn
import com.mobileprism.fishing.ui.viewmodels.restore.SearchAccountViewModel
import com.mobileprism.fishing.ui.viewmodels.restore.UserLogin
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.Constants
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchAccountScreen(upPress: () -> Unit, onNext: (UserLogin) -> Unit) {
    val viewModel: SearchAccountViewModel = getViewModel()
    val searchState = viewModel.searchState.collectAsState()
    val confirmState = viewModel.confirmState.collectAsState()
    val loginInfo by viewModel.restoreInfo.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val modalBottomSheetState =
        rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmStateChange = { false }
        )

    LaunchedEffect(confirmState.value) {
        when (val state = confirmState.value) {
            is BaseViewState.Error -> {
                // TODO: error handling
            }
            is BaseViewState.Loading -> {}
            is BaseViewState.Success -> {
                onNext(state.data)
                viewModel.resetStates()
            }
            null -> {}
        }
    }
    LaunchedEffect(searchState.value) {
        if (searchState.value is UiState.Success) {
            modalBottomSheetState.animateTo(targetValue = ModalBottomSheetValue.Expanded)
        }
    }

    ModalBottomSheetLayout(
        modifier = Modifier.imePadding(),
        sheetState = modalBottomSheetState,
        sheetShape = Constants.modalBottomSheetCorners,
        sheetContent = {
            DefaultAuthColumn {
                AuthTopBar({
                    coroutineScope.launch {
                        modalBottomSheetState.animateTo(targetValue = ModalBottomSheetValue.Hidden)
                    }
                }) {
                    HeaderText(
                        modifier = Modifier,
                        text = "Введите код, отправленный вам на почту",
                    )
                }

                Text(
                    modifier = Modifier,
                    text = "Если аккаунт действительно принадлежит вам, то вы сможете восстановить пароль",
                    style = MaterialTheme.typography.body1
                )

                FishingOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = loginInfo.otp,
                    onValueChange = viewModel::onOtpSet,
                    isError = loginInfo.otpError.successful.not(),
                    errorString = loginInfo.otpError.errorMessage,
                )
                AnimatedVisibility(visible = confirmState.value is BaseViewState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Crossfade(confirmState.value is BaseViewState.Loading) {
                        when (it) {
                            true -> {
                                DefaultButtonOutlined(
                                    text = stringResource(R.string.cancel),
                                    onClick = viewModel::cancelConfirm
                                )
                            }
                            else -> {
                                Spacer(modifier = Modifier.size(4.dp))
                            }
                        }
                    }

                    FishingButtonFilled(
                        text = stringResource(R.string.confirm),
                        onClick = viewModel::confirmAccount
                    )
                }

            }
        }) {

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
                    enabled = searchState.value !is UiState.InProgress,
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

                AnimatedVisibility(visible = searchState.value is UiState.InProgress) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Crossfade(searchState.value is UiState.InProgress) {
                        when (it) {
                            true -> {
                                DefaultButtonOutlined(
                                    text = stringResource(R.string.cancel),
                                    onClick = viewModel::cancelSearch
                                )
                            }
                            else -> {
                                Spacer(modifier = Modifier.size(4.dp))
                            }
                        }
                    }

                    FishingButtonFilled(
                        text = stringResource(R.string.find_account),
                        onClick = viewModel::searchAccount
                    )
                }

            }
        }
    }
}