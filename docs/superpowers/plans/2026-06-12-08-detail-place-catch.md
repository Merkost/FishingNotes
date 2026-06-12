---
# Detail Screens (Place & Catch) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refresh the Place-detail and Catch-detail screens onto the shared design system — section cards, deduped edit sheets, a tabbed pager + detail header, read-only value rows, metric items, a unified add affordance, real loading/empty states, a localized overflow menu — and fix the entangled correctness/i18n bugs (dead `%s`/`%1$s` replace hacks, generic content descriptions, the 1dp Spacer hack, commented-out dead code, silently-discarded Card params).

**Architecture:** These two screens currently hand-roll every section (card + 12dp padding + `SubtitleWithIcon` header), duplicate the four catch edit sheets, render blank until the marker `StateFlow` emits, and present three divergent "add" affordances. This plan introduces five small screen-detail composables that THIS plan owns (`DetailHeader`, `LabeledValueRow`, `MetricItem`, `AddItemButton`, `AppBarOverflowMenu` in `ui/home/views/`) and CONSUMES the shared Layer-A/B components owned by Plans 01–03 (`Spacing`, `Elevation`, `SectionCard`, `EditBottomSheetScaffold`, `TabbedPager`, `EmptyState`, `ScreenStateContent`, `AppCard`, `AppButton`) at their call sites — never re-implementing them. Each task compiles independently; legacy composables on these screens are removed as their last caller migrates (final cross-app deletions are gated by Plan 11).

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3, Koin, Compose Resources. All shared UI in commonMain (KMP/iOS-safe).

**Sprint:** S3 Create & Detail · **Plan 08 of 11**

**Depends on:** Plan 01 (tokens: `Spacing`, `Elevation`, `Emphasis`, shapes/typography), Plan 02 (`AppCard`, `AppButton`, `EmptyState`, `ScreenStateContent`, `SectionCard`, skeletons), Plan 03 (`AppTopBar`, `TabbedPager`/`AppTabRow`, `EditBottomSheetScaffold`, `BottomActionBar`). Plan 11 (final sweep) runs LAST and gates deletion of any now-orphan legacy composable.

---
---

## File Structure

| File | Action | Responsibility |
|---|---|---|
| `shared/src/commonMain/composeResources/values/strings.xml` | Modify | Add `more_options` + `no_notes_added` localized strings (overflow a11y + Notes-tab empty state) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppBarOverflowMenu.kt` | Create | Reusable `MoreVert` + `DropdownMenu` overflow with a **required localized** `contentDescription` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AddItemButton.kt` | Create | One labelled, ≥48dp, accessible "add" affordance (unifies the 3 divergent add controls) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/LabeledValueRow.kt` | Create | Read-only label-over-value display row (replaces `readOnly` `SimpleUnderlineTextField` misuse) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/MetricItem.kt` | Create | Unified `icon? + label + value` metric presentation (weight/amount/pressure/wind/temp/moon) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/DetailHeader.kt` | Create | Detail-screen header rhythm (leading icon + title + subtitle + metric + trailing action) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/catch/Catch.kt` | Modify | Migrate sections to `SectionCard`/`MetricItem`/`LabeledValueRow`; surface `AppTopBar` + `AppBarOverflowMenu`; parameterized delete string |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/catch/CatchScreenDialogs.kt` | Modify | Dedupe the 4 edit sheets through `EditBottomSheetScaffold`; drop the 1dp Spacer hack |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/place/Place.kt` | Modify | Wrap content in `ScreenStateContent`; surface `AppTopBar`; remove RectangleShape banner sheet hack note |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/place/PlaceViews.kt` | Modify | `DetailHeader` + `TabbedPager` + `EmptyState` + `AddItemButton`; remove commented dead code; parameterized delete string; localized overflow |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/detail/DeleteDialogStringTest.kt` | Create (Test) | Asserts the parameterized delete-confirmation strings contain exactly one `%1$s` placeholder and no `%s` token |

> **Component ownership note.** `Spacing`, `Elevation`, `Emphasis`, `AppCard`, `AppButton`, `EmptyState`, `ScreenStateContent`, `SectionCard`, `EditBottomSheetScaffold`, `TabbedPager`/`AppTabRow`, `AppTopBar`, and skeletons are defined by Plans 01–03; this plan calls them by the exact names in spec §4/§5 and never redefines their internals. `DetailHeader`, `LabeledValueRow`, `MetricItem`, `AddItemButton`, and `AppBarOverflowMenu` are screen-detail components owned by THIS plan (they appear only in the Place/Catch audit, not spec §5).

---

## Tasks

### Task 1: Add localized strings for overflow a11y and Notes-tab empty state

**Files:**
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`

Context: The overflow icon currently uses `Icons.Outlined.MoreVert.name` (literally reads "MoreVert" to screen readers) in both `Catch.kt:158` and `PlaceViews.kt:411`. The Notes tab has no real empty state. The delete strings already use the canonical `%1$s` placeholder (`delete_catch_dialog = Removing the catch "%1$s"`, `delete_place_dialog = Delete place "%1$s"`), so no string edit is needed for the placeholder fix — only the dead `.replace(...)` calls in code get removed (Tasks 7 and 10).

- [ ] **Step 1: Add the two new strings.** In `strings.xml`, immediately after the existing `<string name="toggle_visibility">Toggle visibility</string>` line (currently line 451), add:
```xml
    <string name="more_options">More options</string>
    <string name="no_notes_added">No notes added</string>
```
- [ ] **Step 2: Verify the keys generate.** Run `./gradlew :shared:compileDebugKotlinAndroid` — expected: BUILD SUCCESSFUL and the generated `Res.string.more_options` / `Res.string.no_notes_added` accessors are available (Compose Resources regenerates `Res` on compile).
- [ ] **Step 3: Commit.**
```bash
git add shared/src/commonMain/composeResources/values/strings.xml
git commit -m "Add more_options and no_notes_added strings for detail screens

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 2: Create `AppBarOverflowMenu`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppBarOverflowMenu.kt`

Context: Both detail top bars duplicate the `IconButton { Icon(MoreVert, MoreVert.name) } + DropdownMenu { DropdownMenuItem(delete) }` shape with a non-localized content description. This extracts one accessible, reusable overflow control. `OverflowMenuItem` models each action so callers can add destructive actions with error tint.

- [ ] **Step 1: Write the file.**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.more_options
import org.jetbrains.compose.resources.stringResource

data class OverflowMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val leadingIcon: ImageVector? = null,
    val tint: Color = Color.Unspecified,
)

@Composable
fun AppBarOverflowMenu(
    items: List<OverflowMenuItem>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        modifier = modifier,
        onClick = { expanded = true }
    ) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(Res.string.more_options)
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        items.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        color = if (item.tint == Color.Unspecified) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            item.tint
                        }
                    )
                },
                leadingIcon = item.leadingIcon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (item.tint == Color.Unspecified) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                item.tint
                            }
                        )
                    }
                },
                onClick = {
                    expanded = false
                    item.onClick()
                }
            )
        }
    }
}
```
- [ ] **Step 2: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL.
- [ ] **Step 3: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppBarOverflowMenu.kt
git commit -m "Add AppBarOverflowMenu with localized content description

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 3: Create `AddItemButton`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AddItemButton.kt`

Context: The Place screen has three divergent add affordances — a bare unlabeled `Card { Icon(Add) }` (Notes tab), a labelled `DefaultButtonOutlined` (catches empty state), and another `DefaultButtonOutlined` in the action row. This is one labelled, ≥48dp, accessible add control built on the shared `AppButton(style = Tonal)` (owned by Plan 02). It delegates accessibility and touch-target enforcement to `AppButton`.

- [ ] **Step 1: Write the file.**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun AddItemButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = AppButtonStyle.Tonal,
        enabled = enabled,
        leadingIcon = rememberVectorPainter(Icons.Default.Add)
    )
}
```

