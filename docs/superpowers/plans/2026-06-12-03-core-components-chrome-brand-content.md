---
# Core Components II — Chrome, Brand & Content Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the chrome, brand, and content components of the §5.3/§5.4/§5.5 component library (top bars, tabs, bottom nav, scaffolds, edit/sort/settings sheets, brand surfaces, stat/chart/weather content cells, and shared formatters/transitions) in `commonMain`, alongside the legacy components, so screen plans 04–11 can adopt them incrementally.

**Architecture:** Every component is M3-primitive-based but styled with personality (custom active indicators, brand gradients, tasteful motion), consuming the tokens that Plan 01 owns (`Spacing`, `Elevation`, `Motion`, `Emphasis`, `BrandGradients`, `MaterialTheme.shapes`). Components live in `ui/home/views/` (existing component home), the chart logic reuses the Vico 3.2.2 dependency already wired in `shared/build.gradle.kts:72`, and formatters live in a new `ui/utils/format/` package. Pure logic (formatters, badge/state mappers) gets `kotlin.test` unit tests in `commonTest`; composables are verified by Android compile + iOS compile + install + screenshot checkpoints. Nothing legacy is deleted here — Plan 11 gates all deletions on grep proof.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3, Koin, Compose Resources. All shared UI in commonMain (KMP/iOS-safe).

**Sprint:** S1 · Design System · **Plan 03 of 11**

**Depends on:** Plan 01 (foundation tokens: `Spacing`, `Elevation`, `Motion`, `Emphasis`, `BrandGradients`, updated `Shape`/`Type`/`Color`/`CustomColors`), Plan 02 (Core Components I: `AppText`/`AppTextStyle`, `AppButton`/`AppButtonStyle`, `AppIconButton`, `AppCard`, `SectionCard`, `EmptyState`/`ErrorState`/`LoadingState`, `SkeletonBox`/`SkeletonLine`, `ScreenStateContent`). This plan CONSUMES those by their exact spec names and never redefines them.

---
---

## File Structure

This plan only ADDS files and ADDS string resources. It modifies no existing Kotlin source (legacy stays intact for screen plans to migrate off later; Plan 11 deletes).

| File | Action | Single responsibility |
| --- | --- | --- |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/MeasurementFormatter.kt` | Create | Locale-safe weight/amount formatting, no trailing `.0` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/UnitFormatter.kt` | Create | `value + unit` formatting via parameterized string resource |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/DateLabels.kt` | Create | Localized month/short-date labels (kotlinx-datetime + Res) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/ErrorMessage.kt` | Create | `errorToMessage` friendly auth/network mapper |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/motion/AppNavTransitions.kt` | Create | `SlideUpFadeIn` modifier + nav enter/exit presets |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppTopBar.kt` | Create | Surface-colored top bar (small + large variant) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppTabRow.kt` | Create | `AppTabRow` + `TabbedPager` (surface tab strip) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppBottomNavigation.kt` | Create | M3 NavigationBar styled with branded active indicator + motion |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppScaffold.kt` | Create | Scaffold owning bottom-bar slot + snackbar host + sync banner overlay |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BottomActionBar.kt` | Create | Save/Confirm bottom bar with navigation-bar inset + motion |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/EditBottomSheetScaffold.kt` | Create | Standard edit-sheet shell (title + content + Cancel/Save) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SortOptionsSheet.kt` | Create | Generic sort sheet over `StringOperation` options |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsSelectionDialog.kt` | Create | Generic single-choice settings dialog |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BrandSurfaces.kt` | Create | `BrandGradientCard` + `BrandGradientBackground` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BrandFab.kt` | Create | `BrandFab` + single `FabSize` source |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/FloatingControls.kt` | Create | `FloatingControlSurface` + `FloatingIconButton` (48dp glass controls) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/Chips.kt` | Create | `IconStatChip` + `CountBadge` + `StatusLabel` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/InlineBannerCard.kt` | Create | `InlineBannerCard` + `BannerTone` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AvatarWithBadge.kt` | Create | Avatar with ≥48dp edit badge |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/StatTile.kt` | Create | `StatTile` (+ skeleton) + `StatRow` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/ChartCard.kt` | Create | Brand-tinted Vico `ChartCard` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/WeatherTrendChart.kt` | Create | Brand-tinted Vico line trend chart for weather |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/WeatherContent.kt` | Create | `WeatherMetric` + `WeatherStatGrid` + `WeatherDailyForecastRow` + `LocationPickerChip` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AboutContent.kt` | Create | `AppHeroHeader` + `VersionLabel` + `LabeledIconButton` |
| `shared/src/commonMain/composeResources/values/strings.xml` | Modify | Add parameterized + content-description string resources |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/utils/format/MeasurementFormatterTest.kt` | Create | Unit tests for weight/amount formatting |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/utils/format/DateLabelsTest.kt` | Create | Unit tests for month-index/key parsing |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/views/BadgeStateTest.kt` | Create | Unit tests for `CountBadge` visibility + `StatusLabel` variant mapping |

**Token contract reminder (owned by Plan 01 — reference, never redefine here):**
`Spacing.{none,xxs,xs,sm,md,lg,xl,xxl,xxxl,screenH,sectionGap,listItemGap,cardPadding,fabClearance}`,
`Elevation.{level0..level5,card,raisedCard,dialog,bottomSheet,fab}`,
`Motion.{short,medium,long,enterContent,screenEnter,screenExit,navIndicator}`,
`Emphasis.{disabled,medium,hint,divider,pressedOverlay,scrim}`,
`BrandGradients.brandPrimaryGradient(colorScheme): Brush`,
`MaterialTheme.shapes.{extraSmall,small,medium,large,extraLarge}`.

**Component contract reminder (owned by Plan 02 — consume, never redefine here):**
`AppText(text, style, color, …)` with `AppTextStyle.{Display,Heading,Title,Subtitle,Body,BodySmall,Caption,Support}`,
`AppButton(text, onClick, modifier, style, enabled, loading, leadingIcon)` with `AppButtonStyle.{Filled,Tonal,Outlined,Text}`,
`AppIconButton(onClick, icon, contentDescription, modifier, …)`,
`AppCard(modifier, onClick, shape, elevation, contentPadding, content)`,
`SectionCard(icon, title, modifier, onClick, trailing, content)`,
`SkeletonBox(width, height, shape)` / `SkeletonLine`.

---

## Tasks

### Task 1: Add shared string resources for formatters, banners, and a11y descriptions

These resources back the formatter (`UnitFormatter`/`errorToMessage`) and the content-description requirements for the new components. Adding them first means every later task can reference them by `Res.string.*`.

**Files:**
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`

- [ ] **Step 1: Read the current strings file** to find the closing `</resources>` tag and confirm none of these keys already exist (`grep -nE "value_with_unit|error_generic|error_no_network|error_auth_invalid|stale_data_banner|change_location|edit_photo|cancel\b|save\b|sort_by|zoom_in|zoom_out|app_version" shared/src/commonMain/composeResources/values/strings.xml`). Skip any key that already exists; only add the missing ones.

