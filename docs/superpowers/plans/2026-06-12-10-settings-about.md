---
# Settings & About Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Modernize the Settings and About screens by consolidating the four single-choice dialogs into one generic `SettingsSelectionDialog`, extracting reusable `SelectableColorSwatch`/`ColorSwatchRow`/`ExpandableSettingsSection` and a navigational `SettingsMenuLink` variant, consuming the shared `InlineBannerCard`/`AppTopBar`/`AppHeroHeader`/`VersionLabel`/`LabeledIconButton`/`AppButton` components, and rebuilding About on the token system while fixing the version-string and contentDescription bugs.

**Architecture:** Settings keeps its `SettingsGroup`/`SettingsMenuLink`/`SettingsSwitch` skeleton but routes all four unit/dark-mode pickers through one generic dialog (single `AUTO_DISMISS_DELAY_MS` constant, non-null `currentValue`, `.entries` everywhere). The theme color picker is extracted to `SelectableColorSwatch` + `ColorSwatchRow` (luminance-aware check tint) wrapped in a reusable `ExpandableSettingsSection`. The location banner is replaced by the shared `InlineBannerCard` with a thin `LocationPermissionBanner` adapter. Both screens share the surface-colored `AppTopBar` (Large variant for Settings) so back-navigation is seamless, and About is rebuilt as a single scrollable token-spaced column on `AppHeroHeader` + `VersionLabel` + `LabeledIconButton`.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3, Koin, Compose Resources. All shared UI in commonMain (KMP/iOS-safe).

**Sprint:** S4 — Account & Finish · **Plan 10 of 11**

**Depends on:**
- Plan 01 (tokens): `Spacing` object (`Spacing.sm/md/lg/xl`, `Spacing.screenH`, `Spacing.cardPadding`, `Spacing.sectionGap`), `Emphasis` (`Emphasis.divider`, `Emphasis.hint`), `Shape.kt` mappings.
- Plan 02 (chrome components): `AppTopBar(title, subtitle, navigationIcon, actions, scrollBehavior, large)` (surface-colored), `InlineBannerCard(tone, icon, title, body, actionLabel, onClick)` + `BannerTone { Info, Warning, Error }`.
- Plan 03 (primitives + brand/content components): `AppButton(text, onClick, modifier, style, enabled, loading, leadingIcon)` + `AppButtonStyle { Filled, Tonal, Outlined, Text }`, `AppHeroHeader`, `VersionLabel`, `LabeledIconButton`.

> **Cross-plan rule:** This is a CONSUME plan for the cross-cutting components above — it shows their call sites only and never re-implements them. It OWNS the Settings-local components (`SettingsSelectionDialog`, `SelectableColorSwatch`, `ColorSwatchRow`, `ExpandableSettingsSection`, the `SettingsMenuLink` chevron variant). Dead-code deletion of `LottieStars`/`ItemsSelection`/`DefaultAppBar` is gated by Plan 11 (final grep-proven sweep); this plan only removes references and marks the now-dead `GetTemperatureUnit`/`GetPressureUnit`/`GetWindSpeedUnit`/`GetDarkModeDialog`/`ThemeColorCircle`/`LottieStars` by replacing them, leaving no live callers.

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt` | Modify | Add `SettingsSelectionDialog<T>`, `SelectableColorSwatch`, `ColorSwatchRow`, `ExpandableSettingsSection`, a navigational `SettingsMenuLink` chevron via new `SettingsNavLink`; replace hardcoded dp with `Spacing`/`Emphasis` tokens. |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/SettingsScreen.kt` | Modify | Route all four pickers through `SettingsSelectionDialog`; use `ExpandableSettingsSection` + `ColorSwatchRow` for theme; replace inline location banner with `LocationPermissionBanner` adapter over `InlineBannerCard`; use `AppTopBar(large = true)`; navigational About row via `SettingsNavLink`; token spacing. |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/AboutApp.kt` | Modify | Rebuild as a single token-spaced scroll column on `AppHeroHeader` + `VersionLabel` + `LabeledIconButton` (filled primary review CTA); migrate raw `sp`/`customColors` to typography/`colorScheme`; consume `AppTopBar`; remove dead `LottieStars`. |
| `shared/src/commonMain/composeResources/values/strings.xml` | Modify | Add `app_version_format` (`%s` placeholder, trailing-space fix), `about_this_app`. |
| `shared/src/commonMain/composeResources/values-ru/strings.xml` | Modify | Russian parallels for the two new strings. |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/theme/ContrastingCheckTintTest.kt` | Create (Test) | `kotlin.test` unit tests for the luminance-aware `contrastingCheckTint(...)` pure function. |

---

## Tasks

### Task 1: Add format/label string resources