> **Contract dependency.** `AppButton(text, onClick, modifier, style = Filled, enabled = true, loading = false, leadingIcon = null)` and `AppButtonStyle { Filled, Tonal, Outlined, Text }` are defined by Plan 02 (spec §5.1) with `leadingIcon: Painter?`. If Plan 02 is not yet merged in your worktree, **stop and merge Plan 02 first** — do not re-implement `AppButton` here.

- [ ] **Step 2: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL. If it fails because `AppButton`/`AppButtonStyle` are unresolved, Plan 02 is not merged; merge it and retry.
- [ ] **Step 3: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AddItemButton.kt
git commit -m "Add AddItemButton unifying the divergent add affordances

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 4: Create `LabeledValueRow`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/LabeledValueRow.kt`

Context: Display-only values (rod/bait/lure/note) are currently rendered with `SimpleUnderlineTextField` — a `readOnly` `TextField` with a fake press interaction and a tinted container, exposing a text-cursor affordance for non-editable content. This is a purpose-built read-only row: label on top, value below, optional trailing edit icon, optional click. It uses `Spacing` and `Emphasis` tokens from Plan 01.

- [ ] **Step 1: Write the file.**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.mobileprism.fishing.ui.theme.Emphasis
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun LabeledValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    editContentDescription: String? = null,
) {
    val rowModifier = if (onClick != null) {
        modifier.fillMaxWidth().clickable { onClick() }
    } else {
        modifier.fillMaxWidth()
    }

    Row(
        modifier = rowModifier.padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onClick != null) {
            Icon(
                modifier = Modifier
                    .padding(start = Spacing.sm)
                    .size(20.dp),
                imageVector = Icons.Outlined.Edit,
                contentDescription = editContentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Emphasis.hint)
            )
        }
    }
}
```

> **Token dependency.** `Spacing.xxs/sm` and `Emphasis.hint` are defined by Plan 01 (spec §4.1, §4.5). The `20.dp` is a non-tokenized icon size local to this row (spec keeps icon sizes inline). Add `import androidx.compose.ui.unit.dp` to the imports.

- [ ] **Step 2: Add the missing dp import.** Add `import androidx.compose.ui.unit.dp` to the import block (between the `Spacing`/`Emphasis` imports and the rest, alphabetical placement is fine since this is a fresh file).
- [ ] **Step 3: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL. If `Spacing`/`Emphasis` are unresolved, merge Plan 01 and retry.
- [ ] **Step 4: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/LabeledValueRow.kt
git commit -m "Add LabeledValueRow for read-only display values

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 5: Create `MetricItem`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/MetricItem.kt`

Context: The Catch screen presents key/value metrics three different ways (inline `amount: N pc` text, header+weight row, centered icon-label-value columns for pressure/wind). This unifies them into one `icon? + label + value` item. Two layouts are needed: a compact horizontal one for the title row (weight/amount) and a centered vertical one for the weather grid (pressure/wind). A single composable with a `vertical: Boolean` switch covers both without duplication. Uses `Spacing` tokens (Plan 01).

- [ ] **Step 1: Write the file.**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    vertical: Boolean = false,
) {
    if (vertical) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                if (icon != null) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = icon,
                        contentDescription = null,
                        tint = iconTint
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = icon,
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
```
- [ ] **Step 2: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL.
- [ ] **Step 3: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/MetricItem.kt
git commit -m "Add MetricItem unifying metric presentations

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 6: Create `DetailHeader`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/DetailHeader.kt`

Context: `PlaceTitleView` (PlaceViews.kt:42-100) composes a colored leading icon + title + date subtitle + catch counter + trailing navigate action in a fixed layout. This is a reusable detail-header shape. The extracted `DetailHeader` takes the leading-icon painter + tint, title, subtitle, a `metric` slot (for the existing `ItemCounter`), and a `trailingAction` slot. It fixes the touch-target bug from the audit: the trailing action's end padding is applied OUTSIDE the sized button (the old code put `.padding(end=16.dp)` inside the 48dp box, shrinking the tap area to ~32dp). Uses `Spacing` tokens (Plan 01).

- [ ] **Step 1: Write the file.**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun DetailHeader(
    leadingIcon: Painter,
    leadingIconTint: Color,
    title: String,
    modifier: Modifier = Modifier,
    leadingIconContentDescription: String? = null,
    subtitle: (@Composable () -> Unit)? = null,
    metric: (@Composable RowScope.() -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = leadingIcon,
            contentDescription = leadingIconContentDescription,
            tint = leadingIconTint
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null || metric != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    subtitle?.invoke()
                    metric?.invoke(this)
                }
            }
        }

        trailingAction?.invoke()
    }
}
```
- [ ] **Step 2: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL. If `Spacing.screenH` is unresolved, confirm Plan 01 added the `screenH` semantic alias (spec §4.1) and is merged.
- [ ] **Step 3: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/DetailHeader.kt
git commit -m "Add DetailHeader for detail-screen header rhythm

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 7: Migrate Catch detail sections to `SectionCard` / `MetricItem` / `LabeledValueRow`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/catch/Catch.kt:142-488`

Context: Four sections each hand-roll `DefaultCard`/`DefaultCardClickable` + `Column(padding(12.dp))` + `SubtitleWithIcon` header. Migrate them to `SectionCard(icon, title, modifier, onClick, trailing, content)` (Plan 02, spec §5.1) which owns the card shell, padding, header, and the click/edit affordance. Replace the inline weight/amount layout and the weather pressure/wind columns with `MetricItem`; replace the way-of-fishing `SimpleUnderlineTextField` display values with `LabeledValueRow`. Fix the overflow content description via `AppBarOverflowMenu`. Fix the parameterized delete string. Add a missing-weather empty fallback.

- [ ] **Step 1: Replace `CatchTopBar` (Catch.kt:142-167)** with a surface-colored `AppTopBar` + `AppBarOverflowMenu`. New body:
```kotlin
@Composable
fun CatchTopBar(navController: NavController, catch: UserCatch, onDeleteCatch: () -> Unit) {
    val userPreferences: UserPreferences = koinInject()
    val is12hTime by userPreferences.use12hTimeFormat.collectAsState(initial = false)

    AppTopBar(
        title = stringResource(Res.string.user_catch),
        subtitle = catch.date.toDateTextMonth() + " " + catch.date.toTime(is12hTime),
        navigationIcon = {
            AppIconButton(
                onClick = { navController.popBackStack() },
                icon = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack),
                contentDescription = stringResource(Res.string.back)
            )
        },
        actions = {
            AppBarOverflowMenu(
                items = listOf(
                    OverflowMenuItem(
                        label = stringResource(Res.string.delete),
                        onClick = onDeleteCatch,
                        leadingIcon = Icons.Outlined.Delete,
                        tint = MaterialTheme.colorScheme.error
                    )
                )
            )
        }
    )
}
```

> **Contract dependency.** `AppTopBar(title, subtitle = null, navigationIcon = null, actions = {}, scrollBehavior = null)` and `AppIconButton(onClick, icon, contentDescription, modifier, ...)` are owned by Plan 03/02 (spec §5.3/§5.1). `AppTopBar` is **surface-colored by default** per the locked decision.

- [ ] **Step 2: Fix the delete-confirmation string (Catch.kt:176).** In `DeleteCatchDialog`, replace the `.replace(...)` hack with the canonical parameterized resource. Change:
```kotlin
        primaryText = stringResource(Res.string.delete_catch_dialog).replace("%s", catch.fishType).replace("%1\$s", catch.fishType),