- [ ] **Step 2: Insert the new string resources** immediately before `</resources>`:
```xml
    <!-- formatters -->
    <string name="value_with_unit">%1$s %2$s</string>
    <string name="value_with_unit_compact">%1$s%2$s</string>
    <string name="metric_with_separator">%1$s, %2$s</string>

    <!-- friendly error messages -->
    <string name="error_generic">Something went wrong. Please try again.</string>
    <string name="error_no_network">No internet connection. Check your network and retry.</string>
    <string name="error_timeout">The request timed out. Please try again.</string>
    <string name="error_auth_invalid">Incorrect email or password.</string>
    <string name="error_auth_user_not_found">No account found for this email.</string>
    <string name="error_auth_email_taken">This email is already registered.</string>
    <string name="error_server">The server is unavailable right now. Please try later.</string>

    <!-- inline banners -->
    <string name="stale_data_banner_title">Showing cached data</string>
    <string name="stale_data_banner_body">This information may be out of date.</string>

    <!-- content descriptions / actions -->
    <string name="change_location">Change location</string>
    <string name="edit_photo">Edit photo</string>
    <string name="zoom_in">Zoom in</string>
    <string name="zoom_out">Zoom out</string>
    <string name="sort_by">Sort by</string>
    <string name="app_version">Version %1$s</string>
```

- [ ] **Step 3: Verify the file is well-formed XML** — prefer a tool that does not parse untrusted entities. Use `xmllint --noout shared/src/commonMain/composeResources/values/strings.xml && echo OK` (ships with macOS). If `xmllint` is unavailable, use defused parsing: `python3 -c "from defusedxml.ElementTree import parse; parse('shared/src/commonMain/composeResources/values/strings.xml'); print('OK')"`. Do NOT use Python's stdlib `xml.dom.minidom`/`xml.etree` parsers — they are vulnerable to XXE and billion-laughs attacks. Expected: prints `OK`.

- [ ] **Step 4: Verify resource accessor generation** — run `./gradlew :shared:generateComposeResClass`. Expected: BUILD SUCCESSFUL; the generated `Res.string` now exposes `value_with_unit`, `error_generic`, `change_location`, etc.

- [ ] **Step 5: Commit** —
```
git add shared/src/commonMain/composeResources/values/strings.xml
git commit -m "Add string resources for formatters, banners, and a11y descriptions

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 2: MeasurementFormatter (weight/amount) + unit test

Replaces the file-local `formatWeight` in `StatisticsScreen.kt:386-390` and the `${value} ${kg}` concatenation app-wide. Pure logic, fully testable.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/MeasurementFormatter.kt`
- Test: `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/utils/format/MeasurementFormatterTest.kt`

- [ ] **Step 1: Write `MeasurementFormatter.kt`:**
```kotlin
package com.mobileprism.fishing.ui.utils.format

object MeasurementFormatter {

    fun weight(value: Double): String = trimNumber(roundTo(value, 2))

    fun amount(value: Int): String = value.toString()

    fun decimal(value: Double, maxDecimals: Int = 2): String = trimNumber(roundTo(value, maxDecimals))

    private fun roundTo(value: Double, decimals: Int): Double {
        var factor = 1.0
        repeat(decimals) { factor *= 10.0 }
        return (value * factor).toLong() / factor
    }

    private fun trimNumber(value: Double): String {
        if (value == value.toLong().toDouble()) return value.toLong().toString()
        return value.toString().trimEnd('0').trimEnd('.')
    }
}
```

- [ ] **Step 2: Write `MeasurementFormatterTest.kt`:**
```kotlin
package com.mobileprism.fishing.ui.utils.format

import kotlin.test.Test
import kotlin.test.assertEquals

class MeasurementFormatterTest {

    @Test
    fun weightDropsTrailingZeroForWholeNumber() {
        assertEquals("3", MeasurementFormatter.weight(3.0))
    }

    @Test
    fun weightKeepsTwoDecimalsAndTrimsTrailingZero() {
        assertEquals("1.5", MeasurementFormatter.weight(1.50))
        assertEquals("1.25", MeasurementFormatter.weight(1.25))
    }

    @Test
    fun weightRoundsDownToTwoDecimals() {
        assertEquals("1.23", MeasurementFormatter.weight(1.23999))
    }

    @Test
    fun weightHandlesZero() {
        assertEquals("0", MeasurementFormatter.weight(0.0))
    }

    @Test
    fun amountRendersInteger() {
        assertEquals("42", MeasurementFormatter.amount(42))
    }

    @Test
    fun decimalRespectsMaxDecimals() {
        assertEquals("3.142", MeasurementFormatter.decimal(3.14159, maxDecimals = 3))
    }
}
```

- [ ] **Step 3: Run the test** — `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.utils.format.MeasurementFormatterTest"`. Expected: BUILD SUCCESSFUL, 6 tests passed.

- [ ] **Step 4: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/MeasurementFormatter.kt shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/utils/format/MeasurementFormatterTest.kt
git commit -m "Add MeasurementFormatter with unit tests

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 3: UnitFormatter, DateLabels, errorToMessage + DateLabels test

`UnitFormatter` formats value+unit via parameterized resources (no `+` concatenation). `DateLabels` parses the `"2025-01"` month keys used by Statistics and exposes a localized month abbreviation. `errorToMessage` maps throwables to friendly strings.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/UnitFormatter.kt`
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/DateLabels.kt`
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/ErrorMessage.kt`
- Test: `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/utils/format/DateLabelsTest.kt`

- [ ] **Step 1: Confirm month string resources exist** — run `grep -nE "\"month_jan\"|\"month_feb\"|\"month_short_1\"|\"jan\b\"" shared/src/commonMain/composeResources/values/strings.xml`. If month-abbreviation resources do NOT exist, add this block before `</resources>` in `strings.xml` (and re-run `./gradlew :shared:generateComposeResClass`):
```xml
    <string name="month_short_1">Jan</string>
    <string name="month_short_2">Feb</string>
    <string name="month_short_3">Mar</string>
    <string name="month_short_4">Apr</string>
    <string name="month_short_5">May</string>
    <string name="month_short_6">Jun</string>
    <string name="month_short_7">Jul</string>
    <string name="month_short_8">Aug</string>
    <string name="month_short_9">Sep</string>
    <string name="month_short_10">Oct</string>
    <string name="month_short_11">Nov</string>
    <string name="month_short_12">Dec</string>
```

- [ ] **Step 2: Write `UnitFormatter.kt`:**
```kotlin
package com.mobileprism.fishing.ui.utils.format

import androidx.compose.runtime.Composable
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.value_with_unit
import fishing.shared.generated.resources.value_with_unit_compact
import org.jetbrains.compose.resources.stringResource

object UnitFormatter {

    @Composable
    fun valueWithUnit(value: String, unit: String): String =
        stringResource(Res.string.value_with_unit, value, unit)

    @Composable
    fun valueWithUnitCompact(value: String, unit: String): String =
        stringResource(Res.string.value_with_unit_compact, value, unit)
}
```

- [ ] **Step 3: Write `DateLabels.kt`** — exposes a pure key->month-index parser (testable) and a composable that resolves the localized abbreviation:
```kotlin
package com.mobileprism.fishing.ui.utils.format