**Files:**
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`
- Modify: `shared/src/commonMain/composeResources/values-ru/strings.xml`

Rationale: the existing `current_app_version` = `"Current version:"` has no trailing space, producing `"Current version:1.2.3"` when concatenated (audit About line 486). Replace concatenation with a `%s` format string. Add `about_this_app` so the About row label no longer duplicates the group title (audit Settings line 459).

- [ ] **Step 1: Add English strings.** In `shared/src/commonMain/composeResources/values/strings.xml`, insert these two lines immediately after the existing `<string name="current_app_version">Current version:</string>` line (line 236):
  ```xml
    <string name="app_version_format">Version %s</string>
    <string name="about_this_app">About this app</string>
  ```

- [ ] **Step 2: Add Russian strings.** In `shared/src/commonMain/composeResources/values-ru/strings.xml`, insert these two lines immediately after the existing `<string name="current_app_version">"Текущая версия: "</string>` line (line 227):
  ```xml
    <string name="app_version_format">"Версия %s"</string>
    <string name="about_this_app">"О приложении"</string>
  ```

- [ ] **Step 3: Verify (compile).** Run:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL; the generated `Res.string.app_version_format` and `Res.string.about_this_app` accessors become available (no Kotlin source uses them yet, so this only confirms resource generation).

- [ ] **Step 4: Commit.**
  ```
  git add shared/src/commonMain/composeResources/values/strings.xml shared/src/commonMain/composeResources/values-ru/strings.xml
  git commit -m "Add app_version_format and about_this_app strings

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 2: Add luminance-aware check tint helper + unit tests (TDD)

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt`
- Test: `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/theme/ContrastingCheckTintTest.kt`

This pure function fixes the hardcoded `Color.White` check tint (audit Settings line 467), which is invisible on the light-yellow swatch (`0xFFFFCA28`). It is pure `Color` math (no Compose runtime), so it gets real `kotlin.test` assertions first.

- [ ] **Step 1: Write the failing test.** Create `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/theme/ContrastingCheckTintTest.kt`:
  ```kotlin
  package com.mobileprism.fishing.ui.theme

  import androidx.compose.ui.graphics.Color
  import com.mobileprism.fishing.ui.home.views.contrastingCheckTint
  import kotlin.test.Test
  import kotlin.test.assertEquals

  class ContrastingCheckTintTest {

      @Test
      fun darkSwatchReturnsWhite() {
          val tint = contrastingCheckTint(Color(0xFF1565C0))
          assertEquals(Color.White, tint)
      }

      @Test
      fun lightYellowSwatchReturnsDark() {
          val tint = contrastingCheckTint(Color(0xFFFFCA28))
          assertEquals(Color.Black, tint)
      }

      @Test
      fun pureWhiteReturnsDark() {
          val tint = contrastingCheckTint(Color.White)
          assertEquals(Color.Black, tint)
      }

      @Test
      fun pureBlackReturnsWhite() {
          val tint = contrastingCheckTint(Color.Black)
          assertEquals(Color.White, tint)
      }

      @Test
      fun midGreenReturnsWhite() {
          val tint = contrastingCheckTint(Color(0xFF66BB6A))
          assertEquals(Color.White, tint)
      }
  }
  ```

- [ ] **Step 2: Run the test — expect compile failure (red).** Run:
  ```
  ./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.theme.ContrastingCheckTintTest"
  ```
  Expected: FAILS to compile because `contrastingCheckTint` does not exist yet. This is the red step.

- [ ] **Step 3: Add the implementation.** In `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt`, add these imports to the import block (after the existing `import androidx.compose.ui.graphics.Color` on line 21):
  ```kotlin
  import androidx.compose.ui.graphics.luminance
  ```
  Then add this top-level function at the end of the file (after the last `}`):
  ```kotlin
  fun contrastingCheckTint(background: Color): Color =
      if (background.luminance() > 0.5f) Color.Black else Color.White
  ```

- [ ] **Step 4: Run the test — expect green.** Run:
  ```
  ./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.theme.ContrastingCheckTintTest"
  ```
  Expected: BUILD SUCCESSFUL, 5 tests pass. `Color.luminance()` is a KMP-safe `androidx.compose.ui.graphics` extension; the threshold `0.5f` returns Black for the light-yellow swatch and White for the blue/green/black swatches.

- [ ] **Step 5: Commit.**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/theme/ContrastingCheckTintTest.kt
  git commit -m "Add luminance-aware contrastingCheckTint helper with tests

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 3: Extract SelectableColorSwatch + ColorSwatchRow

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt`

Promote `ThemeColorCircle` (SettingsScreen.kt:357-406) and the theme expand row (291-316) into reusable `SelectableColorSwatch(color, selected, contentDescription, onClick)` and `ColorSwatchRow`, using the `contrastingCheckTint` from Task 2 and `Spacing` tokens. The `color = null` sweep-gradient case (Dynamic theme) stays supported.

- [ ] **Step 1: Add imports.** In `SettingsComponents.kt`, add these imports to the import block (place alphabetically near the existing `androidx.compose.foundation.*` and `androidx.compose.material3.*` imports):
  ```kotlin
  import androidx.compose.foundation.background
  import androidx.compose.foundation.border
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.Arrangement
  import androidx.compose.foundation.layout.Box
  import androidx.compose.foundation.shape.CircleShape
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.Check
  import androidx.compose.material3.Surface
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Brush
  import androidx.compose.ui.semantics.Role
  import androidx.compose.ui.semantics.contentDescription
  import androidx.compose.ui.semantics.semantics
  import com.mobileprism.fishing.ui.theme.Spacing
  ```

- [ ] **Step 2: Add `SelectableColorSwatch`.** Append to `SettingsComponents.kt` (after the `contrastingCheckTint` function):
  ```kotlin
  @Composable
  fun SelectableColorSwatch(
      color: Color?,
      selected: Boolean,
      contentDescription: String,
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
  ) {
      val swatchBrush = if (color != null) {
          SolidColor(color)
      } else {
          Brush.sweepGradient(
              listOf(
                  Color(0xFFEF5350),
                  Color(0xFFAB47BC),
                  Color(0xFF42A5F5),
                  Color(0xFF66BB6A),
                  Color(0xFFFFCA28),
                  Color(0xFFEF5350),
              )
          )
      }
      val checkTint = if (color != null) contrastingCheckTint(color) else Color.White

      Box(
          modifier = modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(swatchBrush)
              .then(
                  if (selected) {
                      Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                  } else {
                      Modifier
                  }
              )
              .clickable(
                  onClick = onClick,
                  role = Role.RadioButton,
              )
              .semantics {
                  this.contentDescription = contentDescription
                  this.selected = selected
              },
          contentAlignment = Alignment.Center,
      ) {
          if (selected) {
              Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = checkTint,
              )
          }
      }
  }
  ```
  Note: add `import androidx.compose.ui.graphics.SolidColor` and `import androidx.compose.ui.unit.dp` to the import block if not already present (the file already imports `androidx.compose.ui.unit.dp` at line 25). `this.selected` inside `semantics` requires `import androidx.compose.ui.semantics.selected`.

- [ ] **Step 3: Add the `selected` semantics import.** Add to the import block:
  ```kotlin
  import androidx.compose.ui.graphics.SolidColor
  import androidx.compose.ui.semantics.selected
  ```

- [ ] **Step 4: Add `ColorSwatchRow`.** Append to `SettingsComponents.kt`:
  ```kotlin
  @Composable
  fun <T> ColorSwatchRow(
      options: List<T>,
      selected: T,
      colorOf: (T) -> Color?,
      contentDescriptionOf: @Composable (T) -> String,
      onSelect: (T) -> Unit,
      modifier: Modifier = Modifier,
  ) {
      Row(
          modifier = modifier
              .fillMaxWidth()
              .padding(horizontal = Spacing.md, vertical = Spacing.md),
          horizontalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterHorizontally),
      ) {
          options.forEach { option ->
              SelectableColorSwatch(
                  color = colorOf(option),
                  selected = option == selected,
                  contentDescription = contentDescriptionOf(option),
                  onClick = { onSelect(option) },
              )
          }
      }
  }
  ```

