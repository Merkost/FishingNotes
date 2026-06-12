---
# Core Components I — Primitives & State System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the tokens-driven, M3-based core component primitives (`AppButton`, `AppIconButton`, `AppText`, `AppCard`, `SectionCard`, `FormTextField`, `PickerField`) and the state/list system (`EmptyState`/`ErrorState`/`LoadingState`, `InlineLoader`/`ListAppendLoader`, `ScreenStateContent<T>`, `PagedListScaffold<T>`, `SkeletonBox`/`SkeletonLine` + component skeletons) in `commonMain`, alongside the existing legacy components, so screen plans can adopt them incrementally.

**Architecture:** Each component is a thin, styled wrapper over a Material3 primitive (Button/IconButton/Text/Card/OutlinedTextField) consuming the Plan 01 foundation tokens (`Spacing`, `Elevation`, `Emphasis`, `Motion`, `MaterialTheme.shapes`, `MaterialTheme.typography`) and M3 color roles — never hardcoded `Dp`/`Color`/alpha. State components own viewport centering, retry affordances, and skeleton→content crossfade so screens stop hand-assembling these. The legacy duplicates in `Buttons.kt`/`Text.kt`/`Cards.kt`/`TextFields.kt`/`DefaultViews.kt` stay untouched in this plan; their deletion is gated by Plan 11.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3, Koin, Compose Resources. All shared UI in commonMain (KMP/iOS-safe).

**Sprint:** S1 · Design System · **Plan 02 of 11**

**Depends on:** Plan 01 (Foundation tokens — `Spacing`, `Elevation`, `Emphasis`, `Motion`, updated `Shape`/`Color`/`Type`). This plan CONSUMES those tokens by name; if a token referenced below is not yet present, that is a Plan 01 gap to flag, not to redefine here.

---
---

## File Structure

| File | Action | Single responsibility |
| --- | --- | --- |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppButton.kt` | Create | `AppButton` + `AppButtonStyle` enum (Filled/Tonal/Outlined/Text), built-in loading spinner + leading icon |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppIconButton.kt` | Create | `AppIconButton` — single icon-only button, ≥48dp target, required non-null `contentDescription` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppText.kt` | Create | `AppText` + `AppTextStyle` enum, `TextWithLeadingIcon` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppCard.kt` | Create | `AppCard` (single tokenized card) + `SectionCard` (icon header + content) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/FormTextField.kt` | Create | `FormTextField` wrapper over `OutlinedTextField` + `PickerField` read-only field |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/StateViews.kt` | Create | `EmptyState` (+ `NoPhotos`/`NoPlaces`/`NoCatches` presets), `ErrorState` (+ `NoInternet` preset), `LoadingState`, `InlineLoader`, `ListAppendLoader` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/ScreenStateContent.kt` | Create | `ScreenStateContent<T>` Crossfade over `BaseViewState<T>` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/PagedListScaffold.kt` | Create | `PagedListScaffold<T>` — generic paged list state machine (refresh/error/empty/append + grouping + FAB inset) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/Skeletons.kt` | Create | `SkeletonBox`, `SkeletonLine`, `CardSkeleton`, `ListItemSkeleton` |
| `shared/src/commonMain/composeResources/values/strings.xml` | Modify | Add `no_places_added`, `no_photos_added`, `empty_state_no_catches_title`, etc. only if absent (see Task 6) |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/components/AppTextStyleTest.kt` | Create | Unit test for `AppTextStyle` → M3 role mapping (pure logic) |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/components/AppButtonStyleTest.kt` | Create | Unit test asserting `AppButtonStyle` enum entry set (pure logic) |

> **Ownership note (cross-plan):** Token objects `Spacing`, `Elevation`, `Emphasis`, `Motion` and the updated `Shape`/`Color`/`Type` are owned by Plan 01. This plan only *reads* them via `com.mobileprism.fishing.ui.theme.*`. Do not redefine them. Navigation/bars/chrome components (`AppBottomNavigation`, `AppTopBar`, `AppScaffold`, `BottomActionBar`, etc.) and brand/content components (`BrandFab`, `StatTile`, `ChartCard`, etc.) are owned by Plan 03; do not build them here.

---

## Tasks

### Task 1: `AppButton` + `AppButtonStyle`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppButton.kt`