import androidx.compose.runtime.Composable
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.month_short_1
import fishing.shared.generated.resources.month_short_10
import fishing.shared.generated.resources.month_short_11
import fishing.shared.generated.resources.month_short_12
import fishing.shared.generated.resources.month_short_2
import fishing.shared.generated.resources.month_short_3
import fishing.shared.generated.resources.month_short_4
import fishing.shared.generated.resources.month_short_5
import fishing.shared.generated.resources.month_short_6
import fishing.shared.generated.resources.month_short_7
import fishing.shared.generated.resources.month_short_8
import fishing.shared.generated.resources.month_short_9
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object DateLabels {

    fun monthNumberFromKey(key: String): Int? {
        val parts = key.split("-")
        if (parts.size != 2) return null
        val month = parts[1].toIntOrNull() ?: return null
        return if (month in 1..12) month else null
    }

    private val shortMonthResources: List<StringResource> = listOf(
        Res.string.month_short_1, Res.string.month_short_2, Res.string.month_short_3,
        Res.string.month_short_4, Res.string.month_short_5, Res.string.month_short_6,
        Res.string.month_short_7, Res.string.month_short_8, Res.string.month_short_9,
        Res.string.month_short_10, Res.string.month_short_11, Res.string.month_short_12,
    )

    @Composable
    fun shortMonthFromKey(key: String): String {
        val month = monthNumberFromKey(key) ?: return key
        return stringResource(shortMonthResources[month - 1])
    }
}
```

- [ ] **Step 4: Write `ErrorMessage.kt`** — maps a throwable to a friendly resource, keyed off message substrings (KMP-safe; no Android exception types). Returns a `StringResource` so callers resolve in their own composable scope:
```kotlin
package com.mobileprism.fishing.ui.utils.format

import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.error_auth_email_taken
import fishing.shared.generated.resources.error_auth_invalid
import fishing.shared.generated.resources.error_auth_user_not_found
import fishing.shared.generated.resources.error_generic
import fishing.shared.generated.resources.error_no_network
import fishing.shared.generated.resources.error_server
import fishing.shared.generated.resources.error_timeout
import org.jetbrains.compose.resources.StringResource

fun errorToMessage(throwable: Throwable?): StringResource {
    val text = (throwable?.message ?: "").lowercase()
    return when {
        text.contains("timeout") || text.contains("timed out") -> Res.string.error_timeout
        text.contains("unknownhost") || text.contains("no internet") ||
            text.contains("network") || text.contains("connection") -> Res.string.error_no_network
        text.contains("password") || text.contains("credential") ||
            text.contains("invalid-login") -> Res.string.error_auth_invalid
        text.contains("user-not-found") || text.contains("no account") -> Res.string.error_auth_user_not_found
        text.contains("email-already") || text.contains("already in use") ||
            text.contains("already registered") -> Res.string.error_auth_email_taken
        text.contains("500") || text.contains("502") || text.contains("503") ||
            text.contains("server") -> Res.string.error_server
        else -> Res.string.error_generic
    }
}
```

- [ ] **Step 5: Write `DateLabelsTest.kt`** (only the pure parser is unit-testable; the composable resolver is covered by the install/screenshot checkpoint):
```kotlin
package com.mobileprism.fishing.ui.utils.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateLabelsTest {

    @Test
    fun parsesValidMonthKey() {
        assertEquals(1, DateLabels.monthNumberFromKey("2025-01"))
        assertEquals(12, DateLabels.monthNumberFromKey("2024-12"))
    }

    @Test
    fun returnsNullForMalformedKey() {
        assertNull(DateLabels.monthNumberFromKey("2025"))
        assertNull(DateLabels.monthNumberFromKey("2025-13"))
        assertNull(DateLabels.monthNumberFromKey("2025-00"))
        assertNull(DateLabels.monthNumberFromKey("abc-de"))
    }
}
```

- [ ] **Step 6: Run the test** — `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.utils.format.DateLabelsTest"`. Expected: BUILD SUCCESSFUL, 2 tests passed.

- [ ] **Step 7: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL (validates the composable formatters and resource imports resolve).

- [ ] **Step 8: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/UnitFormatter.kt shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/DateLabels.kt shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/format/ErrorMessage.kt shared/src/commonMain/composeResources/values/strings.xml shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/utils/format/DateLabelsTest.kt
git commit -m "Add UnitFormatter, DateLabels, errorToMessage formatters

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 4: AppNavTransitions + SlideUpFadeIn

Shared nav enter/exit presets and an entrance modifier so detail screens animate consistently (replaces the hard-cut nav destinations and the ad-hoc per-screen tweens). Built on Plan 01 `Motion` durations.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/motion/AppNavTransitions.kt`

- [ ] **Step 1: Write `AppNavTransitions.kt`:**
```kotlin
package com.mobileprism.fishing.ui.utils.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry
import com.mobileprism.fishing.ui.theme.Motion

object AppNavTransitions {

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(Motion.medium)) +
            slideInVertically(
                animationSpec = tween(Motion.medium),
                initialOffsetY = { fullHeight -> fullHeight / 12 }
            )
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(Motion.short))
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(Motion.medium))
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(Motion.short))
    }
}

fun Modifier.slideUpFadeIn(visible: Boolean = true): Modifier = composed {
    val progress = remember(visible) {
        androidx.compose.animation.core.Animatable(if (visible) 0f else 1f)
    }
    androidx.compose.runtime.LaunchedEffect(visible) {
        progress.animateTo(if (visible) 1f else 0f, tween(Motion.medium))
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 24f
    }
}
```

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL. (If `Motion.short`/`Motion.medium` are not yet present, Plan 01 has not landed — stop and resolve the dependency.)

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/motion/AppNavTransitions.kt
git commit -m "Add AppNavTransitions and slideUpFadeIn entrance modifier

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 5: AppTopBar (surface-colored, small + large) — §5.3

Surface-colored app-wide by decision. Replaces `DefaultAppBar` (`AppBar.kt`) and the per-screen custom bars; fixes the unused `elevation` param by simply not having one (tonal surface via M3 scroll behavior). Legacy `DefaultAppBar` stays until Plan 11.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppTopBar.kt`