- [ ] **Step 5: Verify (compile).** Run:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. `SelectableColorSwatch`/`ColorSwatchRow` compile but are not yet called (call sites wired in Task 6).

- [ ] **Step 6: Commit.**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt
  git commit -m "Extract SelectableColorSwatch and ColorSwatchRow

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 4: Add SettingsSelectionDialog, ExpandableSettingsSection, and SettingsNavLink

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt`

One generic single-choice dialog (audit Settings lines 455/457/461/463): label-provider based (NOT bound to `StringOperation`, because the weather enums `TemperatureValues`/`PressureValues`/`WindSpeedValues` do not implement that interface — they expose a `.stringRes` extension instead), non-null `currentValue`, immediate dismiss with an animated radio check, single `AUTO_DISMISS_DELAY_MS` constant. Plus `ExpandableSettingsSection` (audit Settings line 475) and `SettingsNavLink` — the navigational chevron variant of `SettingsMenuLink` (audit Settings line 459).

> **Deviation from spec §5.3 noted:** the spec writes `SettingsSelectionDialog<T : StringOperation>`. The current weather unit enums are plain domain enums (`com.mobileprism.fishing.domain.entity.weather.WeatherUnits`) with a UI-side `.stringRes` extension and are NOT `StringOperation`. To stay KMP-safe without touching the domain layer, this dialog takes an explicit `label: @Composable (T) -> String` provider, which `StringOperation` callers satisfy with `{ stringResource(it.stringRes) }`. Same generic dialog, one delay constant — spec intent preserved.

- [ ] **Step 1: Add imports.** In `SettingsComponents.kt`, add to the import block:
  ```kotlin
  import androidx.compose.animation.AnimatedVisibility
  import androidx.compose.foundation.selection.selectable
  import androidx.compose.foundation.layout.height
  import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
  import androidx.compose.material3.RadioButton
  import androidx.compose.runtime.LaunchedEffect
  import androidx.compose.runtime.getValue
  import androidx.compose.runtime.mutableStateOf
  import androidx.compose.runtime.remember
  import androidx.compose.runtime.setValue
  import com.mobileprism.fishing.ui.home.views.DefaultDialog
  import kotlinx.coroutines.delay
  ```
  Note: `DefaultDialog` is in the same package (`com.mobileprism.fishing.ui.home.views`) — the import is redundant if so; omit it if the IDE flags it as same-package. Keep `import org.jetbrains.compose.resources` accessors only in call sites, not here.

- [ ] **Step 2: Add the delay constant and `SettingsSelectionDialog`.** Append to `SettingsComponents.kt`:
  ```kotlin
  private const val AUTO_DISMISS_DELAY_MS = 200L

  @Composable
  fun <T> SettingsSelectionDialog(
      title: String,
      options: List<T>,
      currentValue: T,
      label: @Composable (T) -> String,
      onSelect: (T) -> Unit,
      onDismiss: () -> Unit,
  ) {
      var pendingSelection by remember { mutableStateOf<T?>(null) }

      LaunchedEffect(pendingSelection) {
          val selection = pendingSelection ?: return@LaunchedEffect
          delay(AUTO_DISMISS_DELAY_MS)
          onSelect(selection)
      }

      DefaultDialog(
          primaryText = title,
          onDismiss = onDismiss,
      ) {
          Column(modifier = Modifier.fillMaxWidth()) {
              options.forEach { option ->
                  val isSelected = (pendingSelection ?: currentValue) == option
                  Row(
                      modifier = Modifier
                          .fillMaxWidth()
                          .height(56.dp)
                          .selectable(
                              selected = isSelected,
                              onClick = { pendingSelection = option },
                          )
                          .padding(horizontal = Spacing.md),
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                      RadioButton(
                          selected = isSelected,
                          onClick = { pendingSelection = option },
                      )
                      Spacer(modifier = Modifier.width(Spacing.md))
                      Text(
                          text = label(option),
                          style = MaterialTheme.typography.bodyLarge,
                          color = MaterialTheme.colorScheme.onSurface,
                      )
                  }
              }
          }
      }
  }
  ```

- [ ] **Step 3: Add `ExpandableSettingsSection`.** Append to `SettingsComponents.kt`:
  ```kotlin
  @Composable
  fun ExpandableSettingsSection(
      title: String,
      subtitle: String?,
      icon: ImageVector?,
      expanded: Boolean,
      onToggle: () -> Unit,
      modifier: Modifier = Modifier,
      content: @Composable () -> Unit,
  ) {
      Column(modifier = modifier.fillMaxWidth()) {
          SettingsMenuLink(
              title = title,
              subtitle = subtitle,
              icon = icon,
              onClick = onToggle,
          )
          AnimatedVisibility(visible = expanded) {
              content()
          }
      }
  }
  ```

- [ ] **Step 4: Add `SettingsNavLink` (navigational chevron variant).** Append to `SettingsComponents.kt`:
  ```kotlin
  @Composable
  fun SettingsNavLink(
      title: String,
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
      subtitle: String? = null,
      icon: ImageVector? = null,
  ) {
      SettingsMenuLink(
          title = title,
          subtitle = subtitle,
          icon = icon,
          onClick = onClick,
          modifier = modifier,
          trailing = {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
          },
      )
  }
  ```

- [ ] **Step 5: Verify (compile).** Run:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. New components compile but are not yet referenced.

- [ ] **Step 6: Commit.**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt
  git commit -m "Add SettingsSelectionDialog, ExpandableSettingsSection, SettingsNavLink

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 5: Migrate SettingsComponents hardcoded dp/alpha to tokens

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt`

Audit (Settings REUSE line 478): `dividerInset 56dp`, `group-title inset 32dp`, row padding `16/14dp`, divider alpha `0.5f` are magic literals. Snap them to `Spacing`/`Emphasis` tokens. Also remove the now-dead raw-`sp` `GrayText`/`SettingsHeader` typography by routing through `MaterialTheme.typography` (keeps the file consistent; these are used by the legacy paths — leave the function but fix the typography).

- [ ] **Step 1: Add `Emphasis` import.** In `SettingsComponents.kt`, add:
  ```kotlin
  import com.mobileprism.fishing.ui.theme.Emphasis
  ```