Replaces (eventually, in Plan 11) `DefaultButton`, `DefaultButtonFilled`, `DefaultButtonOutlined`, `DefaultButtonSecondaryLight`, `LoadingIconButtonOutlined` in `Buttons.kt` and the login/onboarding one-off CTAs. No forced uppercase; built-in loading spinner; ≥48dp via M3 `Button` defaults.

- [ ] **Step 1: Create the file with the enum and component.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppButton.kt` with exactly this content:

```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.mobileprism.fishing.ui.theme.Spacing

enum class AppButtonStyle { Filled, Tonal, Outlined, Text }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Filled,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: Painter? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current,
                )
            } else if (leadingIcon != null) {
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(text = text, maxLines = 1)
        }
    }

    val effectiveEnabled = enabled && !loading

    when (style) {
        AppButtonStyle.Filled -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = effectiveEnabled,
        ) { content() }

        AppButtonStyle.Tonal -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = effectiveEnabled,
        ) { content() }

        AppButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = effectiveEnabled,
        ) { content() }

        AppButtonStyle.Text -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = effectiveEnabled,
        ) { content() }
    }
}
```

- [ ] **Step 2: Add the missing `dp` import.** The body uses `18.dp`/`2.dp`. Add `import androidx.compose.ui.unit.dp` to the import block (keep imports alphabetically grouped with the others). Confirm `ButtonDefaults` import is unused and remove it if the compiler warns (it is only kept here for discoverability — delete the line `import androidx.compose.material3.ButtonDefaults` to avoid an unused-import warning).

- [ ] **Step 3: Verify (compile Android).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`, no errors referencing `AppButton.kt`. (Task name confirmed to exist in `:shared`.)

- [ ] **Step 4: Verify (iOS source set compiles).** Run:
```
./gradlew :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`. Confirms no Android-only API leaked into commonMain.

- [ ] **Step 5: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppButton.kt
git commit -m "Add AppButton + AppButtonStyle core primitive"
```

---

### Task 2: `AppIconButton`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppIconButton.kt`

The single icon-only button. Enforces a 48dp minimum hit target (M3 `IconButton` already sizes to 48dp; we additionally apply `sizeIn` to guarantee it under tighter parent constraints) and requires a non-null `contentDescription`.

- [ ] **Step 1: Create the file.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppIconButton.kt`:

```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        enabled = enabled,
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
```

- [ ] **Step 2: Verify (compile Android).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Verify (iOS source set compiles).** Run:
```
./gradlew :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppIconButton.kt
git commit -m "Add AppIconButton with enforced 48dp target and required contentDescription"
```

---

### Task 3: `AppText` + `AppTextStyle` + `TextWithLeadingIcon`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppText.kt`
- Test: `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/components/AppTextStyleTest.kt`

Collapses the 14 text composables in `Text.kt`. `AppTextStyle` maps each role to an `MaterialTheme.typography` style. `TextWithLeadingIcon` replaces `SubtitleWithIcon`.

- [ ] **Step 1: Create the component.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppText.kt`:

```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

enum class AppTextStyle {
    Display, Heading, Title, Subtitle, Body, BodySmall, Caption, Support
}

@Composable
@ReadOnlyComposable
private fun AppTextStyle.toTextStyle(): TextStyle = when (this) {
    AppTextStyle.Display -> MaterialTheme.typography.displaySmall
    AppTextStyle.Heading -> MaterialTheme.typography.headlineSmall
    AppTextStyle.Title -> MaterialTheme.typography.titleLarge
    AppTextStyle.Subtitle -> MaterialTheme.typography.titleMedium
    AppTextStyle.Body -> MaterialTheme.typography.bodyLarge
    AppTextStyle.BodySmall -> MaterialTheme.typography.bodyMedium
    AppTextStyle.Caption -> MaterialTheme.typography.bodySmall
    AppTextStyle.Support -> MaterialTheme.typography.labelMedium
}

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: AppTextStyle = AppTextStyle.Body,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.toTextStyle(),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun TextWithLeadingIcon(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
    style: AppTextStyle = AppTextStyle.Subtitle,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentDescription: String? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        AppText(text = text, style = style, color = color)
    }
}
```

- [ ] **Step 2: Add the test mirror of the role mapping.** Because `toTextStyle` is a `@Composable` (cannot be unit-tested without a Compose runtime), the test asserts the *contract* — that the enum has exactly the 8 documented entries in the documented order, so screen plans can rely on the set. Write `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/components/AppTextStyleTest.kt`:

```kotlin
package com.mobileprism.fishing.ui.components

