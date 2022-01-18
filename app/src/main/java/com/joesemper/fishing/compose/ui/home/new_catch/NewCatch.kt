package com.joesemper.fishing.compose.ui.home.new_catch

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.ui.home.advertising.BannerAdvertView
import com.joesemper.fishing.compose.ui.home.advertising.showInterstitialAd
import com.joesemper.fishing.compose.ui.home.views.DefaultAppBar
import com.joesemper.fishing.compose.ui.home.views.ModalLoadingDialog
import com.joesemper.fishing.compose.ui.home.views.PhotosView
import com.joesemper.fishing.domain.NewCatchViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.Constants.bottomBannerPadding
import com.joesemper.fishing.utils.Constants.defaultFabBottomPadding
import com.joesemper.fishing.utils.Constants.modalBottomSheetCorners
import com.joesemper.fishing.utils.network.currentConnectivityState
import com.joesemper.fishing.utils.network.observeConnectivityAsFlow
import com.joesemper.fishing.utils.time.toDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel
import java.util.*

object Constants {
    const val TAG = "NEW_CATCH_LOG"
}

@ExperimentalComposeUiApi
@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@Composable
fun NewCatchScreen(
    upPress: () -> Unit,
    receivedPlace: UserMapMarker?,
    navController: NavController
) {

    val place by remember { mutableStateOf(receivedPlace) }
    val viewModel: NewCatchViewModel by viewModel()
    val calendar = Calendar.getInstance()

    var isNull by remember { mutableStateOf(true) }
    place?.let {
        viewModel.marker.value = place; isNull = false
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val connectionState by context.observeConnectivityAsFlow()
        .collectAsState(initial = context.currentConnectivityState)

    SubscribeToProgress(viewModel.uiState, upPress)
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = null) {
        viewModel.date.value = calendar.timeInMillis
    }

    LaunchedEffect(key1 = viewModel.marker.value, key2 = viewModel.date.value, connectionState) {
        viewModel.marker.value?.let {
            if (viewModel.date.value.toDate() != Date().time.toDate()) {
                viewModel.getHistoricalWeather()
            } else {
                viewModel.getWeather()
            }
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Expanded)
    )

    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var currentBottomSheet: BottomSheetNewCatchScreen? by remember { mutableStateOf(null) }

    val closeSheet: () -> Unit = {
        coroutineScope.launch { modalBottomSheetState.hide() }
    }

    val openSheet: (BottomSheetNewCatchScreen) -> Unit = {
        currentBottomSheet = it
        coroutineScope.launch { modalBottomSheetState.show() }
    }

    if (!modalBottomSheetState.isVisible) {
        currentBottomSheet = null
    }

    val loadingDialogState = remember { mutableStateOf(false) }

    DisposableEffect(key1 = null) {
        onDispose { calendar.timeInMillis = Date().time }
    }

    ModalBottomSheetLayout(
        modifier = Modifier,
        sheetShape = modalBottomSheetCorners,
        sheetState = modalBottomSheetState,
        sheetContent = {
            Spacer(modifier = Modifier.height(1.dp))
            currentBottomSheet?.let { currentSheet ->
                NewCatchModalBottomSheetContent(
                    currentScreen = currentSheet,
                    viewModel = viewModel,
                    onCloseBottomSheet = closeSheet
                )
            }
        }
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier,
            topBar = {
                DefaultAppBar(
                    onNavClick = upPress,
                    title = stringResource(R.string.new_catch)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = defaultFabBottomPadding),
                    onClick = {
                        if (viewModel.isInputCorrect()) {
                            loadingDialogState.value = true
                            showInterstitialAd(
                                context = context,
                                onAdLoaded = {
                                    viewModel.createNewUserCatch()
                                }
                            )
                        } else {
                            SnackbarManager.showMessage(R.string.not_all_fields_are_filled)
                        }
                    }) {
                    Icon(
                        Icons.Filled.Done,
                        stringResource(R.string.create),
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            },
            sheetContent = {
                BannerAdvertView(adId = stringResource(R.string.new_catch_admob_banner_id))
            },
            sheetShape = RectangleShape,
            sheetGesturesEnabled = false,
            sheetPeekHeight = 0.dp
        ) {
            ModalLoadingDialog(
                dialogSate = loadingDialogState,
                text = stringResource(id = R.string.saving_new_catch)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(state = scrollState, enabled = true)
                        .padding(horizontal = 16.dp, vertical = 12.dp)

                ) {

                    Places(viewModel, isNull, navController)  //Выпадающий список мест
                    FishAndWeight(viewModel.fishAmount, viewModel.weight)
                    Fishing(viewModel.rod, viewModel.bite, viewModel.lure)
                    DateAndTime(viewModel.date)
                    NewCatchWeatherItem(viewModel)
                    PhotosView(
                        photos = viewModel.images.toList(),
                        onEditClick = { openSheet(BottomSheetNewCatchScreen.EditPhotosScreen) }
                    )
                    Spacer(modifier = Modifier.padding(64.dp))
                }
                Spacer(modifier = Modifier.size(bottomBannerPadding))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SubscribeToProgress(vmUiState: StateFlow<BaseViewState>, upPress: () -> Unit) {
    val errorDialog = rememberSaveable { mutableStateOf(false) }

    val uiState by vmUiState.collectAsState()
    when (uiState) {
        is BaseViewState.Success<*> -> {
            if ((uiState as BaseViewState.Success<*>).data != null) {
                Toast.makeText(
                    LocalContext.current,
                    stringResource(R.string.catch_added_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                upPress()
            }
        }
        is BaseViewState.Loading -> {
//            LoadingDialog()
        }
        is BaseViewState.Error -> {
            ErrorDialog(errorDialog)
            errorDialog.value = true
            Toast.makeText(
                LocalContext.current,
                "Error: ${(uiState as BaseViewState.Error).error.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