- [ ] **Step 2: Token the `SettingsGroup` insets.** In `SettingsGroup` (lines 34-50), replace the title padding and surface padding:
  - Replace `modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)` with `modifier = Modifier.padding(start = Spacing.xl, bottom = Spacing.sm)`.
  - Replace `modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)` with `modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md)`.

- [ ] **Step 3: Token the `SettingsMenuLink` paddings.** In `SettingsMenuLink` (lines 62-96):
  - Replace `.padding(horizontal = 16.dp, vertical = 14.dp)` with `.padding(horizontal = Spacing.md, vertical = Spacing.md)`.
  - Replace `Spacer(modifier = Modifier.width(16.dp))` (both occurrences, after the icon and before `trailing`) with `Spacer(modifier = Modifier.width(Spacing.md))`.

- [ ] **Step 4: Token the `SettingsDivider`.** In `SettingsDivider` (lines 124-128), replace:
  ```kotlin
      HorizontalDivider(
          modifier = Modifier.padding(start = 56.dp),
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )
  ```
  with:
  ```kotlin
      HorizontalDivider(
          modifier = Modifier.padding(start = Spacing.xxxl + Spacing.sm),
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Emphasis.divider)
      )
  ```
  Note: `Spacing.xxxl + Spacing.sm = 48 + 8 = 56.dp`, preserving the original inset on the 4dp grid. `Emphasis.divider = 0.12f` (per spec §4.5) replaces the ad-hoc `0.5f`.

- [ ] **Step 5: Token `SettingsCheckbox` padding.** In `SettingsCheckbox` (lines 159-185):
  - Replace `Row(modifier = Modifier.padding(16.dp), ...)` with `Row(modifier = Modifier.padding(Spacing.md), ...)`.
  - Replace `Spacer(modifier = Modifier.size(16.dp))` with `Spacer(modifier = Modifier.size(Spacing.md))`.

- [ ] **Step 6: Verify (compile).** Run:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. (If `Spacing.xl` is not `24.dp`/`Spacing.sm` not `8.dp` per Plan 01, adjust the token names to match the actual Plan 01 values — `Spacing.xl=24, Spacing.sm=8, Spacing.md=12, Spacing.xxxl=48` per spec §4.1. The group-title inset `32.dp` equals `Spacing.xxl`; if `Spacing.xxl=32` exists, prefer it over `Spacing.xl` for the `start = 32.dp` case — use `start = Spacing.xxl`.)

- [ ] **Step 7: Fix the inset token in Step 2 if needed.** Per spec §4.1 `Spacing.xxl=32`. In `SettingsGroup`, set the title inset to `start = Spacing.xxl` (not `Spacing.xl`). Edit the line from Step 2 accordingly so it reads:
  ```kotlin
              modifier = Modifier.padding(start = Spacing.xxl, bottom = Spacing.sm)
  ```