import com.mobileprism.fishing.ui.home.views.AppTextStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTextStyleTest {

    @Test
    fun appTextStyleHasEightRoles() {
        assertEquals(8, AppTextStyle.entries.size)
    }

    @Test
    fun appTextStyleEntriesInDocumentedOrder() {
        assertEquals(
            listOf(
                "Display", "Heading", "Title", "Subtitle",
                "Body", "BodySmall", "Caption", "Support",
            ),
            AppTextStyle.entries.map { it.name },
        )
    }
}
```

- [ ] **Step 3: Run the unit test.** Run:
```
./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.components.AppTextStyleTest"
```
Expected: `BUILD SUCCESSFUL`, 2 tests pass. (Task `testDebugUnitTest` confirmed to exist.)

- [ ] **Step 4: Verify (compile Android + iOS).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppText.kt shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/components/AppTextStyleTest.kt
git commit -m "Add AppText + AppTextStyle + TextWithLeadingIcon with role-mapping test"
```

---

### Task 4: `AppCard` + `SectionCard`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppCard.kt`

Single tokenized card replacing the 5 `Cards.kt` variants (`MyCard`, `MyCardNoPadding`, `DefaultCard`, `DefaultCardClickable`, `MyClickableCard`). `SectionCard` = card + icon header + content with an optional edit affordance when `onClick != null`.

- [ ] **Step 1: Create the file.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppCard.kt`. Note: uses Plan 01's `Elevation.card` token and `Spacing.cardPadding`; shape defaults to `MaterialTheme.shapes.medium`.

```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: CornerBasedShape = MaterialTheme.shapes.medium,
    elevation: Dp = Elevation.card,
    contentPadding: Dp = Spacing.cardPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    val elevationValues = CardDefaults.cardElevation(defaultElevation = elevation)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            elevation = elevationValues,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            elevation = elevationValues,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun SectionCard(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            AppText(
                text = title,
                style = AppTextStyle.Subtitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            when {
                trailing != null -> trailing()
                onClick != null -> AppIconButton(
                    onClick = onClick,
                    icon = androidx.compose.material.icons.Icons.Default.Edit.let {
                        androidx.compose.ui.graphics.vector.rememberVectorPainter(it)
                    },
                    contentDescription = title,
                )
            }
        }
        Column(
            modifier = Modifier.padding(top = Spacing.md),
            content = content,
        )
    }
}
```

- [ ] **Step 2: Fix the edit-icon import to be KMP-safe.** The inline `Icons.Default.Edit` reference above uses `material-icons-core`. Confirm `material-icons` is available in commonMain by grepping: `grep -rn "androidx.compose.material.icons" shared/src/commonMain/kotlin | head`. If it resolves elsewhere in commonMain, replace the `trailing == null && onClick != null` branch with a clean import-based version. Edit the `onClick != null ->` arm to:

```kotlin
                onClick != null -> AppIconButton(
                    onClick = onClick,
                    icon = rememberVectorPainter(Icons.Default.Edit),
                    contentDescription = title,
                )
```
and add these imports to the top of the file:
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.rememberVectorPainter
```
If the grep in this step shows `androidx.compose.material.icons` is NOT available in commonMain, instead change the `icon` parameter to a `Res.drawable` edit icon: add `import fishing.shared.generated.resources.Res`, `import fishing.shared.generated.resources.*`, `import org.jetbrains.compose.resources.painterResource`, and use `icon = painterResource(Res.drawable.ic_baseline_edit_24)` if it exists (grep `ls shared/src/commonMain/composeResources/drawable/ | grep -i edit`); otherwise fall back to the existing `Res.drawable.ic_baseline_navigation_24` is wrong — pick the closest edit/pencil drawable found and record which one in the commit message.

- [ ] **Step 3: Add the `weight` import.** `Modifier.weight(1f)` requires `RowScope`. It is already in scope inside the `Row { }` lambda, so no extra import is needed; verify by compiling.

- [ ] **Step 4: Verify (compile Android).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`. If `Icons.Default.Edit` does not resolve, apply the `painterResource` fallback from Step 2 and re-run.

- [ ] **Step 5: Verify (iOS source set compiles).** Run:
```
./gradlew :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppCard.kt
git commit -m "Add AppCard + SectionCard single tokenized card primitives"
```

---

### Task 5: `FormTextField` + `PickerField`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/FormTextField.kt`

`FormTextField` = one wrapper over `OutlinedTextField` (replaces hand-rolled fields in ~9 files). `PickerField` = read-only "tap to open a picker" field that removes the disabled-colors hack in `SimpleUnderlineTextField`.