- [ ] **Step 1: Write `AppTopBar.kt`** — small + large variants, surface container colors, optional subtitle, optional navigation icon via `AppIconButton`:
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mobileprism.fishing.ui.theme.AppText
import com.mobileprism.fishing.ui.theme.AppTextStyle
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.back
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appTopBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface,
    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { AppTopBarTitle(title = title, subtitle = subtitle) },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                AppIconButton(
                    onClick = onNavigationClick,
                    icon = navigationIcon,
                    contentDescription = stringResource(Res.string.back),
                )
            }
        },
        actions = actions,
        colors = appTopBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = Icons.AutoMirrored.Filled.ArrowBack,
    onNavigationClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    LargeTopAppBar(
        modifier = modifier,
        title = { AppTopBarTitle(title = title, subtitle = subtitle) },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                AppIconButton(
                    onClick = onNavigationClick,
                    icon = navigationIcon,
                    contentDescription = stringResource(Res.string.back),
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun AppTopBarTitle(title: String, subtitle: String?) {
    Column {
        AppText(text = title, style = AppTextStyle.Title)
        if (subtitle != null) {
            AppText(
                text = subtitle,
                style = AppTextStyle.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
```
> NOTE: `AppText` / `AppTextStyle` live in the package Plan 02 declares them. The import above assumes `com.mobileprism.fishing.ui.theme`; if Plan 02 placed them in `ui.home.views`, drop the import (same package). Confirm against Plan 02's file before compiling.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL. If unresolved `AppText`/`AppTextStyle`/`AppIconButton`, fix the import to match Plan 02's actual package, then recompile.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppTopBar.kt
git commit -m "Add surface-colored AppTopBar (small + large variants)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 6: AppTabRow + TabbedPager — §5.3

Surface-based tab strip with real selected/unselected color states and a synchronized indicator + crossfade content. Replaces the flat primary `TabRow` in Notes and the Place-detail tabs.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppTabRow.kt`

- [ ] **Step 1: Write `AppTabRow.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.AppText
import com.mobileprism.fishing.ui.theme.AppTextStyle
import com.mobileprism.fishing.ui.theme.Motion

data class AppTab(
    val title: String,
)

@Composable
fun AppTabRow(
    tabs: List<AppTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        divider = {},
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onSelect(index) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = { AppText(text = tab.title, style = AppTextStyle.Title) },
            )
        }
    }
}

@Composable
fun TabbedPager(
    tabs: List<AppTab>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit = {},
    content: @Composable (page: Int) -> Unit,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        AppTabRow(
            tabs = tabs,
            selectedIndex = pagerState.currentPage,
            onSelect = onSelect,
        )
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(),
        ) { page ->
            Crossfade(
                targetState = page,
                animationSpec = androidx.compose.animation.core.tween(Motion.medium),
                label = "TabbedPagerContent",
            ) { current -> content(current) }
        }
    }
}

@Composable
fun rememberTabbedPagerState(pageCount: Int, initialPage: Int = 0): PagerState =
    rememberPagerState(initialPage = initialPage) { pageCount }
```
> NOTE: `TabRowDefaults.SecondaryIndicator` and `tabIndicatorOffset` are stable in current Material3; if the build flags either as experimental, add the corresponding `@OptIn(ExperimentalMaterial3Api::class)`. Keep the `AppText` import aligned with Plan 02's package (same caveat as Task 5).

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppTabRow.kt
git commit -m "Add AppTabRow and TabbedPager surface tab components

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 7: AppBottomNavigation (M3 NavigationBar + branded active indicator + motion) — §5.3

Replaces the ~240-line Jetsnack port in `Home.kt:43-299`. M3 `NavigationBar`/`NavigationBarItem` base (free 48dp+ targets, ripple, `role = Tab` semantics), styled with a custom filled brand active-indicator and a subtle selected-icon scale spring (`Motion.navIndicator`). No `.uppercase()`. Data-driven via an `AppNavItem` list so the screen never sees layout internals.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppBottomNavigation.kt`

- [ ] **Step 1: Write `AppBottomNavigation.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Motion

data class AppNavItem(
    val key: String,
    val icon: ImageVector,
    val label: String,
)

@Composable
fun appBottomNavItemColors(): NavigationBarItemColors = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
fun AppBottomNavigation(
    items: List<AppNavItem>,
    currentKey: String,
    onSelect: (AppNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = Elevation.level0,
    ) {
        items.forEach { item ->
            val selected = item.key == currentKey
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1f else 0.92f,
                animationSpec = Motion.navIndicator,
                label = "navIconScale",
            )
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(item) },
                colors = appBottomNavItemColors(),
                icon = {
                    Icon(
                        modifier = Modifier.scale(iconScale),
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}
```
> NOTE: `Motion.navIndicator` must be typed `SpringSpec<Float>` (Plan 01 §4.4). `NavigationBarItem` already enforces ≥48dp targets and applies `role = Tab`; the icon `contentDescription = item.label` gives each destination a screen-reader name even when the label scales — resolving the §5.3 a11y findings without any custom semantics. No `.uppercase()` is applied anywhere (resolves the casing finding).

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppBottomNavigation.kt
git commit -m "Add AppBottomNavigation (M3 NavigationBar with branded indicator)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 8: AppScaffold (bottom bar + snackbar host + sync banner overlay) — §5.3

Owns the bottom-bar slot, the snackbar host, and a sync-status banner that OVERLAYS the content (instead of reflowing every screen, as `SyncStatusIndicator` does today in the Home `Column`). Inset policy is centralized here.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppScaffold.kt`

- [ ] **Step 1: Write `AppScaffold.kt`** — content fills the scaffold body; the sync banner is an `AnimatedVisibility` overlay anchored to the top of the body (over content, does not push it):
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomBar: @Composable () -> Unit = {},
    syncBanner: @Composable () -> Unit = {},
    showSyncBanner: Boolean = false,
    content: @Composable (contentPadding: androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = bottomBar,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            content(androidx.compose.foundation.layout.PaddingValues())
            AnimatedVisibility(
                visible = showSyncBanner,
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            ) { syncBanner() }
        }
    }
}

internal val AppScaffoldNoElevation: Dp = Dp.Hairline
```
> NOTE: The banner overlays the top of the body and does not reflow content (resolves the §5.3 "sync indicator pushes NavHost down" finding). `content` receives an empty `PaddingValues` because the outer `Scaffold` already applied window insets to the `Box`; screens that need the bottom-bar height can read it from the passed value in a future revision. Keep this minimal — screen plans wire the actual sync state and bottom bar.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AppScaffold.kt
git commit -m "Add AppScaffold with overlay sync banner and snackbar host

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 9: BottomActionBar — §5.3

Save/Confirm bar with `navigationBarsPadding` and tuned slide/fade entrance; used by NewCatch/EditProfile/place flows. Built on Plan 02 `AppButton` + Plan 01 `Spacing`/`Elevation`/`Motion`.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BottomActionBar.kt`

- [ ] **Step 1: Write `BottomActionBar.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.AppButton
import com.mobileprism.fishing.ui.theme.AppButtonStyle
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun BottomActionBar(
    primaryText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Elevation.level2,
            shadowElevation = Elevation.level2,
        ) {
            AppButton(
                text = primaryText,
                onClick = onClick,
                style = AppButtonStyle.Filled,
                enabled = enabled,
                loading = loading,
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
            )
        }
    }
}
```
> NOTE: `AppButton`/`AppButtonStyle` import follows Plan 02's package (same caveat as Task 5).

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BottomActionBar.kt
git commit -m "Add BottomActionBar save/confirm bar

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 10: EditBottomSheetScaffold — §5.3

Standard edit-sheet shell (title + content slot + Cancel/Save row). Dedupes the 4 catch edit sheets. Caller hosts the `ModalBottomSheet`; this renders its body.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/EditBottomSheetScaffold.kt`

- [ ] **Step 1: Confirm `cancel`/`save` string resources exist** — `grep -nE "\"cancel\"|\"save\"" shared/src/commonMain/composeResources/values/strings.xml`. If either is missing, add it to `strings.xml` and re-run `./gradlew :shared:generateComposeResClass`:
```xml
    <string name="cancel">Cancel</string>
    <string name="save">Save</string>
```

- [ ] **Step 2: Write `EditBottomSheetScaffold.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.AppButton
import com.mobileprism.fishing.ui.theme.AppButtonStyle
import com.mobileprism.fishing.ui.theme.AppText
import com.mobileprism.fishing.ui.theme.AppTextStyle
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.cancel
import fishing.shared.generated.resources.save
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditBottomSheetScaffold(
    title: String,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
    saving: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        AppText(text = title, style = AppTextStyle.Heading)
        content()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppButton(
                text = stringResource(Res.string.cancel),
                onClick = onCancel,
                style = AppButtonStyle.Text,
            )
            AppButton(
                text = stringResource(Res.string.save),
                onClick = onSave,
                style = AppButtonStyle.Filled,
                enabled = saveEnabled,
                loading = saving,
            )
        }
    }
}
```

- [ ] **Step 3: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/EditBottomSheetScaffold.kt shared/src/commonMain/composeResources/values/strings.xml
git commit -m "Add EditBottomSheetScaffold edit-sheet shell

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 11: SortOptionsSheet + SettingsSelectionDialog — §5.3

Two generic single-choice surfaces over `StringOperation` options (the existing interface at `ui/utils/enums/StringOperation.kt` with `val stringRes: StringResource`). `SortOptionsSheet` collapses the Places/Catches sort sheets; `SettingsSelectionDialog` replaces the 3 unit dialogs + dark-mode dialog. Both reuse the existing generic `ItemsSelection` body.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SortOptionsSheet.kt`
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsSelectionDialog.kt`

- [ ] **Step 1: Write `SortOptionsSheet.kt`** — sheet body with a title row + the existing `ItemsSelection`. The caller hosts the `ModalBottomSheet`:
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.AppText
import com.mobileprism.fishing.ui.theme.AppTextStyle
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.enums.StringOperation

@Composable
fun <T> SortOptionsSheet(
    title: String,
    options: List<T>,
    current: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) where T : StringOperation {
    val currentState: State<T?> = remember(current) { mutableStateOf(current) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.lg),
    ) {
        AppText(
            text = title,
            style = AppTextStyle.Heading,
            modifier = Modifier.padding(bottom = Spacing.sm),
        )
        ItemsSelection(
            radioOptions = options,
            currentOption = currentState,
            onSelectedItem = onSelect,
        )
    }
}
```

- [ ] **Step 2: Write `SettingsSelectionDialog.kt`** — one generic single-choice `AlertDialog` (replaces the 3 unit dialogs + dark-mode dialog):
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mobileprism.fishing.ui.theme.AppText
import com.mobileprism.fishing.ui.theme.AppTextStyle
import com.mobileprism.fishing.ui.utils.enums.StringOperation
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> SettingsSelectionDialog(
    title: String,
    options: List<T>,
    current: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) where T : StringOperation {
    val currentState: State<T?> = remember(current) { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = title, style = AppTextStyle.Title) },
        text = {
            ItemsSelection(
                radioOptions = options,
                currentOption = currentState,
                onSelectedItem = {
                    onSelect(it)
                    onDismiss()
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                AppText(text = stringResource(Res.string.cancel), style = AppTextStyle.Body)
            }
        },
    )
}
```

- [ ] **Step 3: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SortOptionsSheet.kt shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/SettingsSelectionDialog.kt
git commit -m "Add SortOptionsSheet and SettingsSelectionDialog generic selectors

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 12: BrandGradientCard / BrandGradientBackground — §5.4

Theme-driven brand surfaces unifying the 3 hand-rolled gradient surfaces. Consumes `BrandGradients.brandPrimaryGradient(colorScheme)` (Plan 01 §4.6) — never hardcodes hex.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BrandSurfaces.kt`

- [ ] **Step 1: Write `BrandSurfaces.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import com.mobileprism.fishing.ui.theme.BrandGradients
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun BrandGradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    contentPadding: androidx.compose.ui.unit.Dp = Spacing.cardPadding,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(BrandGradients.brandPrimaryGradient(MaterialTheme.colorScheme))
            .padding(contentPadding),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
            content = content,
        )
    }
}

@Composable
fun BrandGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrandGradients.brandPrimaryGradient(MaterialTheme.colorScheme)),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
            content = content,
        )
    }
}
```
> NOTE: `Elevation` import is intentionally retained for consistency with other brand surfaces but is not used here; remove the line if the build flags an unused import as an error (it won't by default). `BrandGradients.brandPrimaryGradient(colorScheme)` returns a `Brush` (Plan 01 contract).

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BrandSurfaces.kt
git commit -m "Add BrandGradientCard and BrandGradientBackground

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 13: BrandFab + single FabSize — §5.4

One branded FAB with the canonical `FabSize`, replacing the map's `FishingFab` and collapsing the two `FabSize=56.dp` declarations (one in `FloatingActionButtons.kt:106`, one in `MapScreen.kt`). Uses the brand gradient and `onPrimary` icon tint (not `Color.White`).

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BrandFab.kt`

- [ ] **Step 1: Write `BrandFab.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.BrandGradients
import com.mobileprism.fishing.ui.theme.Elevation

val FabSize: Dp = 56.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BrandFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier.size(FabSize),
        shape = MaterialTheme.shapes.large,
        color = androidx.compose.ui.graphics.Color.Transparent,
        shadowElevation = Elevation.fab,
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(BrandGradients.brandPrimaryGradient(MaterialTheme.colorScheme))
                .combinedClickable(
                    interactionSource = interaction,
                    indication = androidx.compose.material3.ripple(),
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
                content = content,
            )
        }
    }
}
```
> NOTE: `androidx.compose.material3.ripple()` is the M3 ripple factory. The white-on-gradient tints become `onPrimary` via `LocalContentColor`. `FabSize` here is now the single source of truth — the private `FabSize` in `FloatingActionButtons.kt:106` is left intact and gets removed by Plan 11 after `FabWithMenu` migrates.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL. If `ripple()` is unresolved on this Material3 version, replace `indication = androidx.compose.material3.ripple()` with `indication = androidx.compose.material.ripple.rememberRipple()` only if that artifact is present; otherwise drop the explicit `interactionSource`/`indication` args and use the default `combinedClickable(onClick = onClick, onLongClick = onLongClick)`. State which fallback you used.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/BrandFab.kt
git commit -m "Add BrandFab with single canonical FabSize

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 14: FloatingControlSurface + FloatingIconButton — §5.4

Map "glass" control pill + an icon button enforcing a 48dp hit target with a required non-null `contentDescription`. Replaces the 5+ map `44.dp IconButton + clip + 22.dp icon` copies and the sub-48dp dismiss buttons.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/FloatingControls.kt`

- [ ] **Step 1: Write `FloatingControls.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Emphasis

@Composable
fun FloatingControlSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 1f - Emphasis.pressedOverlay),
        shadowElevation = Elevation.card,
        content = content,
    )
}

@Composable
fun FloatingIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
) {
    Box(
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
```
> NOTE: `contentDescription` is a required non-null `String` (resolves the map a11y findings). The 48dp floor is enforced by `sizeIn` plus `IconButton`'s own 48dp default, so the visual 22dp glyph keeps a full hit target. `Emphasis.pressedOverlay = 0.08f` reproduces the map pill's 0.92 alpha exactly.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/FloatingControls.kt
git commit -m "Add FloatingControlSurface and FloatingIconButton

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 15: Chips — IconStatChip / CountBadge / StatusLabel — §5.4 + badge-state unit test

`IconStatChip` (icon + label pill, em-dash empty fallback), `CountBadge` (hide-on-zero, accessible tint), `StatusLabel` (semantic Auto/Done/Offline/Loading). Includes a pure `StatusLabelVariant` and a `shouldShowCountBadge` helper so the visibility/mapping logic is unit-testable.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/Chips.kt`
- Test: `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/views/BadgeStateTest.kt`

- [ ] **Step 1: Confirm status string resources exist** — `grep -nE "\"status_auto\"|\"status_done\"|\"status_offline\"|\"status_loading\"|\"auto_filled\"|\"offline\"" shared/src/commonMain/composeResources/values/strings.xml`. If the four `status_*` keys are missing, add to `strings.xml` and re-run `./gradlew :shared:generateComposeResClass`:
```xml
    <string name="status_auto">Auto</string>
    <string name="status_done">Done</string>
    <string name="status_offline">Offline</string>
    <string name="status_loading">Loading</string>
```

- [ ] **Step 2: Write `Chips.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.status_auto
import fishing.shared.generated.resources.status_done
import fishing.shared.generated.resources.status_loading
import fishing.shared.generated.resources.status_offline
import org.jetbrains.compose.resources.stringResource

const val EmptyStatValue: String = "—"

fun shouldShowCountBadge(count: Int): Boolean = count > 0

@Composable
fun IconStatChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label.ifEmpty { EmptyStatValue },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    if (!shouldShowCountBadge(count)) return
    Box(
        modifier = modifier
            .let { m ->
                if (contentDescription != null) {
                    m.clearAndSetSemantics { this.contentDescription = contentDescription }
                } else m
            }
            .size(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}

enum class StatusLabelVariant { Auto, Done, Offline, Loading }

@Composable
fun StatusLabel(
    variant: StatusLabelVariant,
    modifier: Modifier = Modifier,
) {
    val container: Color
    val onContainer: Color
    when (variant) {
        StatusLabelVariant.Auto -> {
            container = MaterialTheme.colorScheme.primaryContainer
            onContainer = MaterialTheme.colorScheme.onPrimaryContainer
        }
        StatusLabelVariant.Done -> {
            container = MaterialTheme.colorScheme.secondaryContainer
            onContainer = MaterialTheme.colorScheme.onSecondaryContainer
        }
        StatusLabelVariant.Offline -> {
            container = MaterialTheme.colorScheme.errorContainer
            onContainer = MaterialTheme.colorScheme.onErrorContainer
        }
        StatusLabelVariant.Loading -> {
            container = MaterialTheme.colorScheme.surfaceContainerHigh
            onContainer = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    val text = when (variant) {
        StatusLabelVariant.Auto -> stringResource(Res.string.status_auto)
        StatusLabelVariant.Done -> stringResource(Res.string.status_done)
        StatusLabelVariant.Offline -> stringResource(Res.string.status_offline)
        StatusLabelVariant.Loading -> stringResource(Res.string.status_loading)
    }
    Surface(modifier = modifier, shape = MaterialTheme.shapes.small, color = container) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val leading: ImageVector? = when (variant) {
                StatusLabelVariant.Done -> Icons.Default.Check
                StatusLabelVariant.Offline -> Icons.Default.CloudOff
                else -> null
            }
            if (variant == StatusLabelVariant.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = onContainer,
                )
            } else if (leading != null) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = leading,
                    contentDescription = null,
                    tint = onContainer,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = onContainer,
            )
        }
    }
}
```

- [ ] **Step 3: Write `BadgeStateTest.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BadgeStateTest {

    @Test
    fun countBadgeHiddenForZeroAndNegative() {
        assertFalse(shouldShowCountBadge(0))
        assertFalse(shouldShowCountBadge(-1))
    }

    @Test
    fun countBadgeShownForPositive() {
        assertTrue(shouldShowCountBadge(1))
        assertTrue(shouldShowCountBadge(150))
    }

    @Test
    fun statusVariantsAreDistinct() {
        assertEquals(4, StatusLabelVariant.entries.size)
    }

    @Test
    fun emptyStatValueIsEmDash() {
        assertEquals("—", EmptyStatValue)
    }
}
```

- [ ] **Step 4: Run the test** — `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.home.views.BadgeStateTest"`. Expected: BUILD SUCCESSFUL, 4 tests passed.