```
to:
```kotlin
        primaryText = stringResource(Res.string.delete_catch_dialog, catch.fishType),
```
(`delete_catch_dialog` already is `Removing the catch "%1$s"`, so the positional arg fills the single canonical placeholder.)

- [ ] **Step 3: Reduce the content column rhythm (Catch.kt:204-211).** In `CatchContent`, change the inter-card spacing from the backwards `6.dp` to the token `Spacing.sectionGap` and the screen padding to tokens. Replace:
```kotlin
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
```
with:
```kotlin
            .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
```

- [ ] **Step 4: Migrate `CatchTitleView` (Catch.kt:255-291)** to `SectionCard` + `MetricItem`. Replace the whole function body with:
```kotlin
@Composable
fun CatchTitleView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.ic_fish),
        title = catch.fishType,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            MetricItem(
                label = stringResource(Res.string.kg),
                value = catch.fishWeight.toString()
            )
            MetricItem(
                label = stringResource(Res.string.amount),
                value = "${catch.fishAmount} ${stringResource(Res.string.pc)}"
            )
        }
    }
}
```

> **Contract dependency.** `SectionCard(icon, title, modifier, onClick = null, trailing = null, content)` is owned by Plan 02 (spec §5.1): the card shell, internal padding, icon-title header, and the onClick-driven edit affordance live in `SectionCard`. The `painterResource(Res.drawable.ic_fish)` keeps the fish glyph as the header icon. `ic_fish` exists (it is `TabItem.Catches`' icon).

- [ ] **Step 5: Migrate `WayOfFishingView` (Catch.kt:308-364)** to `SectionCard` + `LabeledValueRow`. Replace the whole function with:
```kotlin
@Composable
fun WayOfFishingView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.ic_fishing_rod),
        title = stringResource(Res.string.way_of_fishing),
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            LabeledValueRow(
                label = stringResource(Res.string.fish_rod),
                value = catch.fishingRodType.ifBlank { stringResource(Res.string.no_rod) },
                onClick = onClick,
                editContentDescription = stringResource(Res.string.edit)
            )
            LabeledValueRow(
                label = stringResource(Res.string.bait),
                value = catch.fishingBait.ifBlank { stringResource(Res.string.no_bait) },
                onClick = onClick,
                editContentDescription = stringResource(Res.string.edit)
            )
            LabeledValueRow(
                label = stringResource(Res.string.lure),
                value = catch.fishingLure.ifBlank { stringResource(Res.string.no_lure) },
                onClick = onClick,
                editContentDescription = stringResource(Res.string.edit)
            )
        }
    }
}
```

- [ ] **Step 6: Migrate `CatchWeatherView` (Catch.kt:366-488)** to `SectionCard` + `MetricItem`, with a missing-data fallback. The weather card is non-editable (no `onClick`). Replace the whole function with:
```kotlin
@Composable
fun CatchWeatherView(
    modifier: Modifier = Modifier,
    catch: UserCatch
) {
    val weatherPrefs: WeatherPreferences = koinInject()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val windSpeedUnit by weatherPrefs.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.weather_sunny),
        title = stringResource(Res.string.weather)
    ) {
        if (catch.weatherPrimary.isBlank() && catch.weatherIcon.isBlank()) {
            NoContentView(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.no_description),
                icon = painterResource(Res.drawable.weather_sunny)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(getWeatherIconByName(catch.weatherIcon)),
                        contentDescription = stringResource(Res.string.weather)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column {
                        MetricItem(
                            label = stringResource(Res.string.weather),
                            value = temperatureUnit.getTemperature(catch.weatherTemperature)
                                    + stringResource(temperatureUnit.stringRes)
                        )
                        SecondaryTextSmall(
                            text = catch.weatherPrimary.replaceFirstChar { it.uppercase() }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    MetricItem(
                        label = stringResource(Res.string.moon_phase),
                        value = (catch.weatherMoonPhase * 100).toInt().toString()
                                + " " + stringResource(Res.string.percent),
                        icon = painterResource(getMoonIconByPhase(catch.weatherMoonPhase)),
                        vertical = true
                    )
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricItem(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.pressure),
                        value = pressureUnit.getPressureFromMmhg(catch.weatherPressure)
                                + " " + pressureUnit.name,
                        icon = painterResource(Res.drawable.ic_gauge),
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        vertical = true
                    )
                    MetricItem(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.wind),
                        value = windSpeedUnit.getWindSpeed(catch.weatherWindSpeed.toDouble())
                                + " " + windSpeedUnit.name,
                        icon = painterResource(Res.drawable.ic_wind),
                        vertical = true
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 7: Update imports in `Catch.kt`.** Add the new imports and remove ones that only served deleted code:
  - Add: `import com.mobileprism.fishing.ui.home.views.AppTopBar`, `import com.mobileprism.fishing.ui.home.views.AppIconButton`, `import com.mobileprism.fishing.ui.home.views.AppBarOverflowMenu`, `import com.mobileprism.fishing.ui.home.views.OverflowMenuItem`, `import com.mobileprism.fishing.ui.home.views.SectionCard`, `import com.mobileprism.fishing.ui.home.views.MetricItem`, `import com.mobileprism.fishing.ui.home.views.LabeledValueRow`, `import com.mobileprism.fishing.ui.theme.Spacing`, `import androidx.compose.material.icons.automirrored.filled.ArrowBack`, `import androidx.compose.material.icons.outlined.Delete`, `import androidx.compose.ui.graphics.vector.rememberVectorPainter`, `import fishing.shared.generated.resources.back`, `import fishing.shared.generated.resources.edit`.
  - Remove the now-unused `import androidx.compose.material.icons.outlined.MoreVert` (overflow is internal to `AppBarOverflowMenu`) and `import androidx.compose.ui.draw.rotate` (the rotated wind icon was removed when wind moved to `MetricItem`).
  - Note `import fishing.shared.generated.resources.*` is already present (line 22) and covers `ic_fish`, `ic_fishing_rod`, `weather_sunny`, `ic_gauge`, `ic_wind`, `no_description`, `kg`, `amount`, `pc`, etc.; the wildcard already imports `DefaultAppBar` consumers — leave it.

> The wind-direction `rotate` arrow is dropped from the value (it was a decorative duplicate of the `ic_wind` icon). If the wind direction arrow must be retained, leave it out of this plan and raise it as a follow-up — the audit only required unifying the metric presentation, not preserving the rotated glyph.

- [ ] **Step 8: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL. Resolve any unresolved-reference from a missing Plan 01–03 component by merging the owning plan (do NOT re-implement it here).
- [ ] **Step 9: Verify (iOS source set).** `./gradlew :shared:compileKotlinIosSimulatorArm64` — expected BUILD SUCCESSFUL (no Android-only API entered `commonMain`).
- [ ] **Step 10: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/catch/Catch.kt
git commit -m "Migrate Catch detail sections to SectionCard/MetricItem/LabeledValueRow

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 8: Dedupe the 4 catch edit sheets via `EditBottomSheetScaffold`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/catch/CatchScreenDialogs.kt:110-467`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/catch/Catch.kt:99-117`

Context: All four sheets repeat `Column(padding(horizontal=20, vertical=16)) + spacedBy(12) + PrimaryText title + content + Row(Cancel + 8dp spacer + Save)`. Route each through `EditBottomSheetScaffold(title, onCancel, onSave, saveEnabled, content)` (Plan 03, spec §5.3) which owns the padding, title, and standardized Cancel/Save action row. Also remove the `Spacer(1.dp)` hack at the top of the modal sheet in `Catch.kt`.

- [ ] **Step 1: Remove the 1dp Spacer hack (Catch.kt:99-117).** In `CatchInfoScreen`, delete the line `Spacer(modifier = Modifier.height(1.dp))` (currently line 105) inside the `ModalBottomSheet` block. `EditBottomSheetScaffold` owns the top padding/drag-handle treatment, so the workaround is no longer needed.

- [ ] **Step 2: Migrate `FishTypeAmountAndWeightDialog` (CatchScreenDialogs.kt:110-171).** Replace its whole body with:
```kotlin
@Composable
fun FishTypeAmountAndWeightDialog(
    viewModel: UserCatchViewModel,
    onCloseBottomSheet: () -> Unit
) {
    val fishType = remember { mutableStateOf("") }
    val fishAmount = remember { mutableStateOf("") }
    val fishWeight = remember { mutableStateOf("") }

    LaunchedEffect(key1 = viewModel.catch.collectAsState().value) {
        viewModel.catch.value.let {
            fishType.value = it.fishType
            fishAmount.value = it.fishAmount.toString()
            fishWeight.value = it.fishWeight.toString()
        }
    }

    EditBottomSheetScaffold(
        title = stringResource(Res.string.user_catch),
        onCancel = onCloseBottomSheet,
        onSave = {
            viewModel.updateCatchInfo(
                fishType = fishType.value,
                fishAmount = fishAmount.value.toInt(),
                fishWeight = fishWeight.value.toDouble()
            )
            onCloseBottomSheet()
        }
    ) {
        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = fishType,
            label = stringResource(Res.string.fish_species)
        )

        FishAmountAndWeightView(
            modifier = Modifier.fillMaxWidth(),
            amountState = fishAmount,
            weightState = fishWeight
        )
    }
}
```

> **Contract dependency.** `EditBottomSheetScaffold(title, onCancel, onSave, saveEnabled, content)` is owned by Plan 03 (spec §5.3). It supplies the title `PrimaryText`, the `spacedBy(12.dp)` content column, and the Cancel/Save action row (Cancel = `AppButton` Text, Save = `AppButton` Filled). `saveEnabled` defaults to `true`. The content lambda is a `ColumnScope.() -> Unit` matching the spec.

- [ ] **Step 3: Migrate `EditWayOfFishingDialog` (CatchScreenDialogs.kt:173-241).** Replace its whole body with:
```kotlin
@Composable
fun EditWayOfFishingDialog(
    viewModel: UserCatchViewModel,
    onCloseBottomSheet: () -> Unit
) {
    val rod = remember { mutableStateOf("") }
    val bait = remember { mutableStateOf("") }
    val lure = remember { mutableStateOf("") }

    LaunchedEffect(key1 = viewModel.catch.collectAsState().value) {
        viewModel.catch.value.let {
            rod.value = it.fishingRodType
            bait.value = it.fishingBait
            lure.value = it.fishingLure
        }
    }

    EditBottomSheetScaffold(
        title = stringResource(Res.string.way_of_fishing),
        onCancel = onCloseBottomSheet,
        onSave = {
            viewModel.updateWayOfFishing(
                fishingRodType = rod.value,
                fishingLure = lure.value,
                fishingBait = bait.value
            )
            onCloseBottomSheet()
        }
    ) {
        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = rod,
            label = stringResource(Res.string.fish_rod),
            singleLine = false
        )
        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = bait,
            label = stringResource(Res.string.bait),
            singleLine = false
        )
        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = lure,
            label = stringResource(Res.string.lure),
            singleLine = false
        )
    }
}
```

- [ ] **Step 4: Migrate `EditNoteDialog` (CatchScreenDialogs.kt:243-355).** This sheet has a delete affordance and a `saveEnabled` condition, so keep its delete `IconButton` inside the content and pass `saveEnabled`. Replace its whole body with:
```kotlin
@ExperimentalComposeUiApi
@Composable
fun EditNoteDialog(
    note: Note,
    onSaveNote: (Note) -> Unit,
    deleteOption: Boolean = false,
    onDeleteNote: (Note) -> Unit = {},
    onCloseDialog: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val onClose = {
        keyboardController?.hide()
        onCloseDialog()
    }

    val noteId = remember { mutableStateOf(note.id) }
    val noteTitle = remember { mutableStateOf(note.title) }
    val noteDescriptionState = remember { mutableStateOf(note.description) }
    val noteDateCreated = remember { mutableStateOf(note.dateCreated) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(note) {
        noteId.value = note.id
        noteTitle.value = note.title
        noteDescriptionState.value = note.description
        noteDateCreated.value = note.dateCreated
    }

    if (showDeleteConfirm) {
        DefaultDialog(
            primaryText = stringResource(Res.string.delete_note_dialog),
            secondaryText = stringResource(Res.string.sure_delete_note_dialog),
            onNegativeClick = { showDeleteConfirm = false },
            onPositiveClick = {
                onDeleteNote(
                    Note(
                        noteId.value,
                        noteTitle.value,
                        noteDescriptionState.value,
                        noteDateCreated.value
                    )
                )
                onClose()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    val isTitleValid = ValidationUtils.isValidNoteTitle(noteTitle.value)

    EditBottomSheetScaffold(
        title = if (noteId.value.isEmpty()) stringResource(Res.string.new_note)
        else stringResource(Res.string.edit_note),
        onCancel = onClose,
        onSave = {
            onSaveNote(
                Note(
                    id = noteId.value,
                    title = noteTitle.value.trim(),
                    description = noteDescriptionState.value,
                    dateCreated = Clock.System.now().toEpochMilliseconds()
                )
            )
            onClose()
        },
        saveEnabled = noteDescriptionState.value.isNotBlank() && isTitleValid,
        leadingAction = if (deleteOption) {
            {
                AppIconButton(
                    onClick = { showDeleteConfirm = true },
                    icon = rememberVectorPainter(Icons.Default.Delete),
                    contentDescription = stringResource(Res.string.delete)
                )
            }
        } else null
    ) {
        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = noteTitle,
            label = stringResource(Res.string.title),
            singleLine = true,
            isError = !isTitleValid
        )
        SimpleOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textState = noteDescriptionState,
            label = stringResource(Res.string.note),
            singleLine = false,
            isError = noteDescriptionState.value.isBlank()
        )
    }
}
```

> **Contract addendum.** `EditBottomSheetScaffold` exposes an optional `leadingAction: (@Composable () -> Unit)? = null` slot (rendered start-aligned in the action row, pushing Cancel/Save to the end). This is part of the Plan 03 contract for sheets that carry a destructive/secondary action. If Plan 03's signature lacks `leadingAction`, add it there (it is owned by Plan 03) — do not branch the scaffold in this plan.

- [ ] **Step 5: Migrate `AddPhotoDialog` (CatchScreenDialogs.kt:357-467).** This sheet has a custom header (title + `MaxCounterView`) and gallery/camera buttons in the action area, so it uses `EditBottomSheetScaffold` with the counter in a `headerTrailing` slot and the gallery/camera buttons in the `leadingAction` slot. Replace its whole body with:
```kotlin
@Composable
fun AddPhotoDialog(
    photos: List<String>,
    mediaPicker: MediaPickerLauncher,
    onPickedPhotosHandlerChange: ((List<String>) -> Unit) -> Unit,
    onSavePhotosClick: (List<String>) -> Unit,
    onCloseBottomSheet: () -> Unit
) {
    val tempDialogPhotosState = remember { mutableStateListOf<String>() }

    DisposableEffect(Unit) {
        onPickedPhotosHandlerChange { newPhotos ->
            if ((newPhotos.size + tempDialogPhotosState.size) > MAX_PHOTOS) {
                SnackbarManager.showMessage(Res.string.max_photos_allowed)
            } else {
                tempDialogPhotosState.addAll(newPhotos)
            }
        }
        onDispose {
            onPickedPhotosHandlerChange { _: List<String> -> }
        }
    }

    LaunchedEffect(key1 = photos) {
        tempDialogPhotosState.clear()
        tempDialogPhotosState.addAll(photos)
    }

    EditBottomSheetScaffold(
        title = stringResource(Res.string.photos),
        onCancel = onCloseBottomSheet,
        onSave = {
            if (tempDialogPhotosState.size > MAX_PHOTOS) {
                SnackbarManager.showMessage(Res.string.max_photos_allowed)
            } else {
                onSavePhotosClick(tempDialogPhotosState.toList())
                onCloseBottomSheet()
            }
        },
        headerTrailing = {
            MaxCounterView(
                count = tempDialogPhotosState.size,
                maxCount = MAX_PHOTOS,
                icon = painterResource(Res.drawable.ic_baseline_photo_24)
            )
        },
        leadingAction = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconButton(
                    onClick = { mediaPicker.launchGallery() },
                    icon = painterResource(Res.drawable.ic_baseline_add_photo_alternate_24),
                    contentDescription = stringResource(Res.string.gallery)
                )
                AppIconButton(
                    onClick = { mediaPicker.launchCamera() },
                    icon = painterResource(Res.drawable.ic_baseline_photo_camera_24),
                    contentDescription = stringResource(Res.string.camera)
                )
            }
        }
    ) {
        LazyRow(
            modifier = Modifier
                .defaultMinSize(minHeight = 120.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = Spacing.xs, horizontal = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (tempDialogPhotosState.isNotEmpty()) {
                items(items = tempDialogPhotosState) {
                    ItemPhoto(
                        photo = it,
                        clickedPhoto = { },
                        deletedPhoto = { tempDialogPhotosState.remove(it) }
                    )
                }
            } else {
                item {
                    NoContentView(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.no_photos_added),
                        icon = painterResource(Res.drawable.ic_no_photos)
                    )
                }
            }
        }
    }
}
```

> **Contract addendum.** `EditBottomSheetScaffold` exposes an optional `headerTrailing: (@Composable () -> Unit)? = null` slot rendered to the right of the title (for counters/badges). Owned by Plan 03; add it there if missing. The gallery/camera affordances become `AppIconButton`s in `leadingAction` (≥48dp, required content descriptions) — they were previously full-width outlined buttons in the action row.

- [ ] **Step 6: Update imports in `CatchScreenDialogs.kt`.** Add: `import com.mobileprism.fishing.ui.home.views.EditBottomSheetScaffold`, `import com.mobileprism.fishing.ui.home.views.AppIconButton`, `import com.mobileprism.fishing.ui.theme.Spacing`, `import androidx.compose.ui.graphics.vector.rememberVectorPainter`. Remove the now-unused imports for the deleted action rows: `import com.mobileprism.fishing.ui.home.views.DefaultButton`, `import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled`, `import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined`, `import com.mobileprism.fishing.ui.home.views.PrimaryText`, `import androidx.compose.foundation.layout.Spacer`, `import androidx.compose.foundation.layout.width`, `import androidx.compose.foundation.layout.wrapContentHeight`, `import androidx.compose.material3.IconButton`. Keep `Icon`, `Icons`, `Icons.Default.Delete`, `Row`, `Arrangement`, `Alignment`, `PaddingValues`, `defaultMinSize`, `fillMaxWidth`, `padding` (still used in remaining code).

- [ ] **Step 7: Remove the now-unused `Spacer`/`height` imports from `Catch.kt` if orphaned.** After Step 1 removed `Spacer(Modifier.height(1.dp))`, check whether `Spacer`/`height` are still referenced elsewhere in `Catch.kt` (they are — `CatchWeatherView` uses `Spacer` and `width`, and `Spacer(Modifier.weight(1f))`). Leave the wildcard `import androidx.compose.foundation.layout.*` (Catch.kt:5) untouched.

- [ ] **Step 8: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL.
- [ ] **Step 9: Verify (iOS source set).** `./gradlew :shared:compileKotlinIosSimulatorArm64` — expected BUILD SUCCESSFUL.
- [ ] **Step 10: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/catch/CatchScreenDialogs.kt shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/catch/Catch.kt
git commit -m "Dedupe catch edit sheets via EditBottomSheetScaffold; drop 1dp Spacer hack

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 9: Migrate `DefaultNoteView` to `SectionCard` + `LabeledValueRow`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/DefaultViews.kt:31-81`

Context: `DefaultNoteView` is the fourth catch-detail section and is also reused on the Place screen. It currently hand-rolls `DefaultCardClickable` + `SubtitleWithIcon` + a `readOnly` `SimpleUnderlineTextField`. Migrate to `SectionCard` + `LabeledValueRow` to match the other three catch sections and remove the TextField misuse. Keep the empty `NoContentView` fallback.

- [ ] **Step 1: Replace `DefaultNoteView` (DefaultViews.kt:31-81)** with:
```kotlin
@Composable
fun DefaultNoteView(
    modifier: Modifier = Modifier,
    note: Note,
    onClick: () -> Unit,
) {
    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.ic_baseline_sticky_note_2_24),
        title = stringResource(Res.string.note),
        onClick = onClick
    ) {
        if (note.description.isEmpty()) {
            NoContentView(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.no_description),
                icon = painterResource(Res.drawable.ic_no_note)
            )
        } else {
            LabeledValueRow(
                label = note.dateCreated.toDate(),
                value = note.description,
                onClick = onClick,
                editContentDescription = stringResource(Res.string.edit)
            )
        }
    }
}
```
- [ ] **Step 2: Update imports in `DefaultViews.kt`.** Add: `import com.mobileprism.fishing.ui.home.views.SectionCard` is **not needed** (same package) — instead add `import fishing.shared.generated.resources.edit` and `import fishing.shared.generated.resources.note` (the wildcard `import fishing.shared.generated.resources.*` at line 26 already covers these; confirm and add only if the wildcard is later narrowed). `SectionCard`, `LabeledValueRow`, `NoContentView` are all in the same `ui.home.views` package — no import needed. Remove `import androidx.compose.foundation.layout.Column` and the `SubtitleWithIcon`/`SimpleUnderlineTextField`/`SupportText` usages are gone — but those are same-package and were never imported; only verify `Column` is still used elsewhere in the file (it is, in `NoContentView`/`NoInternetView`/`ErrorView`), so leave it.
- [ ] **Step 3: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL.
- [ ] **Step 4: Verify (iOS source set).** `./gradlew :shared:compileKotlinIosSimulatorArm64` — expected BUILD SUCCESSFUL.
- [ ] **Step 5: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/DefaultViews.kt
git commit -m "Migrate DefaultNoteView to SectionCard + LabeledValueRow

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 10: Refactor Place detail — `DetailHeader`, `TabbedPager`, `EmptyState`, `AddItemButton`, localized overflow, dead-code removal

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/place/PlaceViews.kt:42-423`