- [ ] **Step 1: Create the file.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/FormTextField.kt`:

```kotlin
package com.mobileprism.fishing.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: Painter? = null,
    readOnly: Boolean = false,
    isError: Boolean = false,
    singleLine: Boolean = true,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Next,
    ),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = leadingIcon?.let {
            { Icon(painter = it, contentDescription = null) }
        },
        readOnly = readOnly,
        isError = isError,
        singleLine = singleLine,
        supportingText = supportingText?.let { { Text(it) } },
        shape = MaterialTheme.shapes.small,
        keyboardOptions = keyboardOptions,
    )
}

@Composable
fun PickerField(
    value: String,
    label: String,
    leadingIcon: Painter?,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) onClick()
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = leadingIcon?.let {
            { Icon(painter = it, contentDescription = null) }
        },
        readOnly = true,
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        interactionSource = interactionSource,
    )
}
```

- [ ] **Step 2: Confirm `LocalContentColor` import is unused.** The body does not use `LocalContentColor`; remove the line `import androidx.compose.material3.LocalContentColor` to avoid an unused-import warning.

- [ ] **Step 3: Verify (compile Android + iOS).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/FormTextField.kt
git commit -m "Add FormTextField + PickerField input primitives"
```

---

### Task 6: Add missing empty-state string resources

**Files:**
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`

The `EmptyState` presets in Task 7 need titles for No Places and No Photos. `no_cathces_added`, `note`, `place`, `retry`, `something_went_wrong`, `network_error_message` already exist. Add only what is missing.

- [ ] **Step 1: Check which strings already exist.** Run:
```
grep -nE 'name="(no_places_added|no_photos_added)"' shared/src/commonMain/composeResources/values/strings.xml
```
If both already exist, skip to Step 3 (no edit needed) and note that in the commit.

- [ ] **Step 2: Add the missing strings.** Insert these two lines into `shared/src/commonMain/composeResources/values/strings.xml`, immediately after the existing `<string name="no_cathces_added">No catches added</string>` line:
```xml
    <string name="no_places_added">No places added</string>
    <string name="no_photos_added">No photos added</string>
```
(If only one is missing, add only that one.)

- [ ] **Step 3: Verify resource accessors regenerate.** Run:
```
./gradlew :shared:generateResourceAccessorsForAndroidMain
```
Expected: `BUILD SUCCESSFUL`. If this exact task name is not found, fall back to `./gradlew :shared:compileDebugKotlinAndroid` which triggers accessor generation as a dependency. Confirm `Res.string.no_places_added` and `Res.string.no_photos_added` are now usable (they will be referenced in Task 7).

- [ ] **Step 4: Commit.**
```
git add shared/src/commonMain/composeResources/values/strings.xml
git commit -m "Add no_places_added/no_photos_added empty-state strings"
```

---

### Task 7: State views — `EmptyState`, `ErrorState`, `LoadingState`, `InlineLoader`, `ListAppendLoader`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/StateViews.kt`

Replaces `NoContentView`+per-screen `Spacer`+CTA, `ErrorView`, and `NoInternetView` (those legacy versions stay until Plan 11). `EmptyState` and `ErrorState` center themselves; the `padding(top = 128.dp)` hack is gone. Presets `NoPhotos`/`NoPlaces`/`NoCatches` (on `EmptyState`) and `NoInternet` (on `ErrorState`). `ModalLoadingDialog` (in `Dialogs.kt`) is kept for blocking writes only and is NOT touched here.

- [ ] **Step 1: Create the file.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/StateViews.kt`. Note the package is `com.mobileprism.fishing.ui.components.state`. Uses existing drawables `ic_no_photos`, `ic_no_place_on_map`, `ic_fishing`, `ic_no_internet`, `ic_error` and the existing/added strings.

```kotlin
package com.mobileprism.fishing.ui.components.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import com.mobileprism.fishing.ui.home.views.AppText
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.ic_error
import fishing.shared.generated.resources.ic_fishing
import fishing.shared.generated.resources.ic_no_internet
import fishing.shared.generated.resources.ic_no_photos
import fishing.shared.generated.resources.ic_no_place_on_map
import fishing.shared.generated.resources.network_error_message
import fishing.shared.generated.resources.no_cathces_added
import fishing.shared.generated.resources.no_photos_added
import fishing.shared.generated.resources.no_places_added
import fishing.shared.generated.resources.retry
import fishing.shared.generated.resources.something_went_wrong
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyState(
    illustration: Painter,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                painter = illustration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp),
            )
            AppText(
                text = title,
                style = AppTextStyle.Title,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (description != null) {
                AppText(
                    text = description,
                    style = AppTextStyle.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (action != null) action()
        }
    }
}