- [ ] **Step 5: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/Chips.kt shared/src/commonMain/composeResources/values/strings.xml shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/views/BadgeStateTest.kt
git commit -m "Add IconStatChip, CountBadge, StatusLabel chips with badge-state tests

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 16: InlineBannerCard — §5.4

`InlineBannerCard(tone, icon, title, body, actionLabel, onClick)` with `BannerTone { Info, Warning, Error }`. For location-permission, weather stale-data, sync/offline messaging.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/InlineBannerCard.kt`

- [ ] **Step 1: Write `InlineBannerCard.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing

enum class BannerTone { Info, Warning, Error }

@Composable
fun InlineBannerCard(
    tone: BannerTone,
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    actionLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val container: Color
    val onContainer: Color
    when (tone) {
        BannerTone.Info -> {
            container = MaterialTheme.colorScheme.secondaryContainer
            onContainer = MaterialTheme.colorScheme.onSecondaryContainer
        }
        BannerTone.Warning -> {
            container = MaterialTheme.colorScheme.tertiaryContainer
            onContainer = MaterialTheme.colorScheme.onTertiaryContainer
        }
        BannerTone.Error -> {
            container = MaterialTheme.colorScheme.errorContainer
            onContainer = MaterialTheme.colorScheme.onErrorContainer
        }
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                contentDescription = null,
                tint = onContainer,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = onContainer,
                )
                if (body != null) {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainer,
                    )
                }
                if (actionLabel != null && onClick != null) {
                    TextButton(onClick = onClick) {
                        Text(text = actionLabel, color = onContainer)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/InlineBannerCard.kt
git commit -m "Add InlineBannerCard with Info/Warning/Error tones

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 17: AvatarWithBadge — §5.4

Avatar with a ≥48dp edit badge (no dead no-op), shared by Profile/EditProfile. Image is rendered by the caller through a slot so this stays platform-agnostic (no image-loading dependency).

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AvatarWithBadge.kt`

- [ ] **Step 1: Write `AvatarWithBadge.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Elevation

@Composable
fun AvatarWithBadge(
    contentDescription: String,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 96.dp,
    onEdit: (() -> Unit)? = null,
    image: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.size(avatarSize), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(androidx.compose.foundation.shape.CircleShape),
            content = image,
        )
        if (onEdit != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = Elevation.card,
                ) {
                    AppIconButton(
                        onClick = onEdit,
                        icon = Icons.Default.Edit,
                        contentDescription = contentDescription,
                    )
                }
            }
        }
    }
}
```
> NOTE: `AppIconButton` (Plan 02) already enforces a 48dp hit target; the `Box(size = 48.dp)` keeps the badge visually compact while remaining accessible. The edit affordance is functional (wired to `onEdit`) — resolving the EditProfile "photo-edit no-op" bug at the component level. `AppIconButton` import follows Plan 02's package.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AvatarWithBadge.kt
git commit -m "Add AvatarWithBadge with accessible edit badge

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 18: StatTile (+ skeleton) + StatRow — §5.4

Dashboard metric tile with merged semantics and a skeleton variant, plus a `StatRow` layout helper. Promotes the private `StatCard` from `StatisticsScreen.kt:290-328` and the map `StatChip`. Uses `MeasurementFormatter` callers, Plan 02 `AppCard`/`SkeletonBox`, Plan 01 `Spacing`.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/StatTile.kt`