Context: The Place screen has the most concentrated set of audit findings: three divergent add affordances, a missing Notes-tab empty state, commented-out dead button blocks, an `Icons.Outlined.MoreVert.name` content description, a hand-wired TabRow+HorizontalPager, a sub-48dp navigate button, and a `.replace(...)` delete-string hack. This task migrates them.

- [ ] **Step 1: Migrate `PlaceTitleView` (PlaceViews.kt:42-100)** to `DetailHeader`. Replace its whole body with:
```kotlin
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
            SupportText(text = place.dateOfCreation.toDateTextMonth())
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
```

> The sub-48dp bug is fixed: `AppIconButton` (Plan 02) enforces a 48dp min hit target internally, and `DetailHeader` applies the header's end inset OUTSIDE the action via its own `padding`, not inside the sized button.

- [ ] **Step 2: Migrate `PlaceTabsView` + `PlaceTabsContentView` (PlaceViews.kt:102-184)** to the shared `TabbedPager`. Delete both functions and replace the two call sites in `Place.kt` (handled in Task 11). For now, in `PlaceViews.kt`, **delete** `PlaceTabsView` (lines 102-143) and `PlaceTabsContentView` (lines 145-184) entirely — the pager wiring moves to `Place.kt` using `TabbedPager(tabs, pagerState, content)` (Plan 03, spec §5.3). Keep `PlaceCatchesView`, `PlaceNotes`, and `NoteModalBottomSheet`.