@Composable
fun EmptyState.NoCatches(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) = EmptyState(
    illustration = painterResource(Res.drawable.ic_fishing),
    title = stringResource(Res.string.no_cathces_added),
    modifier = modifier,
    action = action,
)

@Composable
fun EmptyStateNoCatches(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) = EmptyState(
    illustration = painterResource(Res.drawable.ic_fishing),
    title = stringResource(Res.string.no_cathces_added),
    modifier = modifier,
    action = action,
)

@Composable
fun EmptyStateNoPlaces(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) = EmptyState(
    illustration = painterResource(Res.drawable.ic_no_place_on_map),
    title = stringResource(Res.string.no_places_added),
    modifier = modifier,
    action = action,
)

@Composable
fun EmptyStateNoPhotos(
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) = EmptyState(
    illustration = painterResource(Res.drawable.ic_no_photos),
    title = stringResource(Res.string.no_photos_added),
    modifier = modifier,
    action = action,
)

@Composable
fun ErrorState(
    message: String,
    illustration: Painter,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                painter = illustration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp),
            )
            AppText(
                text = message,
                style = AppTextStyle.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (onRetry != null) {
                AppButton(
                    text = stringResource(Res.string.retry),
                    onClick = onRetry,
                    style = AppButtonStyle.Tonal,
                )
            }
        }
    }
}

@Composable
fun ErrorStateGeneric(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) = ErrorState(
    message = stringResource(Res.string.something_went_wrong),
    illustration = painterResource(Res.drawable.ic_error),
    modifier = modifier,
    onRetry = onRetry,
)

@Composable
fun ErrorStateNoInternet(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) = ErrorState(
    message = stringResource(Res.string.network_error_message),
    illustration = painterResource(Res.drawable.ic_no_internet),
    modifier = modifier,
    onRetry = onRetry,
)

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun InlineLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp))
    }
}

@Composable
fun ListAppendLoader(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        if (isError && onRetry != null) {
            AppButton(
                text = stringResource(Res.string.retry),
                onClick = onRetry,
                style = AppButtonStyle.Text,
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
    }
}
```

- [ ] **Step 2: Remove the erroneous extension preset.** The `fun EmptyState.NoCatches(...)` declaration above is intentionally invalid (an extension on a composable function is not valid Kotlin). Delete that entire block (the `@Composable fun EmptyState.NoCatches(...)` function) — keep only the top-level `EmptyStateNoCatches`, `EmptyStateNoPlaces`, `EmptyStateNoPhotos` presets and the `ErrorState*` presets. Also remove the unused import `import androidx.compose.material3.MaterialTheme` only if the compiler flags it (it is used by `colorScheme`, so it should stay).

- [ ] **Step 3: Confirm drawable accessors exist.** Run:
```
ls shared/src/commonMain/composeResources/drawable/ | grep -iE "ic_no_photos|ic_no_place_on_map|ic_fishing\.|ic_no_internet|ic_error"
```
Expected: all five files listed. If any is missing, adjust the corresponding `Res.drawable.*` reference to the closest existing drawable and note the substitution in the commit message.

- [ ] **Step 4: Verify (compile Android).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Verify (iOS source set compiles).** Run:
```
./gradlew :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/StateViews.kt
git commit -m "Add EmptyState/ErrorState/LoadingState/InlineLoader/ListAppendLoader with centered viewport"
```

---

### Task 8: `SkeletonBox`, `SkeletonLine`, `CardSkeleton`, `ListItemSkeleton`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/Skeletons.kt`

Built on the existing KMP-safe `Modifier.placeholder()` (`com.mobileprism.fishing.ui.utils.placeholder`). These are generic skeleton primitives; the existing `PlaceItemSkeleton`/`CatchItemSkeleton` in `NotesViews.kt` stay until their screens migrate (Plan 11 deletes legacy).

- [ ] **Step 1: Create the file.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/Skeletons.kt`:

```kotlin
package com.mobileprism.fishing.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.placeholder

@Composable
fun SkeletonBox(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape,
            )
    )
}

@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    fraction: Float = 1f,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(
        modifier = modifier
            .fillMaxWidth(fraction)
            .height(height)
            .placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape,
            )
    )
}