- [ ] **Step 8: Verify (compile) again.** Run:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit.**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsComponents.kt
  git commit -m "Token SettingsComponents spacing and divider alpha

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 6: Rebuild SettingsScreen on the new components

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/SettingsScreen.kt`

Route the three weather pickers + dark mode through `SettingsSelectionDialog` (one delay constant, `.entries`, non-null current value); use `ExpandableSettingsSection` + `ColorSwatchRow` for theme; replace the inline location banner body with a thin adapter over the shared `InlineBannerCard`; consume `AppTopBar(large = true)`; make the About row a `SettingsNavLink` with the `about_this_app` label; token the screen spacing.

- [ ] **Step 1: Replace the file with the consolidated version.** Overwrite `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/SettingsScreen.kt` with:
  ```kotlin
  package com.mobileprism.fishing.ui.home.settings

  import androidx.compose.foundation.layout.Arrangement
  import androidx.compose.foundation.layout.Column
  import androidx.compose.foundation.layout.Spacer
  import androidx.compose.foundation.layout.fillMaxSize
  import androidx.compose.foundation.layout.fillMaxWidth
  import androidx.compose.foundation.layout.height
  import androidx.compose.foundation.layout.padding
  import androidx.compose.foundation.rememberScrollState
  import androidx.compose.foundation.verticalScroll
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.AccessTime
  import androidx.compose.material.icons.filled.Air
  import androidx.compose.material.icons.automirrored.filled.ArrowBack
  import androidx.compose.material.icons.filled.ColorLens
  import androidx.compose.material.icons.filled.Compress
  import androidx.compose.material.icons.filled.DarkMode
  import androidx.compose.material.icons.filled.Info
  import androidx.compose.material.icons.filled.LocationCity
  import androidx.compose.material.icons.filled.LocationOn
  import androidx.compose.material.icons.filled.Thermostat
  import androidx.compose.material.icons.filled.ZoomIn
  import androidx.compose.material3.ExperimentalMaterial3Api
  import androidx.compose.material3.Icon
  import androidx.compose.material3.IconButton
  import androidx.compose.material3.MaterialTheme
  import androidx.compose.material3.Scaffold
  import androidx.compose.material3.TopAppBarDefaults
  import androidx.compose.runtime.Composable
  import androidx.compose.runtime.collectAsState
  import androidx.compose.runtime.getValue
  import androidx.compose.runtime.mutableStateOf
  import androidx.compose.runtime.remember
  import androidx.compose.runtime.rememberCoroutineScope
  import androidx.compose.runtime.setValue
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.input.nestedscroll.nestedScroll
  import androidx.compose.ui.unit.dp
  import androidx.navigation.NavController
  import com.mobileprism.fishing.domain.entity.weather.PressureValues
  import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
  import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
  import com.mobileprism.fishing.model.datastore.UserPreferences
  import com.mobileprism.fishing.model.datastore.WeatherPreferences
  import com.mobileprism.fishing.ui.MainDestinations
  import com.mobileprism.fishing.ui.home.map.LocationPermissionDialog
  import com.mobileprism.fishing.ui.home.views.AppTopBar
  import com.mobileprism.fishing.ui.home.views.BannerTone
  import com.mobileprism.fishing.ui.home.views.ColorSwatchRow
  import com.mobileprism.fishing.ui.home.views.ExpandableSettingsSection
  import com.mobileprism.fishing.ui.home.views.InlineBannerCard
  import com.mobileprism.fishing.ui.home.views.SettingsDivider
  import com.mobileprism.fishing.ui.home.views.SettingsGroup
  import com.mobileprism.fishing.ui.home.views.SettingsNavLink
  import com.mobileprism.fishing.ui.home.views.SettingsSelectionDialog
  import com.mobileprism.fishing.ui.home.views.SettingsSwitch
  import com.mobileprism.fishing.ui.home.weather.stringRes
  import com.mobileprism.fishing.ui.theme.Spacing
  import com.mobileprism.fishing.ui.utils.enums.AppThemeValues
  import com.mobileprism.fishing.ui.utils.enums.DarkModeValues
  import com.mobileprism.fishing.ui.utils.isDynamicColorSupported
  import com.mobileprism.fishing.ui.utils.rememberAppSettingsOpener
  import com.mobileprism.fishing.ui.utils.rememberLocationPermissionGranted
  import com.mobileprism.fishing.ui.utils.rememberPermissionsController
  import fishing.shared.generated.resources.Res
  import fishing.shared.generated.resources.*
  import kotlinx.coroutines.launch
  import org.jetbrains.compose.resources.stringResource
  import org.koin.compose.koinInject

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun SettingsScreen(backPress: () -> Unit, navController: NavController) {
      val userPreferences: UserPreferences = koinInject()
      val weatherPreferences: WeatherPreferences = koinInject()

      val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

      Scaffold(
          topBar = {
              AppTopBar(
                  title = stringResource(Res.string.settings),
                  large = true,
                  scrollBehavior = scrollBehavior,
                  navigationIcon = {
                      IconButton(onClick = backPress) {
                          Icon(
                              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                              contentDescription = stringResource(Res.string.back),
                          )
                      }
                  },
              )
          },
          modifier = Modifier
              .fillMaxSize()
              .nestedScroll(scrollBehavior.nestedScrollConnection),
      ) { paddingValues ->
          Column(
              modifier = Modifier
                  .verticalScroll(rememberScrollState())
                  .padding(paddingValues),
              verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
          ) {
              LocationPermissionBanner(userPreferences)
              GeneralSettingsGroup(userPreferences)
              WeatherSettingsGroup(weatherPreferences)
              AboutSettingsGroup(navController)
              Spacer(modifier = Modifier.height(Spacing.sm))
          }
      }
  }

  @Composable
  private fun LocationPermissionBanner(userPreferences: UserPreferences) {
      val permissionsController = rememberPermissionsController()
      val locationPermissionGranted by rememberLocationPermissionGranted(permissionsController)
      val shouldShowPermissions by userPreferences.shouldShowLocationPermission.collectAsState(true)
      val openAppSettings = rememberAppSettingsOpener()
      var isPermissionDialogOpen by remember { mutableStateOf(false) }

      if (isPermissionDialogOpen) {
          LocationPermissionDialog(
              userPreferences = userPreferences,
              onCloseCallback = { isPermissionDialogOpen = false },
          )
      }

      if (!locationPermissionGranted) {
          InlineBannerCard(
              tone = if (shouldShowPermissions) BannerTone.Warning else BannerTone.Error,
              icon = Icons.Default.LocationOn,
              title = stringResource(
                  if (shouldShowPermissions) {
                      Res.string.location_permission_banner_title
                  } else {
                      Res.string.location_permission_blocked_title
                  }
              ),
              body = stringResource(
                  if (shouldShowPermissions) {
                      Res.string.location_permission_banner_body
                  } else {
                      Res.string.location_permission_blocked_body
                  }
              ),
              actionLabel = stringResource(
                  if (shouldShowPermissions) {
                      Res.string.location_permission_banner_action
                  } else {
                      Res.string.goto_app_settings
                  }
              ),
              onClick = {
                  if (shouldShowPermissions) {
                      isPermissionDialogOpen = true
                  } else {
                      openAppSettings()
                  }
              },
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = Spacing.md),
          )
      }
  }

  @Composable
  private fun GeneralSettingsGroup(userPreferences: UserPreferences) {
      val coroutineScope = rememberCoroutineScope()

      val appTheme by userPreferences.appTheme.collectAsState(AppThemeValues.Blue)
      val use12hTimeFormat by userPreferences.use12hTimeFormat.collectAsState(false)
      val useFastFabAdd by userPreferences.useFabFastAdd.collectAsState(false)
      val useZoomButtons by userPreferences.useMapZoomButons.collectAsState(false)
      val darkMode by userPreferences.darkMode.collectAsState(DarkModeValues.System)

      var isAppThemeExpanded by remember { mutableStateOf(false) }
      var isDarkModeDialogOpen by remember { mutableStateOf(false) }

      if (isDarkModeDialogOpen) {
          SettingsSelectionDialog(
              title = stringResource(Res.string.dark_mode),
              options = DarkModeValues.entries.toList(),
              currentValue = darkMode,
              label = { stringResource(it.titleRes) },
              onSelect = { value ->
                  coroutineScope.launch { userPreferences.saveDarkMode(value) }
                  isDarkModeDialogOpen = false
              },
              onDismiss = { isDarkModeDialogOpen = false },
          )
      }

      val themeEntries = if (isDynamicColorSupported()) {
          AppThemeValues.entries.toList()
      } else {
          AppThemeValues.entries.filter { it != AppThemeValues.Dynamic }
      }

      SettingsGroup(title = stringResource(Res.string.settings_main)) {
          ExpandableSettingsSection(
              title = stringResource(Res.string.app_theme),
              subtitle = stringResource(appTheme.titleRes),
              icon = Icons.Default.ColorLens,
              expanded = isAppThemeExpanded,
              onToggle = { isAppThemeExpanded = !isAppThemeExpanded },
          ) {
              ColorSwatchRow(
                  options = themeEntries,
                  selected = appTheme,
                  colorOf = { it.color },
                  contentDescriptionOf = { stringResource(it.titleRes) },
                  onSelect = { theme ->
                      coroutineScope.launch { userPreferences.saveAppTheme(theme) }
                  },
              )
          }
          SettingsDivider()
          SettingsNavLink(
              title = stringResource(Res.string.dark_mode),
              subtitle = stringResource(darkMode.titleRes),
              icon = Icons.Default.DarkMode,
              onClick = { isDarkModeDialogOpen = true },
          )
          SettingsDivider()
          SettingsSwitch(
              title = stringResource(Res.string.time_format),
              subtitle = stringResource(Res.string.use_12h),
              icon = Icons.Default.AccessTime,
              checked = use12hTimeFormat,
              onCheckedChange = { use12h ->
                  coroutineScope.launch { userPreferences.saveTimeFormatStatus(use12h) }
              },
          )
          SettingsDivider()
          SettingsSwitch(
              title = stringResource(Res.string.map_zoom_buttons),
              subtitle = stringResource(Res.string.map_zoom_buttons_description),
              icon = Icons.Default.ZoomIn,
              checked = useZoomButtons,
              onCheckedChange = { value ->
                  coroutineScope.launch { userPreferences.saveMapZoomButtons(value) }
              },
          )
          SettingsDivider()
          SettingsSwitch(
              title = stringResource(Res.string.fab_fast_add),
              subtitle = stringResource(Res.string.fast_fab_description),
              icon = Icons.Default.LocationCity,
              checked = useFastFabAdd,
              onCheckedChange = { value ->
                  coroutineScope.launch { userPreferences.saveFabFastAdd(value) }
              },
          )
      }
  }

  @Composable
  private fun WeatherSettingsGroup(weatherPreferences: WeatherPreferences) {
      val coroutineScope = rememberCoroutineScope()

      var isPressureDialogOpen by remember { mutableStateOf(false) }
      var isTemperatureDialogOpen by remember { mutableStateOf(false) }
      var isWindSpeedDialogOpen by remember { mutableStateOf(false) }

      val temperatureUnit by weatherPreferences.getTemperatureUnit.collectAsState(TemperatureValues.C)
      val pressureUnit by weatherPreferences.getPressureUnit.collectAsState(PressureValues.mmHg)
      val windSpeedUnit by weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

      if (isTemperatureDialogOpen) {
          SettingsSelectionDialog(
              title = stringResource(Res.string.choose_temperature_unit),
              options = TemperatureValues.entries.toList(),
              currentValue = temperatureUnit,
              label = { stringResource(it.stringRes) },
              onSelect = { value ->
                  coroutineScope.launch { weatherPreferences.saveTemperatureUnit(value) }
                  isTemperatureDialogOpen = false
              },
              onDismiss = { isTemperatureDialogOpen = false },
          )
      }
      if (isPressureDialogOpen) {
          SettingsSelectionDialog(
              title = stringResource(Res.string.choose_pressure_unit),
              options = PressureValues.entries.toList(),
              currentValue = pressureUnit,
              label = { stringResource(it.stringRes) },
              onSelect = { value ->
                  coroutineScope.launch { weatherPreferences.savePressureUnit(value) }
                  isPressureDialogOpen = false
              },
              onDismiss = { isPressureDialogOpen = false },
          )
      }
      if (isWindSpeedDialogOpen) {
          SettingsSelectionDialog(
              title = stringResource(Res.string.choose_wind_speed_unit),
              options = WindSpeedValues.entries.toList(),
              currentValue = windSpeedUnit,
              label = { stringResource(it.stringRes) },
              onSelect = { value ->
                  coroutineScope.launch { weatherPreferences.saveWindSpeedUnit(value) }
                  isWindSpeedDialogOpen = false
              },
              onDismiss = { isWindSpeedDialogOpen = false },
          )
      }

      SettingsGroup(title = stringResource(Res.string.settings_weather)) {
          SettingsNavLink(
              title = stringResource(Res.string.temperature_unit),
              subtitle = stringResource(temperatureUnit.stringRes),
              icon = Icons.Default.Thermostat,
              onClick = { isTemperatureDialogOpen = true },
          )
          SettingsDivider()
          SettingsNavLink(
              title = stringResource(Res.string.pressure_unit),
              subtitle = stringResource(pressureUnit.stringRes),
              icon = Icons.Default.Compress,
              onClick = { isPressureDialogOpen = true },
          )
          SettingsDivider()
          SettingsNavLink(
              title = stringResource(Res.string.wind_speed_unit),
              subtitle = stringResource(windSpeedUnit.stringRes),
              icon = Icons.Default.Air,
              onClick = { isWindSpeedDialogOpen = true },
          )
      }
  }

  @Composable
  private fun AboutSettingsGroup(navController: NavController) {
      SettingsGroup(title = stringResource(Res.string.settings_about)) {
          SettingsNavLink(
              title = stringResource(Res.string.about_this_app),
              icon = Icons.Default.Info,
              onClick = { navController.navigate(MainDestinations.AboutApp) },
          )
      }
  }
  ```

- [ ] **Step 2: Verify `AppTopBar` signature.** Confirm Plan 02's `AppTopBar` exposes `title: String`, `large: Boolean = false`, `scrollBehavior: TopAppBarScrollBehavior? = null`, and `navigationIcon: @Composable (() -> Unit)? = null`. If Plan 02 named the large flag differently (e.g. a `variant` enum or a separate `AppLargeTopBar`), adjust this call site to the exact Plan 02 API — do NOT redefine `AppTopBar` here. Grep to confirm:
  ```
  grep -n "fun AppTopBar" shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/*.kt
  ```
  Expected: a single `AppTopBar` declaration owned by Plan 02.

- [ ] **Step 3: Verify `InlineBannerCard`/`BannerTone` signature.** Confirm Plan 02's `InlineBannerCard(tone: BannerTone, icon: ImageVector, title: String, body: String, actionLabel: String? = null, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier)` and `BannerTone { Info, Warning, Error }`. Grep:
  ```
  grep -n "fun InlineBannerCard\|enum class BannerTone" shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/*.kt
  ```
  Expected: single declarations owned by Plan 02. If parameter names differ, adjust this call site only.

- [ ] **Step 4: Verify (compile).** Run:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. The old private helpers (`GetTemperatureUnit`, `GetPressureUnit`, `GetWindSpeedUnit`, `GetDarkModeDialog`, `ThemeColorCircle`, `SettingsTopAppBar`) are gone from the file; no compile references remain. The `ItemsSelection`/`DefaultDialog`/`LocationPermissionDialog`-banner-Surface code is no longer imported here.

- [ ] **Step 5: Verify (iOS source set compiles).** Run:
  ```
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  ```
  Expected: BUILD SUCCESSFUL — confirms no Android-only API leaked into the changed `commonMain` file.

- [ ] **Step 6: Commit.**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/SettingsScreen.kt
  git commit -m "Rebuild SettingsScreen on SettingsSelectionDialog, swatches, InlineBannerCard, AppTopBar

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 7: Rebuild AboutApp on AppHeroHeader + VersionLabel + LabeledIconButton

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/AboutApp.kt`

Audit (About lines 481-501): replace the brittle weight-6f/4f vertical-center-inside-scroll layout with a single top-aligned scrollable token-spaced column; rebuild hero on `AppHeroHeader`; fix the version concat/space bug via `VersionLabel`; make the review CTA a filled primary `LabeledIconButton` and donation a secondary; migrate raw `sp`/`customColors` to typography/`colorScheme`; consume `AppTopBar`; remove dead `LottieStars`.

- [ ] **Step 1: Verify shared component signatures (consumed from Plan 03).** Grep to confirm the exact APIs before wiring:
  ```
  grep -n "fun AppHeroHeader\|fun VersionLabel\|fun LabeledIconButton\|fun AppButton\|enum class AppButtonStyle" shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/*.kt
  ```
  Expected (Plan 03 owns these):
  - `AppHeroHeader(icon: DrawableResource, title: String, contentDescription: String, modifier: Modifier = Modifier, subtitle: @Composable (() -> Unit)? = null)`
  - `VersionLabel(version: String?, modifier: Modifier = Modifier)` — internally renders the localized `app_version_format` line with a placeholder while `version == null` and an `unknown_version` terminal state, fixing the missing-space concat app-wide.
  - `LabeledIconButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, style: AppButtonStyle = AppButtonStyle.Filled, contentDescription: String? = null)` — icon LEADS the label, decorative icon (`contentDescription = null`), filled = primary / outlined = secondary.

  If Plan 03 chose different parameter names (for example `AppHeroHeader` takes a painter slot, or `VersionLabel` takes the format `StringResource`), adapt the call sites below to the exact Plan 03 API. Do NOT re-implement these components here.

- [ ] **Step 2: Replace the file.** Overwrite `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/AboutApp.kt` with:
  ```kotlin
  package com.mobileprism.fishing.ui.home.settings

  import androidx.compose.foundation.layout.Arrangement
  import androidx.compose.foundation.layout.Column
  import androidx.compose.foundation.layout.fillMaxSize
  import androidx.compose.foundation.layout.fillMaxWidth
  import androidx.compose.foundation.layout.padding
  import androidx.compose.foundation.rememberScrollState
  import androidx.compose.foundation.verticalScroll
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.automirrored.filled.ArrowBack
  import androidx.compose.material.icons.filled.RateReview
  import androidx.compose.material.icons.filled.Savings
  import androidx.compose.material3.ExperimentalMaterial3Api
  import androidx.compose.material3.Icon
  import androidx.compose.material3.IconButton
  import androidx.compose.material3.Scaffold
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import com.mobileprism.fishing.ui.home.views.AppButtonStyle
  import com.mobileprism.fishing.ui.home.views.AppHeroHeader
  import com.mobileprism.fishing.ui.home.views.AppTopBar
  import com.mobileprism.fishing.ui.home.views.LabeledIconButton
  import com.mobileprism.fishing.ui.home.views.VersionLabel
  import com.mobileprism.fishing.ui.theme.Spacing
  import com.mobileprism.fishing.ui.utils.rememberAppVersion
  import com.mobileprism.fishing.ui.utils.rememberBillingLauncher
  import com.mobileprism.fishing.ui.utils.rememberOpenAppStore
  import fishing.shared.generated.resources.Res
  import fishing.shared.generated.resources.*
  import org.jetbrains.compose.resources.stringResource

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun AboutApp(upPress: () -> Unit) {
      val currentVersion = rememberAppVersion()
      val openAppStore = rememberOpenAppStore()
      val launchBilling = rememberBillingLauncher()

      Scaffold(
          topBar = {
              AppTopBar(
                  title = stringResource(Res.string.settings_about),
                  navigationIcon = {
                      IconButton(onClick = upPress) {
                          Icon(
                              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                              contentDescription = stringResource(Res.string.back),
                          )
                      }
                  },
              )
          },
          modifier = Modifier.fillMaxSize(),
      ) { padding ->
          Column(
              modifier = Modifier
                  .padding(padding)
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState())
                  .padding(horizontal = Spacing.screenH),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
              Spacer(modifier = Modifier.height(Spacing.xxl))
              AppHeroHeader(
                  icon = Res.drawable.ic_launcher,
                  title = stringResource(Res.string.app_name),
                  contentDescription = stringResource(Res.string.app_icon),
                  modifier = Modifier.fillMaxWidth(),
                  subtitle = { VersionLabel(version = currentVersion) },
              )
              Spacer(modifier = Modifier.height(Spacing.xl))
              Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(Spacing.md),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                  LabeledIconButton(
                      text = stringResource(Res.string.leave_review),
                      icon = Icons.Default.RateReview,
                      onClick = { openAppStore() },
                      style = AppButtonStyle.Filled,
                      modifier = Modifier.fillMaxWidth(),
                  )
                  if (launchBilling != null) {
                      LabeledIconButton(
                          text = stringResource(Res.string.app_donation),
                          icon = Icons.Default.Savings,
                          onClick = { launchBilling() },
                          style = AppButtonStyle.Tonal,
                          modifier = Modifier.fillMaxWidth(),
                      )
                  }
              }
              Spacer(modifier = Modifier.height(Spacing.xl))
              Text(
                  text = stringResource(Res.string.made_in_russia),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
              )
              Spacer(modifier = Modifier.height(Spacing.xxl))
          }
      }
  }
  ```

- [ ] **Step 3: Add the remaining imports.** The body above references `Spacer`, `height`, `Text`, `MaterialTheme`, `TextAlign`. Add to the import block:
  ```kotlin
  import androidx.compose.foundation.layout.Spacer
  import androidx.compose.foundation.layout.height
  import androidx.compose.material3.MaterialTheme
  import androidx.compose.material3.Text
  import androidx.compose.ui.text.style.TextAlign
  ```

- [ ] **Step 4: Verify the `made_in_russia` keeps the rotating Easter egg OR confirm removal.** The original rotating `MyClickableCard` "made in russia" Easter egg (AboutApp.kt:114-126) is intentionally dropped in favor of a static `Text` (audit About REUSE: the ad-hoc rotating `MyClickableCard` should be replaced by a tasteful signature row). This is a deliberate simplification — confirm with the screenshot checkpoint (Step 8) that the static signature reads well; if the rotation should stay, it is the only behavior change to reconsider here.

- [ ] **Step 5: Verify (compile).** Run:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. `LottieStars`, `AboutAppAppBar`, `DefaultAppBar`, `MyClickableCard`, `customColors`, raw `sp`, `graphicsLayer` rotation, and `AnimatedResource` are no longer referenced in this file.

- [ ] **Step 6: Verify (iOS source set compiles).** Run:
  ```
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  ```
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit.**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/AboutApp.kt
  git commit -m "Rebuild AboutApp on AppHeroHeader, VersionLabel, LabeledIconButton

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

- [ ] **Step 8: Screenshot checkpoint (USER).** Build and install, then the user launches and captures Settings + About in light and dark:
  ```
  ./gradlew :androidApp:installDebug
  ```
  Expected: BUILD SUCCESSFUL; app id `com.merkost.fishingnotes.debug` installs on both running emulators. User captures: (a) Settings top (Large bar) + location banner (granted/denied states), (b) the App-theme expand row with swatches, (c) each unit dialog + dark-mode dialog (animated check, ~200ms auto-dismiss), (d) About hero + version line + filled review CTA + signature row. Review light + dark; iterate on token spacing/contrast before moving on.

---

### Task 8: Confirm no dead references remain in scope (hand-off to Plan 11)

**Files:**
- None (verification only).

This plan replaced all live callers of `ItemsSelection`, `DefaultDialog`-based unit dialogs, `ThemeColorCircle`, `LottieStars`, `AboutAppAppBar`, and `DefaultAppBar` *within Settings/About*. Actual deletion of those now-orphaned declarations is gated by Plan 11's grep-proven sweep (some, like `ItemsSelection`, are still used by `Notes.kt:199/217`). This task records the residual references so Plan 11 can act on them.

- [ ] **Step 1: Grep residual references this plan must NOT break.** Run:
  ```
  grep -rn "ItemsSelection\|LottieStars\|AboutAppAppBar\|five_stars\|DefaultAppBar\|ThemeColorCircle\|GetTemperatureUnit\|GetPressureUnit\|GetWindSpeedUnit\|GetDarkModeDialog" shared/src/commonMain/kotlin
  ```
  Expected results and the required action:
  - `ItemsSelection` — still referenced by `ui/home/notes/Notes.kt` (lines ~199/217). MUST remain. Not this plan's deletion.
  - `LottieStars` / `five_stars` — zero references after Task 7 (the only definition+usage was in `AboutApp.kt`, now removed). Plan 11 deletes the dead `five_stars` Lottie asset if no other consumer; confirm zero.
  - `AboutAppAppBar` — zero references after Task 7. Plan 11 deletes the orphaned declaration (it lived in `AboutApp.kt` and is gone with the rewrite).
  - `DefaultAppBar` — may still be used by other screens (Map/Weather/etc.). MUST remain until Plan 11 confirms zero callers app-wide. Not this plan's deletion.
  - `ThemeColorCircle`, `GetTemperatureUnit`, `GetPressureUnit`, `GetWindSpeedUnit`, `GetDarkModeDialog` — zero references after Task 6 (they were private to `SettingsScreen.kt` and removed with the rewrite). Confirm zero.

- [ ] **Step 2: Verify the whole shared module still compiles (Android + iOS).** Run:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64
  ```
  Expected: BUILD SUCCESSFUL for both.

- [ ] **Step 3: Run the unit-test suite.** Run:
  ```
  ./gradlew :shared:testDebugUnitTest
  ```
  Expected: BUILD SUCCESSFUL; `ContrastingCheckTintTest` (5 tests) passes alongside the existing suite.

- [ ] **Step 4: Commit (no-op safety / note).** No source change in this task. If grep surfaced an unexpected live caller of a removed private helper, fix the call site and commit:
  ```
  git add -A
  git commit -m "Fix residual reference after Settings/About consolidation

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```
  Otherwise skip the commit.

---

## Verification & success criteria

- **Builds + installs:** `./gradlew :androidApp:installDebug` succeeds; app id `com.merkost.fishingnotes.debug` installs on both running emulators (`emulator-5554`, `emulator-5556`).
- **Android compile:** `./gradlew :shared:compileDebugKotlinAndroid` is green after every task.
- **iOS source set still compiles:** `./gradlew :shared:compileKotlinIosSimulatorArm64` is green after Tasks 6 and 7 — confirms no Android-only API entered the changed `commonMain` files (hard KMP/iOS constraint).
- **Unit tests:** `./gradlew :shared:testDebugUnitTest` is green; `ContrastingCheckTintTest` proves the luminance-aware swatch check tint (Black on the light-yellow `0xFFFFCA28` swatch, White on blue/green/black).
- **Screenshots reviewed (USER):** Settings (Large surface top bar, location banner granted/denied, App-theme swatch expand, all four selection dialogs with animated check + ~200ms auto-dismiss, navigational chevrons on Dark mode / unit / About rows) and About (hero header, fixed version line with a space, filled primary review CTA + tonal donation, signature row) all reviewed in **light + dark**.
- **Consolidation achieved:** the 3 unit dialogs + dark-mode dialog collapse to one `SettingsSelectionDialog` (one `AUTO_DISMISS_DELAY_MS`, non-null `currentValue`, `.entries`); `ThemeColorCircle` → `SelectableColorSwatch`/`ColorSwatchRow` (luminance check); inline location banner → shared `InlineBannerCard`; `LargeTopAppBar`/`DefaultAppBar` mismatch → one `AppTopBar`; About reads as the same product as Settings.
- **Bugs fixed:** version string uses `app_version_format` (`%s`, with a space) via `VersionLabel`; About-row label no longer duplicates the group title (`about_this_app`); developer-string `contentDescription`s removed (decorative icons `null`, swatches carry a localized description); dead `LottieStars` removed.
- **Hand-off to Plan 11:** residual `ItemsSelection` (Notes) and `DefaultAppBar` (other screens) intentionally left live; orphaned `LottieStars`/`five_stars`/`AboutAppAppBar` left for Plan 11's grep-gated deletion.