> **Contract dependency.** `TabbedPager(tabs, pagerState, content)` is owned by Plan 03 (spec §5.3): it renders the surface-based `AppTabRow` (real selected/unselected color states, synchronized indicator, brand accent, no `.uppercase()`) and the `HorizontalPager` with the shared-axis/crossfade transition and `scope.launch + animateScrollToPage` scroll-sync. The local hand-wired `TabRow`/`LeadingIconTab`/indicator is removed.

- [ ] **Step 3: Migrate the Notes tab empty/add affordance in `PlaceNotes` (PlaceViews.kt:202-238).** Replace the bare `Card { Icon(Add) }` and the wrong `Note()` fallback with `AddItemButton` + a real `EmptyState`. Replace the whole `PlaceNotes` body with:
```kotlin
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
```

> **Contract dependency.** `EmptyState(illustration, title, description = null, action = null, modifier)` is owned by Plan 02 (spec §5.2). It centers content in the viewport — replacing the old `padding(top = 128.dp)` hack. `illustration` is a `Painter`. The `NoPhotos`/`NoPlaces`/`NoCatches` presets in spec §5.2 are not used here (notes has no preset); the explicit `illustration + title` overload is used.

- [ ] **Step 4: Migrate the catches empty state in `PlaceCatchesView` (PlaceViews.kt:240-296).** Replace the manual `NoContentView + Spacer + DefaultButtonOutlined` stack with `EmptyState(... action = ...)`. Replace the `catches.isEmpty()` branch (lines 279-293) with:
```kotlin
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
```
Also change the `LazyColumn` `contentPadding` (line 253) from `PaddingValues(horizontal = 4.dp)` to `PaddingValues(horizontal = Spacing.xs)`.