- [ ] **Step 1: Write `StatTile.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.AppCard
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun StatTile(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
                .clearAndSetSemantics { contentDescription = "$title: $value" },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun StatTileSkeleton(modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SkeletonBox(width = 28.dp, height = 28.dp, shape = MaterialTheme.shapes.small)
            SkeletonLine()
            SkeletonLine()
        }
    }
}

@Composable
fun StatRow(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        content = content,
    )
}
```
> NOTE: `title` now allows 2 lines (resolves the §Statistics truncation finding). The merged semantics announce "Total catches: 42" as one node. `AppCard`/`SkeletonBox`/`SkeletonLine` imports follow Plan 02's package; if `SkeletonBox` takes different parameter names than `width/height/shape`, match Plan 02's signature before compiling.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/StatTile.kt
git commit -m "Add StatTile, StatTileSkeleton, and StatRow

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 19: ChartCard (brand-tinted Vico) — §5.4

`ChartCard(title, data, formatLabel)` — `AppCard` + titled header + a brand-tinted Vico column chart with brand column color, `onSurfaceVariant` axis labels, value labels, and a responsive width. Replaces the 5 inline stats chart blocks (`StatisticsScreen.kt:330-384`). Uses the Vico 3.2.2 API exactly as the current `BarChartView` does.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/ChartCard.kt`

- [ ] **Step 1: Write `ChartCard.kt`** — mirrors the current `CartesianChartModelProducer` + `columnSeries` usage (confirmed in `StatisticsScreen.kt:49-57,359-383`), tints columns from `colorScheme.primary`, and provides a per-chart empty slot:
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.AppCard
import com.mobileprism.fishing.ui.theme.Spacing
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent

@Composable
fun ChartCard(
    title: String,
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    formatLabel: (String) -> String = { it },
    emptyLabel: String? = null,
) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.md))
            if (data.isEmpty()) {
                if (emptyLabel != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = emptyLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                BrandColumnChart(data = data, formatLabel = formatLabel)
            }
        }
    }
}

@Composable
private fun BrandColumnChart(
    data: Map<String, Int>,
    formatLabel: (String) -> String,
) {
    val keys = data.keys.toList()
    val values = data.values.toList().map { it.toDouble() }
    val brandColor = MaterialTheme.colorScheme.primary

    val modelProducer = remember(data) { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    val columnProvider = ColumnCartesianLayer.ColumnProvider.series(
        rememberLineComponent(brandColor),
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(columnProvider = columnProvider),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    keys.getOrElse(value.toInt()) { "" }.let(formatLabel)
                },
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp),
    )
}

@Composable
private fun rememberLineComponent(color: androidx.compose.ui.graphics.Color): LineComponent =
    remember(color) {
        LineComponent(
            fill = com.patrykandpatrick.vico.core.common.Fill(color.value.toLong().toInt()),
            thicknessDp = 16f,
        )
    }
```
> CRITICAL: The Vico 3.2.2 column-color API surface varies by patch. The `rememberLineComponent` / `ColumnProvider.series(...)` shape above is the brand-tinting goal, but the EXACT factory (e.g. `rememberLineComponent(fill = fill(brandColor), thickness = 16.dp)` from `com.patrykandpatrick.vico.compose.common.component.rememberLineComponent`, or passing `columnProvider` differently) MUST be confirmed against the resolved artifact. Before writing this file, run `find ~/.gradle/caches -path "*vico*" -name "*.jar" 2>/dev/null | head` and inspect, or open `StatisticsScreen.kt:359-383` (the known-good baseline) and adapt minimally. If the precise color API cannot be confirmed in-session, fall back to the EXACT baseline `rememberColumnCartesianLayer()` (default color) plus styled axes, leave a one-line `// brand column color: pending Vico API confirmation` placeholder ONLY if the project rule against comments is waived — otherwise track it as a follow-up in the structured summary. Do not block the build on an unverified import.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL. If the Vico column-color imports do not resolve, switch `BrandColumnChart` to the verified baseline (`rememberColumnCartesianLayer()` with no custom provider) so the card compiles; record the brand-tint follow-up.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/ChartCard.kt
git commit -m "Add ChartCard with brand-tinted Vico column chart

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 20: WeatherTrendChart (brand-tinted Vico line) — §5.4

