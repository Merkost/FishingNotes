package com.mobileprism.fishing.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.catch.EditNoteDialog
import com.mobileprism.fishing.ui.home.notes.*
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.components.state.EmptyState
import com.mobileprism.fishing.ui.components.CardSkeleton
import com.mobileprism.fishing.ui.components.SkeletonLine
import com.mobileprism.fishing.ui.viewmodels.UserPlaceViewModel
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.utils.Constants.bottomBannerPadding
import com.mobileprism.fishing.utils.time.toDateTextMonth
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun PlaceTitleView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    catchesAmount: Int,
    navigateToMap: () -> Unit
) {
    DetailHeader(
        modifier = modifier,
        leadingIcon = painterResource(Res.drawable.ic_baseline_location_on_24),
        leadingIconTint = Color(place.markerColor),
        leadingIconContentDescription = stringResource(Res.string.place),
        title = place.title,
        subtitle = {
            AppText(
                text = place.dateOfCreation.toDateTextMonth(),
                style = AppTextStyle.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        },
        metric = {
            ItemCounter(
                count = catchesAmount,
                icon = Res.drawable.ic_fishing,
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        trailingAction = {
            AppIconButton(
                onClick = navigateToMap,
                icon = painterResource(Res.drawable.ic_place_on_map),
                contentDescription = stringResource(Res.string.navigate)
            )
        }
    )
}

@ExperimentalComposeUiApi
@Composable
fun NoteModalBottomSheet(
    viewModel: UserPlaceViewModel,
    onCloseBottomSheet: () -> Unit,
) {
    val currentNote by viewModel.currentNote.collectAsState()
    EditNoteDialog(
        note = currentNote ?: Note(),
        onSaveNote = viewModel::updateMarkerNotes,
        deleteOption = (currentNote ?: Note()).id.isNotEmpty(),
        onDeleteNote = viewModel::deleteMarkerNote,
        onCloseDialog = onCloseBottomSheet
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun PlaceNotes(
    notes: List<Note>?,
    onNoteSelected: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.screenH, vertical = Spacing.sm)
    ) {
        item {
            AddItemButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm),
                text = stringResource(Res.string.add_note),
                onClick = { onNoteSelected(Note()) }
            )
        }

        val notesList = notes.orEmpty()
        if (notesList.isEmpty()) {
            item {
                EmptyState(
                    illustration = painterResource(Res.drawable.ic_no_note),
                    title = stringResource(Res.string.no_notes_added),
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(top = Spacing.xl)
                )
            }
        } else {
            items(notesList) { note ->
                DefaultNoteView(
                    modifier = Modifier.padding(bottom = Spacing.listItemGap),
                    note = note,
                    onClick = { onNoteSelected(note) }
                )
            }
        }

        item { Spacer(modifier = Modifier.size(bottomBannerPadding)) }
    }
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun PlaceCatchesView(
    modifier: Modifier = Modifier,
    catches: List<UserCatch>,
    onNewCatchClick: () -> Unit,
    userCatchClicked: (UserCatch) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = Spacing.xs),
    ) {
        when {
            catches.isNotEmpty() -> {
                getDatesList(catches).forEach { catchDate ->
                    stickyHeader {
                        ItemDate(text = catchDate)
                    }
                    items(items = catches
                        .filter { userCatch ->
                            userCatch.date.toDateTextMonth() == catchDate
                        }
                        .sortedByDescending { it.date },
                        key = { it.id }
                    ) {
                        CatchItemView(
                            catch = it,
                            showPlace = false,
                            onClick = { userCatch -> userCatchClicked(userCatch) },
                            childModifier = Modifier
                        )
                    }

                }
                item { Spacer(modifier = Modifier.size(bottomBannerPadding)) }
            }
            catches.isEmpty() -> {
                item {
                    EmptyState(
                        illustration = painterResource(Res.drawable.ic_fishing),
                        title = stringResource(Res.string.no_cathces_added),
                        action = {
                            AddItemButton(
                                text = stringResource(Res.string.new_catch),
                                onClick = onNewCatchClick
                            )
                        },
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = Spacing.xl)
                    )
                }
                item { Spacer(modifier = Modifier.size(bottomBannerPadding)) }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaceButtonsView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    navController: NavController,
    viewModel: UserPlaceViewModel
) {
    val analyticsTracker = com.mobileprism.fishing.ui.utils.LocalAnalytics.current

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Spacing.screenH),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            AppButton(
                text = stringResource(Res.string.new_catch),
                onClick = { newCatchClicked(navController, place) },
                style = AppButtonStyle.Outlined,
                leadingIcon = painterResource(Res.drawable.ic_add_catch)
            )
        }
        item {
            AppButton(
                text = stringResource(Res.string.navigate),
                onClick = { openMapNavigation(place, analyticsTracker) },
                style = AppButtonStyle.Outlined,
                leadingIcon = painterResource(Res.drawable.ic_baseline_navigation_24)
            )
        }
        item {
            AppButton(
                text = stringResource(Res.string.share),
                onClick = { shareMarkerLocation(place, analyticsTracker) },
                style = AppButtonStyle.Outlined,
                leadingIcon = painterResource(Res.drawable.ic_baseline_share_24)
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun DeletePlaceDialog(
    place: UserMapMarker,
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit
) {
    DefaultDialog(
        primaryText = stringResource(Res.string.delete_place_dialog, place.title),
        secondaryText = stringResource(Res.string.sure_delete_place_dialog),
        negativeButtonText = stringResource(Res.string.no),
        onNegativeClick = onDismiss,
        positiveButtonText = stringResource(Res.string.yes),
        onPositiveClick = onPositiveClick,
        onDismiss = onDismiss,
        content = {
            LottieWarning(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceTopBar(
    backPress: () -> Unit,
    viewModel: UserPlaceViewModel,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
) {
    val isVisible by viewModel.markerVisibility.collectAsState()

    val color = animateColorAsState(
        targetValue = if (isVisible == true) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(800)
    )

    AppTopBar(
        modifier = modifier,
        title = stringResource(Res.string.place),
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = backPress,
        actions = {
            IconToggleButton(
                checked = isVisible ?: true,
                onCheckedChange = { viewModel.changeVisibility(it) }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_remove_red_eye_24),
                    contentDescription = stringResource(Res.string.toggle_visibility),
                    tint = color.value
                )
            }
            AppBarOverflowMenu(
                items = listOf(
                    OverflowMenuItem(
                        label = stringResource(Res.string.delete),
                        onClick = onDelete,
                        leadingIcon = Icons.Outlined.Delete,
                        tint = MaterialTheme.colorScheme.error
                    )
                )
            )
        }
    )
}

@Composable
fun PlaceDetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.screenH, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SkeletonLine(modifier = Modifier.fillMaxWidth(0.6f))
        SkeletonLine(modifier = Modifier.fillMaxWidth(0.4f))
        Spacer(modifier = Modifier.size(Spacing.sm))
        repeat(3) {
            CardSkeleton(modifier = Modifier.fillMaxWidth())
        }
    }
}