> **Contract dependency.** `EmptyState`'s `action` slot is a `@Composable () -> Unit` (spec §5.2). `AddItemButton` (Task 3) is the unified add control, replacing the old `DefaultButtonOutlined`.

- [ ] **Step 5: Clean `PlaceButtonsView` (PlaceViews.kt:298-347).** Remove the two commented-out `DefaultButtonOutlined` blocks (lines 333-343) and replace the improvised edge `Spacer(padding(4.dp))` (lines 313, 345) with proper `contentPadding`. Migrate to a `LazyRow` with token padding. Replace the whole body with:
```kotlin
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
```

> **Contract dependency.** `AppButton(... leadingIcon: Painter? = null)` and `AppButtonStyle.Outlined` are owned by Plan 02 (spec §5.1) — no forced uppercase, ≥48dp. This replaces `DefaultButtonOutlined` (which force-uppercased labels). The dead commented Edit/Delete blocks are deleted per the project no-comments rule.

- [ ] **Step 6: Fix the delete-confirmation string in `DeletePlaceDialog` (PlaceViews.kt:357).** Replace:
```kotlin
        primaryText = stringResource(Res.string.delete_place_dialog).replace("%s", place.title).replace("%1\$s", place.title),
```
with:
```kotlin
        primaryText = stringResource(Res.string.delete_place_dialog, place.title),
```
(`delete_place_dialog` is `Delete place "%1$s"`.)

- [ ] **Step 7: Surface `PlaceTopBar` (PlaceViews.kt:374-423)** on surface color via `AppTopBar` + `AppBarOverflowMenu`, keeping the visibility toggle. Replace its whole body with:
```kotlin
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
        navigationIcon = {
            AppIconButton(
                onClick = backPress,
                icon = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack),
                contentDescription = stringResource(Res.string.back)
            )
        },
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
```

> The toggle's "on" color changes from `onPrimary` (which assumed a primary-colored bar) to `primary` because the bar is now surface-colored per the locked decision.

- [ ] **Step 8: Update imports in `PlaceViews.kt`.** Add: `import com.mobileprism.fishing.ui.home.views.DetailHeader`, `import com.mobileprism.fishing.ui.home.views.AppTopBar`, `import com.mobileprism.fishing.ui.home.views.AppIconButton`, `import com.mobileprism.fishing.ui.home.views.AppBarOverflowMenu`, `import com.mobileprism.fishing.ui.home.views.OverflowMenuItem`, `import com.mobileprism.fishing.ui.home.views.AppButton`, `import com.mobileprism.fishing.ui.home.views.AppButtonStyle`, `import com.mobileprism.fishing.ui.home.views.EmptyState`, `import com.mobileprism.fishing.ui.home.catch.AddItemButton` is wrong package — `AddItemButton` is in `ui.home.views`, so add `import com.mobileprism.fishing.ui.home.views.AddItemButton`, `import com.mobileprism.fishing.ui.theme.Spacing`, `import androidx.compose.material.icons.automirrored.filled.ArrowBack`, `import androidx.compose.material.icons.outlined.Delete`, `import androidx.compose.ui.graphics.vector.rememberVectorPainter`, `import fishing.shared.generated.resources.no_notes_added`. Remove now-unused imports: `import androidx.compose.foundation.horizontalScroll`, `import androidx.compose.material3.TabRowDefaults`, `import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset`, `import androidx.compose.material.icons.filled.Add`, `import androidx.compose.material.icons.outlined.MoreVert`, `import androidx.compose.foundation.pager.HorizontalPager`, `import androidx.compose.foundation.pager.PagerState`. Keep `PagerState`-related imports only if `PlaceTabsView`/`PlaceTabsContentView` referenced them — they did, so after deletion these become unused and must be removed (already listed). Keep `kotlinx.coroutines.launch`? It was used by `PlaceTabsView`'s `scope.launch` — now removed, so **remove `import kotlinx.coroutines.launch` and `import androidx.compose.runtime.rememberCoroutineScope`** if no other function uses them (grep confirms only `PlaceTabsView` used `scope`).