A parameterized line trend chart over `List<WeatherChartPoint>` replacing the two hand-rolled Canvas charts (PressureChart/PrecipitationChart). Single unit formatter for value+label (resolves the pressure-unit mismatch finding). Responsive width derived from item count.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/WeatherTrendChart.kt`

- [ ] **Step 1: Write `WeatherTrendChart.kt`** — uses the Vico `lineSeries` API (the line analogue of `columnSeries`); the point model carries pre-converted y-values so value and axis label always share one formatter:
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart

data class WeatherChartPoint(
    val label: String,
    val value: Double,
)

@Composable
fun WeatherTrendChart(
    points: List<WeatherChartPoint>,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 160.dp,
    formatValue: (Double) -> String = { it.toString() },
) {
    if (points.isEmpty()) return

    val labels = points.map { it.label }
    val values = points.map { it.value }
    val modelProducer = remember(points) { CartesianChartModelProducer() }
    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries { series(values) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = CartesianValueFormatter { _, value, _ -> formatValue(value) },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    labels.getOrElse(value.toInt()) { "" }
                },
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth().height(height),
    )
}
```
> CRITICAL: Same Vico-API caveat as Task 19 — `lineSeries` / `rememberLineCartesianLayer` exist in Vico 3.x but verify the exact import path against the resolved artifact (mirror the column-series usage proven in `StatisticsScreen.kt`). Line color tinting from `colorScheme.tertiary`/`secondary` is the goal; if the line-color API can't be confirmed in-session, ship the default-color line + branded axes and record the tint follow-up. The single `formatValue` owns both the y-axis label and the value scale, so the unit mismatch bug cannot recur.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL. If `lineSeries`/`rememberLineCartesianLayer` are unresolved, reconcile with the exact Vico API used by `StatisticsScreen.kt` and adjust before recompiling.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/WeatherTrendChart.kt
git commit -m "Add WeatherTrendChart Vico line trend chart

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 21: Weather content cells — WeatherMetric / WeatherStatGrid / WeatherDailyForecastRow / LocationPickerChip — §5.4