@Composable
fun CardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        SkeletonLine(fraction = 0.5f, height = 18.dp)
        SkeletonLine(fraction = 0.9f)
        SkeletonLine(fraction = 0.7f)
    }
}

@Composable
fun ListItemSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBox(width = 48.dp, height = 48.dp, shape = RoundedCornerShape(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            SkeletonLine(fraction = 0.6f, height = 16.dp)
            SkeletonLine(fraction = 0.4f)
        }
    }
}
```

- [ ] **Step 2: Confirm the placeholder import path.** Run:
```
grep -n "fun Modifier.placeholder" shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/PlaceholderModifier.kt
```
Expected: a match on line 21, confirming `com.mobileprism.fishing.ui.utils.placeholder` is the correct import. If the package differs, fix the import in `Skeletons.kt`.

- [ ] **Step 3: Verify (compile Android + iOS).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/Skeletons.kt
git commit -m "Add SkeletonBox/SkeletonLine + CardSkeleton/ListItemSkeleton primitives"
```

---

### Task 9: `ScreenStateContent<T>`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/ScreenStateContent.kt`

Crossfade between Loading/Error/Content (and optional Empty) for `BaseViewState<T>`-driven screens. `BaseViewState` is `Success<T>`/`Error(Throwable?)`/`Loading(progress)` (confirmed in `viewstates/BaseViewState.kt`). The optional `empty` predicate lets callers route a successful-but-empty payload to an empty slot.

- [ ] **Step 1: Create the file.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/ScreenStateContent.kt`:

```kotlin
package com.mobileprism.fishing.ui.components.state

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.Motion
import com.mobileprism.fishing.ui.viewstates.BaseViewState

@Composable
fun <T> ScreenStateContent(
    state: BaseViewState<T>,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { LoadingState() },
    error: @Composable (Throwable?) -> Unit = { ErrorStateGeneric() },
    isEmpty: (T) -> Boolean = { false },
    empty: @Composable () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    Crossfade(
        targetState = state,
        modifier = modifier,
        animationSpec = Motion.crossfade,
        label = "ScreenStateContent",
    ) { current ->
        when (current) {
            is BaseViewState.Loading -> loading()
            is BaseViewState.Error -> error(current.error)
            is BaseViewState.Success -> {
                if (isEmpty(current.data)) empty() else content(current.data)
            }
        }
    }
}
```

- [ ] **Step 2: Confirm the `Motion.crossfade` token exists (Plan 01 ownership).** Run:
```
grep -rn "crossfade\|val medium\|object Motion" shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Motion.kt
```
- If `Motion.crossfade` exists, keep the code as-is.
- If `Motion` exists but has no `crossfade` (only `short`/`medium`/`long` durations), change the `animationSpec` line to `animationSpec = androidx.compose.animation.core.tween(durationMillis = Motion.medium)` and add `import androidx.compose.animation.core.tween`. Use whichever duration field Plan 01 actually defined (`Motion.medium` per spec §4.4).
- If `Motion.kt` does not exist yet, this is a Plan 01 dependency gap: temporarily inline `animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)` with `import androidx.compose.animation.core.tween`, and record this as a follow-up in the commit message so it can be re-pointed at the token once Plan 01 lands.

- [ ] **Step 3: Verify (compile Android + iOS).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/ScreenStateContent.kt
git commit -m "Add ScreenStateContent<T> crossfade over BaseViewState"
```

---

### Task 10: `PagedListScaffold<T>`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/PagedListScaffold.kt`

The single generic paged-list state machine eliminating the verbatim duplication between `UserCatchesScreen` and `UserPlacesScreen`. Owns: `PullToRefreshBox` + `LazyColumn` + `when(loadState.refresh)` (Loading→skeletons, Error→`ErrorState` retry, NotLoading→empty-or-items), append footer (Loading + append-error retry via `ListAppendLoader`), `Arrangement.spacedBy` gutter, FAB bottom inset (`Spacing.fabClearance`), `animateItem()`, and an optional `groupingKey` for sticky date headers. Wraps `LazyPagingItems<T>` from `androidx.paging.compose` (confirmed import path in the existing screens).

- [ ] **Step 1: Create the file.** Write `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/PagedListScaffold.kt`:

```kotlin
package com.mobileprism.fishing.ui.components.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillParentMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.mobileprism.fishing.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T : Any> PagedListScaffold(
    items: LazyPagingItems<T>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    skeleton: @Composable LazyItemScope.() -> Unit,
    emptyState: @Composable LazyItemScope.() -> Unit,
    modifier: Modifier = Modifier,
    skeletonCount: Int = 5,
    contentPadding: PaddingValues = PaddingValues(
        start = Spacing.screenH,
        end = Spacing.screenH,
        top = Spacing.sm,
        bottom = Spacing.fabClearance,
    ),
    groupingKey: ((T) -> String)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            when (val refresh = items.loadState.refresh) {
                is LoadState.Loading -> {
                    items(skeletonCount) { skeleton() }
                }

                is LoadState.Error -> {
                    item {
                        ErrorStateGeneric(
                            modifier = Modifier.fillParentMaxSize(),
                            onRetry = { items.retry() },
                        )
                    }
                }

                is LoadState.NotLoading -> {
                    if (items.itemCount == 0) {
                        item {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center,
                            ) { emptyState() }
                        }
                    } else if (groupingKey != null) {
                        var lastKey: String? = null
                        for (index in 0 until items.itemCount) {
                            val peeked = items.peek(index)
                            val key = peeked?.let(groupingKey)
                            if (key != null && key != lastKey) {
                                stickyHeader(key = "header_$key") {
                                    com.mobileprism.fishing.ui.home.notes.ItemDate(text = key)
                                }
                                lastKey = key
                            }
                            item(key = "item_$index") {
                                items[index]?.let { value ->
                                    itemContent(value)
                                }
                            }
                        }
                    } else {
                        items(count = items.itemCount) { index ->
                            items[index]?.let { value ->
                                itemContent(value)
                            }
                        }
                    }
                }
            }

            when (items.loadState.append) {
                is LoadState.Loading -> item { ListAppendLoader() }
                is LoadState.Error -> item {
                    ListAppendLoader(isError = true, onRetry = { items.retry() })
                }
                else -> Unit
            }
        }
    }
}
```

- [ ] **Step 2: Verify the `ItemDate` reference path.** The grouping branch calls `ItemDate`. Run:
```
grep -rn "fun ItemDate" shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/notes/
```
Confirm the fully-qualified call `com.mobileprism.fishing.ui.home.notes.ItemDate` resolves. If `ItemDate` lives in a different package/file, update the qualified name accordingly. If it is `private` or `internal` and not visible cross-package, replace the `stickyHeader` body with an inline header that reuses `AppText`:
```kotlin
                                stickyHeader(key = "header_$key") {
                                    com.mobileprism.fishing.ui.home.views.AppText(
                                        text = key,
                                        style = com.mobileprism.fishing.ui.home.views.AppTextStyle.Support,
                                        modifier = Modifier.padding(vertical = Spacing.xs),
                                    )
                                }
```
and add `import androidx.compose.foundation.layout.padding`.

- [ ] **Step 3: Confirm paging + pull-to-refresh APIs.** Run:
```
grep -rn "import androidx.paging.compose.LazyPagingItems\|import androidx.paging.LoadState\|PullToRefreshBox\|\.retry()\|\.peek(" shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/notes/UserCatchesScreen.kt
```
Confirm `LazyPagingItems`, `LoadState`, `PullToRefreshBox`, `.retry()`/`.peek(index)` are used the same way in the existing screen. `PullToRefreshBox` is `androidx.compose.material3.pulltorefresh.PullToRefreshBox` — verify the import resolves; if the existing screen imports a different path, match it.

- [ ] **Step 4: Verify (compile Android).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`. Resolve any `fillParentMaxSize`/`stickyHeader` opt-in issues by confirming `@OptIn(ExperimentalFoundationApi::class)` is present (it is).

- [ ] **Step 5: Verify (iOS source set compiles).** Run:
```
./gradlew :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`. (Paging `LazyPagingItems` is already used in commonMain screens, so it is KMP-safe.)

- [ ] **Step 6: Commit.**
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/components/state/PagedListScaffold.kt
git commit -m "Add PagedListScaffold<T> generic paged-list state machine"
```

---

### Task 11: `AppButtonStyle` contract test + full module verification

**Files:**
- Create: `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/components/AppButtonStyleTest.kt`

A pure-logic guard that the button-style enum contract (consumed by screen plans 04-11) stays stable.

- [ ] **Step 1: Write the test.** Write `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/components/AppButtonStyleTest.kt`:

```kotlin
package com.mobileprism.fishing.ui.components

import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppButtonStyleTest {

    @Test
    fun appButtonStyleHasFourVariants() {
        assertEquals(4, AppButtonStyle.entries.size)
    }

    @Test
    fun appButtonStyleEntriesInDocumentedOrder() {
        assertEquals(
            listOf("Filled", "Tonal", "Outlined", "Text"),
            AppButtonStyle.entries.map { it.name },
        )
    }
}
```

- [ ] **Step 2: Run the new unit tests.** Run:
```
./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.components.AppButtonStyleTest" --tests "com.mobileprism.fishing.ui.components.AppTextStyleTest"
```
Expected: `BUILD SUCCESSFUL`, 4 tests pass total.

- [ ] **Step 3: Full module unit-test run.** Run:
```
./gradlew :shared:testDebugUnitTest
```
Expected: `BUILD SUCCESSFUL` — no pre-existing test regressed.

- [ ] **Step 4: Full module compile (Android + iOS).** Run:
```
./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL` for both, confirming the whole component library compiles for Android and the iOS source set.

- [ ] **Step 5: Install the debug app.** Run:
```
./gradlew :androidApp:installDebug
```
Expected: `BUILD SUCCESSFUL`, `Installing APK 'androidApp-debug.apk' on 'emulator-...'`. (No screen consumes these components yet — this confirms the library links into the app without breaking the existing build.)

- [ ] **Step 6: Commit.**
```
git add shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/components/AppButtonStyleTest.kt
git commit -m "Add AppButtonStyle contract test; verify component library builds + installs"
```

---

## Verification & success criteria

- **Builds:** `./gradlew :shared:compileDebugKotlinAndroid` and `./gradlew :androidApp:installDebug` both succeed (component library links into the app without consuming screens yet).
- **iOS source set still compiles:** `./gradlew :shared:compileKotlinIosSimulatorArm64` succeeds — proving every new `commonMain` file is KMP/iOS-safe (no Android-only API leaked in).
- **Unit tests pass:** `./gradlew :shared:testDebugUnitTest` is green, including the two new contract tests (`AppTextStyleTest`, `AppButtonStyleTest`) and all pre-existing tests.
- **APIs match the spec §5 contract** exactly: `AppButton(text, onClick, modifier, style = Filled, enabled = true, loading = false, leadingIcon = null)` + `AppButtonStyle { Filled, Tonal, Outlined, Text }`; `AppIconButton(onClick, icon, contentDescription, modifier, ...)` with required non-null `contentDescription` and ≥48dp; `AppText(text, style = Body, color = LocalContentColor, ...)` + `AppTextStyle` (Display/Heading/Title/Subtitle/Body/BodySmall/Caption/Support) + `TextWithLeadingIcon`; `AppCard(modifier, onClick = null, shape = shapes.medium, elevation = Elevation.card, contentPadding = Spacing.cardPadding, content)` + `SectionCard(icon, title, modifier, onClick = null, trailing = null, content)`; `FormTextField(...)` + `PickerField(value, label, leadingIcon, placeholder, onClick)`; `EmptyState`/`ErrorState`/`LoadingState`/`InlineLoader`/`ListAppendLoader` + presets; `ScreenStateContent<T>(state, ...)`; `PagedListScaffold<T>(items, isRefreshing, onRefresh, skeleton, emptyState, ...)`; `SkeletonBox(width, height, shape)`/`SkeletonLine` + `CardSkeleton`/`ListItemSkeleton`.
- **Legacy preserved:** `Buttons.kt`, `Text.kt`, `Cards.kt`, `TextFields.kt`, `DefaultViews.kt` are untouched — these new components live alongside them; deletion is gated by Plan 11 on a grep proving zero references.
- **Tokens consumed, not redefined:** all spacing/elevation/motion/shape/color come from Plan 01 tokens (`Spacing`, `Elevation`, `Motion`, `MaterialTheme.shapes`, M3 color roles); no hardcoded design literals introduced (the only raw `dp` values are intrinsic component sizes — icon 24dp, spinner 18dp, skeleton thumbnail 48dp — not design tokens).
- **Screenshot checkpoint (user-run):** Because no screen consumes these components in this plan, there is no end-user-visible change to screenshot yet. The user confirms the app installs and launches on both emulators (`emulator-5554`, `emulator-5556`, id `com.merkost.fishingnotes.debug`) without regression. The first visible adoption is verified in Plan 04+ (screen rollouts), where each screen plan captures light+dark screenshots of the affected screen for review.