- [ ] **Step 9: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL. Resolve unresolved references only by merging the owning Plan 01–03 component (never re-implement). Note: deleting `PlaceTabsView`/`PlaceTabsContentView` will break `Place.kt` call sites — that is fixed in Task 11; if you run the compile before Task 11, expect `Place.kt` unresolved-reference errors, which Task 11 resolves. To keep each task green, **do Task 11 immediately after Task 10 in the same review checkpoint** (they are a pair).
- [ ] **Step 10: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/place/PlaceViews.kt
git commit -m "Refactor Place detail views: DetailHeader, EmptyState, AddItemButton, localized overflow, dead-code removal

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 11: Wire Place screen to `ScreenStateContent` + `TabbedPager` and surface the top bar

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/place/Place.kt:51-160`

Context: `UserPlaceScreen` renders blank until `marker` (a nullable `StateFlow`) emits, and the tab pager was hand-wired in the deleted `PlaceTabsView`/`PlaceTabsContentView`. This task gates the body behind `ScreenStateContent` (Plan 02, spec §5.2) so loading/empty/error are distinct, and uses `TabbedPager` (Plan 03, spec §5.3) for the tabs. The `BottomSheetScaffold`-as-ad-banner-pin and `RectangleShape` are kept as-is for this plan (the audit flags it LOW; converting the banner to a plain bottom Surface is out of this plan's scope and tracked in Plan 11's sweep) — but `topBar` becomes `PlaceTopBar` (already surfaced in Task 10).

- [ ] **Step 1: Gate the content on `marker` via `ScreenStateContent`.** Replace the `marker?.let { ... }` block (Place.kt:109-158) inside `BottomSheetScaffold`'s content lambda with a `ScreenStateContent` that distinguishes loading (marker null) from loaded. Replace lines 109-158 with:
```kotlin
            ScreenStateContent(
                modifier = Modifier.fillMaxSize(),
                value = marker,
                loading = { PlaceDetailSkeleton(modifier = Modifier.fillMaxSize()) }
            ) { userPlace ->

                val userCatches by viewModel.getCatchesByMarkerId(userPlace.id)
                    .collectAsState(listOf())

                val tabs = listOf(TabItem.PlaceCatches, TabItem.Note)
                val pagerState = rememberPagerState(initialPage = 0) { tabs.size }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {

                    PlaceTitleView(
                        place = userPlace,
                        catchesAmount = userCatches.size,
                    ) {
                        navController.navigate(
                            MainDestinations.Map(isAddingNewPlace = false, place = userPlace)
                        )
                    }

                    PlaceButtonsView(
                        modifier = Modifier.padding(vertical = Spacing.lg),
                        place = userPlace,
                        navController = navController,
                        viewModel = viewModel
                    )

                    TabbedPager(
                        modifier = Modifier.fillMaxSize(),
                        tabs = tabs,
                        pagerState = pagerState
                    ) { page ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            when (page) {
                                0 -> PlaceCatchesView(
                                    catches = userCatches,
                                    onNewCatchClick = { newCatchClicked(navController, place) }
                                ) {
                                    navController.navigate(MainDestinations.Catch(it))
                                }
                                1 -> PlaceNotes(notes) { note ->
                                    viewModel.setCurrentNote(note)
                                    showModalSheet = true
                                }
                            }
                        }
                    }
                }
            }
