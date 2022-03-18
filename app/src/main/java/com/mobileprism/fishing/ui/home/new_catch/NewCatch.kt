package com.mobileprism.fishing.ui.home.new_catch

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.showErrorToast
import kotlin.reflect.KFunction0

object Constants {
    const val TAG = "NEW_CATCH_LOG"
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SubscribeToNewCatchProgress(
    uiState: BaseViewState<Nothing?>,
    adIsLoadedState: Boolean,
    loadingDialogState: MutableState<Boolean>,
    upPress: () -> Unit,
    onRetry: () -> Unit,
) {
    var errorDialog by rememberSaveable { mutableStateOf(false) }
    if (errorDialog) AddNewCatchErrorDialog(onClose = { errorDialog = false }, onRetry)
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState, adIsLoadedState) {
        when (uiState) {
            is BaseViewState.Success -> {
                if (adIsLoadedState) {
                    SnackbarManager.showMessage(R.string.catch_added_successfully)
                    upPress()
                }
            }
            is BaseViewState.Loading -> {
                loadingDialogState.value = true
            }
            is BaseViewState.Error -> {
                errorDialog = true
                showErrorToast(context, uiState.error?.message)
            }
        }
    }
}
