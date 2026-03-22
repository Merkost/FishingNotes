# New Place Bottom Sheet Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the cramped `NewPlaceDialog` (`Dialog`) with a `ModalBottomSheet` that has a 5×3 color grid, collapsible description, and geocoded placeholder behavior.

**Architecture:** Rewrite `NewPlaceDialog.kt` composable from `Dialog` to `ModalBottomSheet` content. Replace the horizontal `ColorPicker` with a grid-based `ColorGrid` in `ColorPicker.kt`. Update `MapScreen.kt` to pass `cancelAddNewMarker` on dismiss. No ViewModel changes needed.

**Tech Stack:** Kotlin, Compose Multiplatform, Material3 `ModalBottomSheet`

**Spec:** `docs/superpowers/specs/2026-03-22-new-place-bottom-sheet-design.md`

---

### Task 1: Replace `ColorPicker` with `ColorGrid` in ColorPicker.kt

`ColorPicker` is only used in `NewPlaceDialog.kt`, so the old composables are replaced entirely.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/ColorPicker.kt`

- [ ] **Step 1: Replace file contents with `ColorGrid` and `ColorGridItem`**

```kotlin
package com.mobileprism.fishing.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun ColorGrid(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 5,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { color ->
                    ColorGridItem(
                        selected = color == selectedColor,
                        color = color,
                        onClick = { onColorSelected(color) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorGridItem(
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(
                    width = 2.5.dp,
                    color = if (isSystemInDarkTheme()) Color.White
                            else MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = if (color.luminance() < 0.5f) Color.White else Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

This won't compile yet because `NewPlaceDialog.kt` still imports the old `ColorPicker`. That's expected — Task 2 replaces that file entirely.

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/ColorPicker.kt
git commit -m "feat: replace ColorPicker with ColorGrid for 5x3 grid layout"
```

---

### Task 2: Rewrite NewPlaceDialog as bottom sheet content + add string resources

Replace the entire `NewPlaceDialog` composable with `NewPlaceBottomSheetContent` — a composable meant to be placed inside a `ModalBottomSheet` by the caller.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/NewPlaceDialog.kt`
- Modify: `shared/src/commonMain/composeResources/values/strings.xml` (add `color` string if missing)
- Modify: `shared/src/commonMain/composeResources/values-ru/strings.xml` (add `color` string if missing)

**Reference files (read, don't modify):**
- `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapViews.kt:601-626` — `SetPlaceNameResultListener`
- `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapTypes.kt` — `GeocoderResult` sealed class
- `shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/MapViewModel.kt:43-44,110-130,355-357` — `addNewMarkerState`, `addNewMarker()`, `cancelAddNewMarker()`, `resetAddNewMarkerState()`

- [ ] **Step 1: Add `Res.string.color` if missing**

Check `shared/src/commonMain/composeResources/values/strings.xml` for `<string name="color">`. If absent, add:
- `values/strings.xml`: `<string name="color">Color</string>`
- `values-ru/strings.xml`: `<string name="color">Цвет</string>`

- [ ] **Step 2: Replace `NewPlaceDialog.kt` entirely**

```kotlin
package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButton
import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.ui.utils.ColorGrid
import com.mobileprism.fishing.utils.ValidationUtils
import com.mobileprism.fishing.viewmodels.MapViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

val pickerColors = listOf(
    Color(0xFFEC407A),
    Color(0xFFAB47BC),
    Color(0xFF7E57C2),
    Color(0xFF5C6BC0),
    Color(0xFF42A5F5),
    Color(0xFF29B6F6),
    Color(0xFF26C6DA),
    Color(0xFF26A69A),
    Color(0xFF66BB6A),
    Color(0xFF9CCC65),
    Color(0xFFD4E157),
    Color(0xFFFFEE58),
    Color(0xFFFFCA28),
    Color(0xFFFFA726),
    Color(0xFFFF7043)
)

@Composable
fun NewPlaceBottomSheetContent(
    onDismiss: () -> Unit,
) {
    val viewModel: MapViewModel = koinViewModel()
    val currentCameraPosition by viewModel.currentCameraPosition.collectAsState()
    val uiState by viewModel.addNewMarkerState.collectAsState()
    val placeTileState by viewModel.placeTileViewNameState.collectAsState()

    val noNamePlace = stringResource(Res.string.no_name_place)
    val unnamedPlace = stringResource(Res.string.unnamed_place)
    val cantRecognizePlace = stringResource(Res.string.cant_recognize_place)
    val searchingText = stringResource(Res.string.searching)

    val geocoderResult = placeTileState.geocoderResult

    val placeholderText = when (geocoderResult) {
        is GeocoderResult.Success -> geocoderResult.placeName
        GeocoderResult.NoNamePlace -> unnamedPlace
        GeocoderResult.Failed -> cantRecognizePlace
        GeocoderResult.InProgress -> searchingText
    }

    val fallbackName = when (geocoderResult) {
        is GeocoderResult.Success -> geocoderResult.placeName
        else -> noNamePlace
    }

    val geocoderReady = geocoderResult !is GeocoderResult.InProgress
    val saveEnabled = uiState !is UiState.InProgress && geocoderReady

    LaunchedEffect(uiState) {
        when (uiState) {
            UiState.Success -> {
                onDismiss()
                viewModel.resetAddNewMarkerState()
                SnackbarManager.showMessage(Res.string.add_place_success)
            }
            UiState.Error -> {
                viewModel.resetAddNewMarkerState()
                SnackbarManager.showMessage(Res.string.add_new_place_error)
            }
            else -> {}
        }
    }

    var titleValue by remember { mutableStateOf("") }
    var descriptionValue by remember { mutableStateOf("") }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(pickerColors[0]) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.new_place),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                contentDescription = null,
                tint = selectedColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = titleValue,
            onValueChange = {
                if (it.length <= ValidationUtils.MAX_PLACE_NAME_LENGTH) {
                    titleValue = it
                }
            },
            placeholder = { Text(placeholderText) },
            label = { Text(stringResource(Res.string.title)) },
            singleLine = true,
            supportingText = {
                Text(
                    stringResource(
                        Res.string.char_counter,
                        titleValue.length,
                        ValidationUtils.MAX_PLACE_NAME_LENGTH
                    )
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = titleValue.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { titleValue = "" }) {
                        Icon(Icons.Default.Close, Icons.Default.Delete.name)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = if (descriptionExpanded) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        AnimatedVisibility(
            visible = !descriptionExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            TextButton(onClick = { descriptionExpanded = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(Res.string.description),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = descriptionExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = descriptionValue,
                onValueChange = { descriptionValue = it },
                label = { Text(stringResource(Res.string.description)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.color),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        ColorGrid(
            colors = pickerColors,
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            DefaultButton(
                modifier = Modifier.padding(end = 8.dp),
                text = stringResource(Res.string.cancel),
                onClick = {
                    viewModel.cancelAddNewMarker()
                    onDismiss()
                }
            )

            DefaultButtonFilled(
                text = stringResource(Res.string.save),
                enabled = saveEnabled,
                onClick = {
                    val trimmedTitle = titleValue.trim()
                    viewModel.addNewMarker(
                        RawMapMarker(
                            title = trimmedTitle.ifEmpty { fallbackName },
                            description = descriptionValue,
                            latitude = currentCameraPosition.latitude,
                            longitude = currentCameraPosition.longitude,
                            markerColor = selectedColor.value.hashCode()
                        )
                    )
                }
            )
        }

        DisposableEffect(Unit) {
            focusRequester.requestFocus()
            onDispose { }
        }
    }
}
```

- [ ] **Step 3: Commit**

Won't compile yet — `MapScreen.kt` still references old `NewPlaceDialog`. That's fixed in Task 3.

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/NewPlaceDialog.kt
git add shared/src/commonMain/composeResources/values/strings.xml
git add shared/src/commonMain/composeResources/values-ru/strings.xml
git commit -m "feat: rewrite NewPlaceDialog as bottom sheet content with color grid"
```

---

### Task 3: Update MapScreen to use ModalBottomSheet

Replace `NewPlaceDialog(dialogState, onDismiss)` in `MapScreen.kt` with a `ModalBottomSheet` wrapping `NewPlaceBottomSheetContent`.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapScreen.kt`

**Approach:** `MapControls` doesn't have a direct `viewModel` parameter, but `MapScreen` (the parent) does. Add an `onNewPlaceDialogCancel` callback from `MapScreen` that calls `viewModel.cancelAddNewMarker()` + dismisses. Thread it to `MapControls`. This keeps the ViewModel access at the composable level where it's available.

- [ ] **Step 1: Update `MapScreen` composable to pass cancel callback**

In `MapScreen` (~line 175-181), change how callbacks are passed to `MapControls`:

```kotlin
onNewPlaceDialogCancel = {
    viewModel.cancelAddNewMarker()
    newPlaceDialog = false
},
onNewPlaceDialogDismiss = { newPlaceDialog = false },
```

- [ ] **Step 2: Update `MapControls` signature**

Add `onNewPlaceDialogCancel: () -> Unit` parameter to `MapControls` (around line 197).

Updated signature:
```kotlin
@Composable
private fun MapControls(
    mapUiState: MapUiState,
    viewModel: MapViewModel,
    userPreferences: UserPreferences,
    useZoomButtons: Boolean,
    mapLayersSelection: Boolean,
    onMapLayersSelectionChanged: (Boolean) -> Unit,
    onMapSettingsClicked: () -> Unit,
    newPlaceDialog: Boolean,
    onNewPlaceDialogCancel: () -> Unit,
    onNewPlaceDialogDismiss: () -> Unit,
    place: com.mobileprism.fishing.domain.entity.content.UserMapMarker? = null,
)
```

- [ ] **Step 3: Replace `NewPlaceDialog` call with `ModalBottomSheet`**

Replace line 290-291:
```kotlin
NewPlaceDialog(dialogState = newPlaceDialog, onDismiss = onNewPlaceDialogDismiss)
```

With:
```kotlin
if (newPlaceDialog) {
    ModalBottomSheet(
        onDismissRequest = onNewPlaceDialogCancel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = Constants.modalBottomSheetCorners,
    ) {
        NewPlaceBottomSheetContent(onDismiss = onNewPlaceDialogDismiss)
    }
}
```

Add import if missing: `import androidx.compose.material3.rememberModalBottomSheetState`

- [ ] **Step 4: Verify full project compiles**

Run: `./gradlew :shared:compileKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapScreen.kt
git commit -m "feat: integrate new place bottom sheet into MapScreen"
```

---

### Task 4: Clean up and verify

**Files:**
- Verify: all modified files

- [ ] **Step 1: Remove unused imports from all modified files**

Check `NewPlaceDialog.kt` for any leftover unused imports (e.g., `Dialog`, `MyCard`, `Shapes`, `secondaryFigmaColor`, `VisualTransformation`).

Check `ColorPicker.kt` — should be clean from Task 1.

- [ ] **Step 2: Verify full project builds**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Manual QA checklist**

Test on device/emulator:
1. Open map → tap FAB → enter place selection mode → tap FAB again (confirm) → bottom sheet slides up
2. Title field is empty, placeholder shows geocoded address
3. Type a name → character counter updates → clear button appears
4. Tap "+ Add description" → description field animates in
5. Tap colors in grid → marker icon in header changes color, selected color gets ring
6. Leave title empty → save → place saved with geocoded name
7. Swipe down to dismiss → no phantom save, marker not created
8. Save with InProgress geocoder → save button should be disabled
9. Save successfully → bottom sheet dismisses, snackbar shows success message
10. Save error → snackbar shows error, sheet stays open

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "chore: clean up unused imports from new place bottom sheet migration"
```