```

> **Contract dependency.** `ScreenStateContent` — spec §5.2 lists `ScreenStateContent<T>(state, loading, error, empty, content)` for `BaseViewState`-driven screens. The Place marker is a plain nullable `T?`, not a `BaseViewState`, so use the nullable overload the plan author for Plan 02 must provide: `ScreenStateContent(value: T?, loading, content)` that crossfades a `loading` slot while `value == null` and the `content` slot once non-null. If Plan 02 only ships the `BaseViewState` overload, add the nullable-value overload there (it is owned by Plan 02). `TabbedPager(modifier, tabs, pagerState, content)` is owned by Plan 03 with a `content: @Composable (page: Int) -> Unit` trailing lambda.

- [ ] **Step 2: Add a `PlaceDetailSkeleton` call.** `PlaceDetailSkeleton` is a screen-specific skeleton. Per spec §5.2 the skeleton primitives (`SkeletonBox`, `SkeletonLine`, `CardSkeleton`, `ListItemSkeleton`) are owned by Plan 02. Compose the Place skeleton inline in `PlaceViews.kt` from those primitives by adding this function at the end of `PlaceViews.kt` (do this as part of this task, committing it with `Place.kt`):
```kotlin
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
```
Add `import com.mobileprism.fishing.ui.home.views.SkeletonLine`, `import com.mobileprism.fishing.ui.home.views.CardSkeleton` to `PlaceViews.kt` — **or none if same package** (`SkeletonLine`/`CardSkeleton` live in `ui.home.views`, same package as `PlaceViews.kt`? No — `PlaceViews.kt` is in `ui.home.place`). So add `import com.mobileprism.fishing.ui.home.views.SkeletonLine` and `import com.mobileprism.fishing.ui.home.views.CardSkeleton`.

> **Contract dependency.** `SkeletonLine` and `CardSkeleton` are owned by Plan 02 (spec §5.2). If Plan 02's `CardSkeleton` has a different signature, match it — do not redefine the skeleton primitives here; only compose them.

- [ ] **Step 3: Update imports in `Place.kt`.** Add: `import com.mobileprism.fishing.ui.home.views.ScreenStateContent`, `import com.mobileprism.fishing.ui.home.views.TabbedPager`, `import com.mobileprism.fishing.ui.theme.Spacing`. Remove now-unused: `import androidx.compose.foundation.layout.size` (the trailing `Spacer(size(bottomBannerPadding))` was inside the deleted block — confirm via grep; if the bottom `Spacer` was removed, drop `Spacer`, `size`, and `bottomBannerPadding` imports). Re-check: the old trailing `Spacer(Modifier.size(bottomBannerPadding))` at Place.kt:156 is removed in the replacement (the tab content now owns its own bottom spacing via `PlaceCatchesView`/`PlaceNotes`), so remove `import com.mobileprism.fishing.utils.Constants.bottomBannerPadding`, `import androidx.compose.foundation.layout.Spacer`, and `import androidx.compose.foundation.layout.size` from `Place.kt`.
- [ ] **Step 4: Verify (compile).** `./gradlew :shared:compileDebugKotlinAndroid` — expected BUILD SUCCESSFUL (Task 10 + 11 together resolve all Place references).
- [ ] **Step 5: Verify (iOS source set).** `./gradlew :shared:compileKotlinIosSimulatorArm64` — expected BUILD SUCCESSFUL.
- [ ] **Step 6: Commit.**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/place/Place.kt shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/place/PlaceViews.kt
git commit -m "Gate Place screen on ScreenStateContent + TabbedPager with loading skeleton

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 12: Add a unit test for the parameterized delete strings (i18n bug guard)

**Files:**
- Create (Test): `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/detail/DeleteDialogStringTest.kt`

Context: The delete-confirmation copy was previously assembled with fragile `.replace("%s", x).replace("%1$s", x)` because the author was unsure which placeholder the localized string used. Tasks 7 and 10 switched to canonical positional `stringResource(Res.string..., value)`. This test reads the raw English `strings.xml` resource at runtime via Compose Resources' generated readers is not available in a plain `kotlin.test` JVM unit test, so instead assert against the known-canonical resource format by loading the XML from the test classpath. The shared module already has `commonTest` with `kotlin.test` (confirmed: `UtilFunctionsTest.kt`, `TimeUtilsTest.kt` exist). Because Compose Resources are not trivially loadable in a pure `commonTest`, this test validates the source-of-truth XML file directly via a small string check on its content, which runs on `:shared:testDebugUnitTest`.

- [ ] **Step 1: Write the test.** Read the strings file from the repo at test time using a relative path resolved from the module working directory. `:shared:testDebugUnitTest` runs with the module dir as CWD, so the resource path is `src/commonMain/composeResources/values/strings.xml`.
```kotlin
package com.mobileprism.fishing.ui.home.detail

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DeleteDialogStringTest {

    private val stringsXml: String by lazy {
        val candidates = listOf(
            "src/commonMain/composeResources/values/strings.xml",
            "shared/src/commonMain/composeResources/values/strings.xml"
        )
        val file = candidates
            .map { java.io.File(it) }
            .firstOrNull { it.exists() }
            ?: error("strings.xml not found from CWD ${java.io.File(".").absolutePath}")
        file.readText()
    }

    private fun valueOf(name: String): String {
        val regex = Regex("<string name=\"$name\"[^>]*>(.*?)</string>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(stringsXml)?.groupValues?.get(1)
            ?: error("string '$name' not found")
    }

    @Test
    fun deleteCatchDialog_usesSingleCanonicalPlaceholder() {
        val value = valueOf("delete_catch_dialog")
        assertTrue(value.contains("%1\$s"), "delete_catch_dialog must contain %1\$s, was: $value")
        assertFalse(Regex("(?<!%1)%s").containsMatchIn(value), "delete_catch_dialog must not contain bare %s")
    }

    @Test
    fun deletePlaceDialog_usesSingleCanonicalPlaceholder() {
        val value = valueOf("delete_place_dialog")
        assertTrue(value.contains("%1\$s"), "delete_place_dialog must contain %1\$s, was: $value")
        assertFalse(Regex("(?<!%1)%s").containsMatchIn(value), "delete_place_dialog must not contain bare %s")
    }
}
```

> This test uses `java.io.File`, so it lives in `commonTest` but only runs on the JVM target (`:shared:testDebugUnitTest`). It does not affect the iOS source set. If the team prefers strictly platform-neutral `commonTest`, move this file to `shared/src/androidUnitTest/kotlin/.../DeleteDialogStringTest.kt` (the `androidUnitTest` source set already exists). Default placement is `commonTest` because the JVM is the only target that runs `testDebugUnitTest` and `java.io.File` resolves there.

- [ ] **Step 2: Run the test.** `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.home.detail.DeleteDialogStringTest"` — expected: 2 tests pass. (If CWD resolution fails, the test's `candidates` list handles both `shared/`-prefixed and module-relative invocation.)
- [ ] **Step 3: Commit.**
```bash
git add shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/detail/DeleteDialogStringTest.kt
git commit -m "Add test guarding canonical %1\$s placeholder in delete-dialog strings

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 13: Screenshot checkpoint (user-driven)

**Files:** none (verification only)

- [ ] **Step 1: Build + install.** `./gradlew :androidApp:installDebug` — expected BUILD SUCCESSFUL, installed id `com.merkost.fishingnotes.debug` on both emulators (`emulator-5554`, `emulator-5556`).
- [ ] **Step 2: Screenshot checkpoint (USER).** The user launches the app and captures, in **light and dark**:
  - **Place detail:** loading skeleton (open a place while data resolves), loaded header (`DetailHeader` with colored marker icon + date + catch counter + navigate action), the action button row (New catch / Navigate / Share — sentence case, ≥48dp), Catches tab empty state (`EmptyState` + `AddItemButton`), Notes tab empty state (`no_notes_added` + `AddItemButton`), overflow menu (localized "More options" + red Delete).
  - **Catch detail:** the four sections as `SectionCard`s (title with weight/amount `MetricItem`s, way-of-fishing `LabeledValueRow`s with edit affordance, note section, weather grid with `MetricItem`s), the surface top bar with subtitle, each edit bottom sheet (Fish type/weight, Way of fishing, Note with delete, Photos with counter + gallery/camera icon buttons), and the delete-confirmation dialog showing the fish name correctly interpolated.
- [ ] **Step 3: Review against the audit findings.** Confirm: no `padding(top=128.dp)` empty-state offset; no bare-icon add Card; no uppercase action labels; overflow reads "More options"; clickable sections show an edit affordance; weather degrades to an empty fallback when data is absent; delete dialog shows the correct name with no `%s`/`%1$s` leakage. Iterate before moving on.

---

## Verification & success criteria

- [ ] **App builds + installs:** `./gradlew :androidApp:installDebug` succeeds; app runs on both emulators (`com.merkost.fishingnotes.debug`).
- [ ] **Android compile:** `./gradlew :shared:compileDebugKotlinAndroid` — BUILD SUCCESSFUL.
- [ ] **iOS source set still compiles:** `./gradlew :shared:compileKotlinIosSimulatorArm64` — BUILD SUCCESSFUL (no Android-only API entered `commonMain`; all new components are pure Compose/M3).
- [ ] **Unit test green:** `./gradlew :shared:testDebugUnitTest` — `DeleteDialogStringTest` passes (delete strings use canonical `%1$s`, no bare `%s`).
- [ ] **Screenshots reviewed:** Place detail + Catch detail captured light + dark and approved against the audit findings (Task 13).
- [ ] **Behavioral/i18n fixes landed:** overflow menus use localized `more_options`; both delete dialogs use parameterized `stringResource(..., name)` (no `.replace` hacks); the 1dp Spacer hack removed; commented-out Place buttons removed; Notes tab has a real empty state; Place screen no longer renders blank pre-marker (loading skeleton via `ScreenStateContent`); weather section degrades gracefully; navigate/add icon buttons are ≥48dp.
- [ ] **DRY:** the 4 catch edit sheets share `EditBottomSheetScaffold`; the 4 catch sections share `SectionCard`; the 3 add affordances are one `AddItemButton`; the read-only display values are `LabeledValueRow`; metrics are `MetricItem`; the tab pager is the shared `TabbedPager`.
- [ ] **Deletion gating:** any now-orphan legacy composable touched here (e.g. `PlaceTabsView`/`PlaceTabsContentView` already deleted; `SimpleUnderlineTextField`, `SubtitleWithIcon`, `DefaultButtonOutlined`, the `Cards.kt` `DefaultCard*` family if fully orphaned) is removed ONLY by Plan 11 after a repo-wide grep proves zero references — do not delete them in this plan unless this plan removed their last caller AND a grep confirms zero remaining usages.