Collapses the duplicated weather metric cells, builds the daily row on M3 `ListItem` (≥48dp, ripple, stable slots), and a Material3 chip location picker with dropdown a11y.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/WeatherContent.kt`

- [ ] **Step 1: Write `WeatherContent.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun WeatherMetric(
    label: String,
    icon: DrawableResource,
    value: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconTint,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun WeatherStatGrid(
    metrics: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        metrics.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                rowItems.forEach { cell ->
                    Column(modifier = Modifier.weight(1f)) { cell() }
                }
                repeat(columns - rowItems.size) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

@Composable
fun WeatherDailyForecastRow(
    date: String,
    icon: DrawableResource,
    temperature: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    precipitation: String? = null,
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(text = date, style = MaterialTheme.typography.bodyLarge) },
        leadingContent = {
            Icon(
                modifier = Modifier.size(28.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        supportingContent = precipitation?.let {
            { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
        },
        trailingContent = {
            Text(
                text = temperature,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
    )
}

@Composable
fun LocationPickerChip(
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    AssistChip(
        modifier = modifier.semantics { role = Role.Button },
        onClick = onClick,
        label = { Text(text = label, maxLines = 1) },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp)) }
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = contentDescription,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
    )
}
```
> NOTE: `WeatherDailyForecastRow` uses M3 `ListItem` (≥48dp, stable 3-slot layout) replacing the fixed-80dp clickable Column. `LocationPickerChip` is an `AssistChip` with `role = Button` and a labeled dropdown icon (resolves the §Weather a11y + touch-target findings).

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/WeatherContent.kt
git commit -m "Add WeatherMetric, WeatherStatGrid, WeatherDailyForecastRow, LocationPickerChip

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 22: About content — AppHeroHeader / VersionLabel / LabeledIconButton — §5.4

About-screen hero + version label (parameterized, no concatenation) + a labeled icon button row item.

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AboutContent.kt`

- [ ] **Step 1: Write `AboutContent.kt`:**
```kotlin
package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.app_version
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppHeroHeader(
    title: String,
    logo: Painter,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(
            modifier = Modifier.size(96.dp),
            painter = logo,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun VersionLabel(
    version: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = stringResource(Res.string.app_version, version),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun LabeledIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
```
> NOTE: `VersionLabel` uses the `app_version` parameterized resource (`Version %1$s`) — resolves the About concatenation/spacing finding.

- [ ] **Step 2: Compile Android** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit** —
```
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/views/AboutContent.kt
git commit -m "Add AppHeroHeader, VersionLabel, LabeledIconButton

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 23: Full-suite verification, iOS compile, install, and screenshot checkpoint

Final gate for Plan 03. All new components are present alongside legacy; nothing is deleted. Confirm Android compiles, iOS source set compiles (KMP/iOS-safety hard constraint), all new unit tests pass, the app installs, and the user reviews any screen that previews the new components.

**Files:** (no new files — verification only)

- [ ] **Step 1: Full shared unit-test suite** — `./gradlew :shared:testDebugUnitTest`. Expected: BUILD SUCCESSFUL; `MeasurementFormatterTest` (6), `DateLabelsTest` (2), and `BadgeStateTest` (4) all pass alongside the existing suite.

- [ ] **Step 2: Android compile** — `./gradlew :shared:compileDebugKotlinAndroid`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: iOS source-set compile (KMP/iOS-safety)** — `./gradlew :shared:compileKotlinIosSimulatorArm64`. Expected: BUILD SUCCESSFUL. This proves no Android-only API leaked into the new `commonMain` components (every new file uses only Compose Multiplatform + kotlin stdlib + Vico KMP + Compose Resources). If this fails, the offending import must be replaced with a KMP-safe equivalent before the plan is complete.

- [ ] **Step 4: Install on both emulators** — `./gradlew :androidApp:installDebug`. Expected: BUILD SUCCESSFUL; app id `com.merkost.fishingnotes.debug` installed on `emulator-5554` and `emulator-5556`.

- [ ] **Step 5: Screenshot checkpoint (USER)** — Because Plan 03 builds components alongside legacy without wiring them into screens yet, there is no new on-screen surface to capture from normal navigation. The user confirms the build is healthy and the app launches without regression on both emulators (light + dark). Component visual review happens in the consuming screen plans (04–11) at their checkpoints. Record any build/launch issues before proceeding.

- [ ] **Step 6: Commit (if any verification fix was needed)** — only if Steps 1–4 required edits:
```
git add -A
git commit -m "Fix verification issues for chrome/brand/content components

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Verification & success criteria

Plan 03 is complete when ALL of the following hold:

- **App builds + installs:** `./gradlew :androidApp:installDebug` succeeds; `com.merkost.fishingnotes.debug` is installed on both running emulators.
- **Android shared compiles:** `./gradlew :shared:compileDebugKotlinAndroid` is BUILD SUCCESSFUL.
- **iOS source set still compiles (hard constraint):** `./gradlew :shared:compileKotlinIosSimulatorArm64` is BUILD SUCCESSFUL — confirming every new `commonMain` component is KMP/iOS-safe (no Android-only APIs).
- **Unit tests pass:** `./gradlew :shared:testDebugUnitTest` is BUILD SUCCESSFUL, including the 12 new assertions across `MeasurementFormatterTest`, `DateLabelsTest`, and `BadgeStateTest`.
- **Components exist and match spec names:** all §5.3 chrome (`AppTopBar`/`AppLargeTopBar`, `AppTabRow`/`TabbedPager`, `AppBottomNavigation`, `AppScaffold`, `BottomActionBar`, `EditBottomSheetScaffold`, `SortOptionsSheet`, `SettingsSelectionDialog`), §5.4 brand+content (`BrandGradientCard`/`BrandGradientBackground`, `BrandFab` + single `FabSize`, `FloatingControlSurface`/`FloatingIconButton`, `IconStatChip`/`CountBadge`/`StatusLabel`, `InlineBannerCard`, `AvatarWithBadge`, `StatTile`/`StatRow`, `ChartCard`, `WeatherTrendChart`, `WeatherMetric`/`WeatherStatGrid`/`WeatherDailyForecastRow`/`LocationPickerChip`, `AppHeroHeader`/`VersionLabel`/`LabeledIconButton`), and §5.5 formatters/util (`MeasurementFormatter`, `UnitFormatter`, `DateLabels`, `errorToMessage`, `AppNavTransitions`/`slideUpFadeIn`) are present in `commonMain`.
- **Built alongside legacy:** no existing component or screen file was modified or deleted; only new files and additive `strings.xml` entries were added. Plan 11 gates all legacy deletions on grep proof of zero references.
- **Screenshot checkpoint:** the user confirmed the app launches without regression in light + dark on both emulators; per-component visual review is deferred to the consuming screen plans 04–11.
