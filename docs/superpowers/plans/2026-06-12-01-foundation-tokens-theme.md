---
# Foundation Tokens & Theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish the shared `commonMain` design-token foundation (Spacing, Elevation, Motion, Emphasis, Shape, Color, Type) and real Nunito font weights so every later plan consumes one canonical, KMP/iOS-safe token vocabulary with zero visual regression.

**Architecture:** Add five plain Kotlin token objects under `ui/theme/` (pure `Dp`/`Color`/`AnimationSpec` values — no Android APIs, no CompositionLocal needed), extend `Shape.kt` with `extraSmall` and move the bottom-sheet shape into a `ShapeTokens` helper, privatise the raw palette in `Color.kt` while adding explicit `error*` roles + a theme-driven `BrandGradients`, replace the four single-`nunito.ttf` faux-weight `Font(...)` calls with four real OFL weight files fetched from Google Fonts, and fill the typography scale with tuned `lineHeight`/`letterSpacing`. Behaviour stays identical: token values reproduce the literals they replace, and legacy `val`s that other plans still reference remain until Plan 11 gates their deletion on a zero-reference grep.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3, Koin, Compose Resources. All shared UI in commonMain (KMP/iOS-safe).

**Sprint:** S1 · Design System · **Plan 01 of 11**

**Depends on:** none (this is the foundational plan; Plans 02–11 depend on it)

---
---

## File Structure

| File | Action | Single responsibility |
| --- | --- | --- |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Spacing.kt` | Create | 4dp-grid spacing scale + semantic aliases (`Spacing` object) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Elevation.kt` | Create | M3 tonal elevation ladder + semantic aliases (`Elevation` object) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Motion.kt` | Create | Named durations/easings + `AnimationSpec` presets (`Motion` object) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Emphasis.kt` | Create | Alpha/emphasis constants (`Emphasis` object) |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Shape.kt` | Modify | Add `extraSmall=2dp`; introduce `ShapeTokens.bottomSheet` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Color.kt` | Modify | Privatise raw palette; add `error*` roles per scheme; add `BrandGradients` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/CustomColors.kt` | Modify | Delete dead `backgroundSecondaryColor` field; keep `secondaryTextColor`/`secondaryIconColor` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Type.kt` | Modify | Map four real Nunito weight files 1:1; add `lineHeight`/`letterSpacing`; fill display/headline tiers |
| `shared/src/commonMain/composeResources/font/nunito_regular.ttf` | Create | Nunito Regular (400) OFL weight file |
| `shared/src/commonMain/composeResources/font/nunito_medium.ttf` | Create | Nunito Medium (500) OFL weight file |
| `shared/src/commonMain/composeResources/font/nunito_semibold.ttf` | Create | Nunito SemiBold (600) OFL weight file |
| `shared/src/commonMain/composeResources/font/nunito_bold.ttf` | Create | Nunito Bold (700) OFL weight file |
| `shared/src/commonMain/composeResources/font/nunito.ttf` | Delete (Task 9, gated) | Old single faux-weight file, removed once no accessor references it |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/theme/TokensTest.kt` | Create | kotlin.test assertions on Spacing/Elevation/Emphasis/Shape/BrandGradients values |

### Scope boundaries (honour these — they belong to other plans)
- **`CustomColors.secondaryTextColor` / `secondaryIconColor` consumer migration** (the ~16 `MaterialTheme.customColors.*` / `LocalColors.current.*` call sites in `Text.kt`, `Buttons.kt`, `DefaultViews.kt`, `Counters.kt`, `TextFields.kt`, `NotesViews.kt`, `WeatherScreen.kt`, `AboutApp.kt`, `NewCatchSections.kt`) is owned by the **component plans (02/03)** that consolidate `Text.kt`/`Buttons.kt` into `AppText`/`AppButton`. This plan only fixes the `CustomColors` data class (removing the dead field) and leaves both surviving fields wired so those files keep compiling.
- **Screen-local hardcoded gradient/FAB hex** (`OnboardingScreen.kt`, `MapScreen.kt`, `FirstSpotPromptCard.kt`) is consumed via `BrandGradients` by the **brand-component plan (03)** and the **screen plans (C1/Map)**; this plan only *defines* `BrandGradients`, it does not rewrite those screens.
- **`SettingsScreen.kt` color-picker swatch literals** (`Color(0xFFEF5350)` … the theme-swatch palette) are intentional non-theme colors and stay; they are owned by the **Settings plan (07)** which introduces `SelectableColorSwatch`.
- **`Constants.modalBottomSheetCorners` call-site rewiring** to `ShapeTokens.bottomSheet` is performed by whichever screen plan touches each bottom sheet; this plan defines `ShapeTokens.bottomSheet` and keeps `Constants.modalBottomSheetCorners` as a deprecated alias so nothing breaks mid-migration.
- **Deletion of legacy `val`s** (`backgroundGreenColor`, `surfaceGreenColor`, `cardColor`, `RedGoogleChrome`, `surfaceGrayColor`, `primaryBlueColorTransparent`, etc.) is gated by **Plan 11** on a zero-reference grep. This plan privatises only the palette constants that already have **zero** external references; it leaves externally-referenced legacy `val`s `public` (annotated `@Deprecated`) so the tree keeps compiling.

---

## Tasks

### Task 1: Spacing tokens

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Spacing.kt`

- [ ] **Step 1: Create `Spacing.kt`** with the full token object (values are pure `Dp`, KMP-safe):

```kotlin
package com.mobileprism.fishing.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Spacing {
    val none: Dp = 0.dp
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp

    val screenH: Dp = 16.dp
    val sectionGap: Dp = 24.dp
    val listItemGap: Dp = 8.dp
    val cardPadding: Dp = 16.dp
    val fabClearance: Dp = 88.dp
}
```

- [ ] **Step 2: Verify (compile)** — `./gradlew :shared:compileCommonMainKotlinMetadata` → BUILD SUCCESSFUL (no Android-only references; confirms iOS-safety of the new file).
- [ ] **Step 3: Commit** — `git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Spacing.kt && git commit -m "Add Spacing design tokens (commonMain)"`

---

### Task 2: Elevation tokens

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Elevation.kt`

- [ ] **Step 1: Create `Elevation.kt`** (matches spec §4.2 ladder + semantic aliases):

```kotlin
package com.mobileprism.fishing.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Elevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp

    val card: Dp = level1
    val raisedCard: Dp = level2
    val dialog: Dp = level3
    val bottomSheet: Dp = level4
    val fab: Dp = level3
}
```

- [ ] **Step 2: Verify (compile)** — `./gradlew :shared:compileCommonMainKotlinMetadata` → BUILD SUCCESSFUL.
- [ ] **Step 3: Commit** — `git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Elevation.kt && git commit -m "Add Elevation design tokens (commonMain)"`

---

### Task 3: Emphasis tokens

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Emphasis.kt`

- [ ] **Step 1: Create `Emphasis.kt`** (spec §4.5 — plain `Float` alphas, KMP-safe):

```kotlin
package com.mobileprism.fishing.ui.theme

object Emphasis {
    const val disabled: Float = 0.38f
    const val medium: Float = 0.60f
    const val hint: Float = 0.74f
    const val divider: Float = 0.12f
    const val pressedOverlay: Float = 0.08f
    const val scrim: Float = 0.32f
}
```

- [ ] **Step 2: Verify (compile)** — `./gradlew :shared:compileCommonMainKotlinMetadata` → BUILD SUCCESSFUL.
- [ ] **Step 3: Commit** — `git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Emphasis.kt && git commit -m "Add Emphasis/alpha design tokens (commonMain)"`

---

### Task 4: Motion tokens

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Motion.kt`

- [ ] **Step 1: Create `Motion.kt`** (spec §4.4 — durations/easings + named `AnimationSpec` presets; `androidx.compose.animation.core.*` is part of CMP `compose.foundation`/`compose.animation` and is iOS-safe):

```kotlin
package com.mobileprism.fishing.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

object Motion {
    const val short: Int = 150
    const val medium: Int = 250
    const val long: Int = 400

    val standardEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val emphasizedEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val accelerateEasing: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    fun <T> screenEnter(): FiniteAnimationSpec<T> =
        tween(durationMillis = medium, easing = emphasizedEasing)

    fun <T> screenExit(): FiniteAnimationSpec<T> =
        tween(durationMillis = short, easing = accelerateEasing)

    fun <T> enterContent(): FiniteAnimationSpec<T> =
        tween(durationMillis = medium, easing = standardEasing)

    fun navIndicatorOffset(): FiniteAnimationSpec<IntOffset> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

    fun navIndicatorDp(): FiniteAnimationSpec<Dp> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

    fun navIndicatorFloat(): FiniteAnimationSpec<Float> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
}
```

- [ ] **Step 2: Verify (compile)** — `./gradlew :shared:compileCommonMainKotlinMetadata` → BUILD SUCCESSFUL (confirms no Android-only animation API leaked in).
- [ ] **Step 3: Commit** — `git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Motion.kt && git commit -m "Add Motion design tokens (commonMain)"`

---

### Task 5: Shapes — add `extraSmall` + `ShapeTokens.bottomSheet`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Shape.kt:1-12`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/utils/Constants.kt:14-16`

Current `Shape.kt` defines only `small/medium/large/extraLarge` (4/8/16/24). Spec §4.3 canonical mapping wants chips/fields→`small(8)`, cards→`medium(12)`, sheets→`large(16)`, dialogs→`extraLarge(24)`, plus a new `extraSmall(2)`. The radii therefore change from 4/8/16/24 to 2/8/12/16/24.

> Behaviour note: this nudges card radius 8→12 and large 16 stays; this is the **only** intentional visual change in this plan and is explicitly sanctioned by spec §4.3. Flag it at the screenshot checkpoint.

- [ ] **Step 1: Replace `Shape.kt` body** with the canonical 5-tier scale plus a `ShapeTokens` helper for the non-symmetric bottom-sheet shape (moved out of `Constants.kt` per spec §4.3):

```kotlin
package com.mobileprism.fishing.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object ShapeTokens {
    val bottomSheet: Shape = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
    )
}
```

- [ ] **Step 2: Re-point `Constants.modalBottomSheetCorners` at the new helper** so the value has one source of truth while call sites migrate later. Edit `Constants.kt:14-16` from:

```kotlin
    val modalBottomSheetCorners = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
    )
```

to:

```kotlin
    @Deprecated(
        "Use ShapeTokens.bottomSheet",
        ReplaceWith("ShapeTokens.bottomSheet", "com.mobileprism.fishing.ui.theme.ShapeTokens")
    )
    val modalBottomSheetCorners: Shape = ShapeTokens.bottomSheet
```

- [ ] **Step 3: Fix imports in `Constants.kt`** — replace the now-unused `RoundedCornerShape` import with the `Shape` + `ShapeTokens` imports. Change the import block at `Constants.kt:3-5` from:

```kotlin
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
```

to:

```kotlin
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.ShapeTokens
```

- [ ] **Step 4: Verify (compile)** — `./gradlew :shared:compileCommonMainKotlinMetadata` → BUILD SUCCESSFUL.
- [ ] **Step 5: Commit** — `git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Shape.kt shared/src/commonMain/kotlin/com/mobileprism/fishing/utils/Constants.kt && git commit -m "Add extraSmall shape + ShapeTokens.bottomSheet; canonical radius scale"`

---

### Task 6: Color cleanup — privatise dead palette, add error roles + BrandGradients, delete dead field

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Color.kt`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/CustomColors.kt`

Grounding (verified counts, references outside `ui/theme/`):
- **Zero external references** → safe to make `private` now: `primaryWhiteColor`, `primaryFigmaDarkColor`, `primaryFigmaLightColor`, `surfaceGreenColor`, `secondaryWhiteColor`, `RedGoogleChrome`, `backgroundGreenColor`, `surfaceGrayColor`, `backgroundSecondaryColor` (field), and all the `*Transparent` aliases.
- **Still referenced externally** (leave `public`, mark `@Deprecated`, Plan 11 deletes): `primaryFigmaColor` (1 file), `secondaryFigmaColor` (1 file), `primaryBlueColor` (1 file), `cardColor` (1 file: `NotesViews.kt:116`), `secondaryTextColor` (via `CustomColors`), `secondaryIconColor` (via `CustomColors`).

- [ ] **Step 1: Add `error*` roles to all four schemes.** M3 `lightColorScheme`/`darkColorScheme` already supply default error colors, but spec §4.6 wants them explicit and brand-consistent. Add the four error params to each scheme builder. In `Color.kt`, for `BlueLightColorScheme` (currently ends at line 184 with `inversePrimary = BlueLightInversePrimary,`) add before the closing `)`:

```kotlin
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
```

For `BlueDarkColorScheme` add:

```kotlin
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
```

For `GreenLightColorScheme` add the same light error block:

```kotlin
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
```

For `GreenDarkColorScheme` add the same dark error block:

```kotlin
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
```

- [ ] **Step 2: Privatise the zero-reference raw palette constants** in `Color.kt:7-41`. Change each of these from `val` to `private val` (verified to have no external references): `RedGoogleChrome`, `primaryFigmaLightColor`, `primaryFigmaDarkColor`, `secondaryFigmaLightColor`, `secondaryFigmaDarkColor`, `primaryFigmaTextColor`, `supportFigmaTextColor`, `surfaceGrayColor`, `primaryFigmaBackgroundTint`, `backgroundWhiteColor`, `backgroundGreenColor`, `surfaceGreenColor`, `primaryBlueColorTransparent`, `primaryBlueLightColor`, `primaryBlueLightColorTransparent`, `primaryBlueDarkColor`, `primaryBlueDarkColorTransparent`, `secondaryBlueColor`, `secondaryBlueLightColor`, `secondaryBlueLightColorTransparent`, `secondaryBlueDarkColor`, `primaryWhiteColor`, `secondaryWhiteColor`. Concretely, prefix each listed declaration line with `private `. Example — change line 7:

```kotlin
val RedGoogleChrome = Color(0xFFde5246)
```

to:

```kotlin
private val RedGoogleChrome = Color(0xFFde5246)
```

(Apply the identical `private ` prefix to every constant in the list above. Do **not** privatise `primaryFigmaColor`, `secondaryFigmaColor`, `primaryBlueColor`, `cardColor`, or `secondaryTextColor` — they still have external callers.)

- [ ] **Step 3: Mark the externally-referenced legacy `val`s `@Deprecated`** so Plan 11 can grep them to zero. Add the annotation directly above each of these four declarations in `Color.kt`:

```kotlin
@Deprecated("Use MaterialTheme.colorScheme.primary", ReplaceWith("MaterialTheme.colorScheme.primary"))
val primaryFigmaColor = Color(0xFF43a047)
```
```kotlin
@Deprecated("Use MaterialTheme.colorScheme.secondary", ReplaceWith("MaterialTheme.colorScheme.secondary"))
val secondaryFigmaColor = Color(0xFFff6d00)
```
```kotlin
@Deprecated("Use MaterialTheme.colorScheme.primary", ReplaceWith("MaterialTheme.colorScheme.primary"))
val primaryBlueColor = Color(0xFF2196f3)
```
```kotlin
@Deprecated("Use MaterialTheme.colorScheme.surfaceContainer", ReplaceWith("MaterialTheme.colorScheme.surfaceContainer"))
val cardColor = Color(0xFF8FA590)
```

- [ ] **Step 4: Add `BrandGradients`** (spec §4.6/§5.4 — theme-driven primary gradient replacing the 3 hand-rolled hex gradients in OnboardingScreen/MapScreen/FirstSpotPromptCard, which Plan 03 consumes). Append to the end of `Color.kt`, after `val InitColorScheme = BlueLightColorScheme` (line 273):

```kotlin
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Brush

object BrandGradients {
    fun primaryVertical(scheme: ColorScheme): Brush = Brush.verticalGradient(
        listOf(scheme.primary, scheme.tertiary)
    )

    fun primaryDiagonal(scheme: ColorScheme): Brush = Brush.linearGradient(
        listOf(scheme.primary, scheme.tertiary)
    )

    fun surfaceVertical(scheme: ColorScheme): Brush = Brush.verticalGradient(
        listOf(scheme.surface, scheme.surfaceContainerHighest)
    )
}
```

> The two `import` lines must be moved to the top import block of `Color.kt` (after the existing `import androidx.compose.ui.graphics.Color` at line 5), not left inline. Add `import androidx.compose.material3.ColorScheme` and `import androidx.compose.ui.graphics.Brush` there, and keep only the `object BrandGradients { ... }` block at the bottom.

- [ ] **Step 5: Delete the dead `backgroundSecondaryColor` field** from `CustomColors.kt` (spec §4.6 — verified zero external references). Rewrite `CustomColors.kt` to drop the third field everywhere:

```kotlin
package com.mobileprism.fishing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val secondaryTextColor: Color,
    val secondaryIconColor: Color,
)

fun darkCustomColors(
    secondaryTextColor: Color = Color(0xFFB0B0B8),
    secondaryIconColor: Color = Color(0xFF9E9EA6),
): CustomColors = CustomColors(
    secondaryTextColor,
    secondaryIconColor,
)

fun lightCustomColors(
    secondaryTextColor: Color = secondaryFigmaTextColor,
    secondaryIconColor: Color = Color(0xFF6E6E76),
): CustomColors = CustomColors(
    secondaryTextColor,
    secondaryIconColor,
)

val LocalColors = compositionLocalOf { lightCustomColors() }

val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current
```

> `lightCustomColors` references `secondaryFigmaTextColor`, which is currently `public` in `Color.kt:17` and stays referenced here, so it must **not** be privatised in Step 2. (It was already excluded from the Step 2 list.)

- [ ] **Step 6: Verify no theme-actual passes the removed field.** Grep for `backgroundSecondaryColor` across the repo and confirm the only producers were the two factory functions just edited:

```bash
grep -rnE "\bbackgroundSecondaryColor\b" /Users/merkost/AndroidStudioProjects/FishingNotes/shared/src --include='*.kt'
```
Expected output: no matches. If any `lightCustomColors(... backgroundSecondaryColor = ...)` named-argument call site appears (e.g. in `Theme.kt` actuals under `androidMain`/`iosMain`), remove that argument there in the same commit.

- [ ] **Step 7: Verify (compile, both targets)** — run both to prove commonMain + iOS-safety:
  - `./gradlew :shared:compileCommonMainKotlinMetadata` → BUILD SUCCESSFUL
  - `./gradlew :shared:compileKotlinIosSimulatorArm64` → BUILD SUCCESSFUL
- [ ] **Step 8: Commit** — `git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Color.kt shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/CustomColors.kt && git commit -m "Color cleanup: privatise dead palette, add error roles + BrandGradients, drop dead field"`

---

### Task 7: Fetch real Nunito weight files + wire Compose Resources

**Files:**
- Create: `shared/src/commonMain/composeResources/font/nunito_regular.ttf`
- Create: `shared/src/commonMain/composeResources/font/nunito_medium.ttf`
- Create: `shared/src/commonMain/composeResources/font/nunito_semibold.ttf`
- Create: `shared/src/commonMain/composeResources/font/nunito_bold.ttf`

Spec §4.7: the current `Type.kt` maps a single `Res.font.nunito` (one `nunito.ttf`, a variable/static fallback) to all four weights, producing synthetic faux-bold. We need four real static OFL weight files, one per `FontWeight`. Compose Resources generates one accessor per file in the `font/` dir (`Res.font.<filename_without_ext>`), so the four files yield `Res.font.nunito_regular`, `nunito_medium`, `nunito_semibold`, `nunito_bold`.

Nunito is licensed under the SIL Open Font License (OFL) and is redistributable. The Google Fonts GitHub mirror serves the static instances directly.

- [ ] **Step 1: Download the four static Nunito weight files** from the Google Fonts OFL repo into the font dir. (Network access required; if the runner is offline, hand this single command to the user and continue once the four files exist.)

```bash
cd /Users/merkost/AndroidStudioProjects/FishingNotes/shared/src/commonMain/composeResources/font
BASE="https://raw.githubusercontent.com/google/fonts/main/ofl/nunito"
curl -fSL "$BASE/Nunito%5Bwght%5D.ttf" -o nunito_variable.ttf
echo "Variable font fetched; deriving static instances below"
```

> The Google Fonts repo ships Nunito as a single variable font (`Nunito[wght].ttf`). To get true static weight files we instance it. Use `fonttools` (Python) which is available via the runner's `pip`:

```bash
cd /Users/merkost/AndroidStudioProjects/FishingNotes/shared/src/commonMain/composeResources/font
python3 -m pip install --quiet fonttools
python3 - <<'PY'
from fontTools import varLib
from fontTools.varLib.instancer import instantiateVariableFont
from fontTools.ttLib import TTFont
weights = {"nunito_regular.ttf": 400, "nunito_medium.ttf": 500,
           "nunito_semibold.ttf": 600, "nunito_bold.ttf": 700}
for out, w in weights.items():
    f = TTFont("nunito_variable.ttf")
    instantiateVariableFont(f, {"wght": w}, inplace=True)
    f.save(out)
    print("wrote", out, "@wght", w)
PY
rm -f nunito_variable.ttf
ls -la nunito_*.ttf
```

Expected: four files `nunito_regular.ttf`, `nunito_medium.ttf`, `nunito_semibold.ttf`, `nunito_bold.ttf` present, each non-zero size.

> **If `fonttools`/network is unavailable on the runner:** this is the one step that needs the user. Ask the user to drop the four static Nunito weight files (Regular/Medium/SemiBold/Bold) into `shared/src/commonMain/composeResources/font/` with exactly those four names, then resume at Step 2. Do not substitute a different font.

- [ ] **Step 2: Verify the OFL license is retained.** Nunito's OFL requires the license to travel with the fonts. Confirm the project already carries it or add it:

```bash
ls /Users/merkost/AndroidStudioProjects/FishingNotes/shared/src/commonMain/composeResources/font/ | grep -i "OFL\|LICENSE" || echo "ADD OFL.txt"
```
If missing, fetch it: `curl -fSL "https://raw.githubusercontent.com/google/fonts/main/ofl/nunito/OFL.txt" -o /Users/merkost/AndroidStudioProjects/FishingNotes/shared/src/commonMain/composeResources/font/OFL.txt`

- [ ] **Step 3: Regenerate Compose Resources accessors** so the four new `Res.font.*` symbols exist:

```bash
./gradlew :shared:generateComposeResClass
```
Then confirm the accessors were generated:

```bash
grep -rn "nunito_regular\|nunito_medium\|nunito_semibold\|nunito_bold" /Users/merkost/AndroidStudioProjects/FishingNotes/shared/build/generated/ 2>/dev/null | head
```
Expected: matches for all four accessor names. (`nunito` may still also appear until Task 9 removes the old file.)

- [ ] **Step 4: Commit** — `git add shared/src/commonMain/composeResources/font/nunito_regular.ttf shared/src/commonMain/composeResources/font/nunito_medium.ttf shared/src/commonMain/composeResources/font/nunito_semibold.ttf shared/src/commonMain/composeResources/font/nunito_bold.ttf shared/src/commonMain/composeResources/font/OFL.txt && git commit -m "Add real Nunito 400/500/600/700 weight files (OFL)"`

---

### Task 8: Typography — map weights 1:1, add lineHeight/letterSpacing, fill scale

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Type.kt`

Spec §4.7: map each weight to its own resource (no faux-bold), add tuned `lineHeight`/`letterSpacing`, and fill the missing `displayLarge/Medium`, `headlineLarge/Medium` tiers.

- [ ] **Step 1: Replace `Type.kt` entirely** with the per-weight family and the filled scale:

```kotlin
package com.mobileprism.fishing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.nunito_bold
import fishing.shared.generated.resources.nunito_medium
import fishing.shared.generated.resources.nunito_regular
import fishing.shared.generated.resources.nunito_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun NunitoFontFamily() = FontFamily(
    Font(Res.font.nunito_regular, FontWeight.Normal),
    Font(Res.font.nunito_medium, FontWeight.Medium),
    Font(Res.font.nunito_semibold, FontWeight.SemiBold),
    Font(Res.font.nunito_bold, FontWeight.Bold),
)

@Composable
fun AppTypography(): Typography {
    val nunito = NunitoFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = nunito,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
    )
}
```

- [ ] **Step 2: Verify (compile, both targets)**:
  - `./gradlew :shared:compileCommonMainKotlinMetadata` → BUILD SUCCESSFUL (resolves the four `Res.font.nunito_*` accessors)
  - `./gradlew :shared:compileKotlinIosSimulatorArm64` → BUILD SUCCESSFUL
- [ ] **Step 3: Commit** — `git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/Type.kt && git commit -m "Type.kt: per-weight Nunito mapping, tuned lineHeight/letterSpacing, full scale"`

---

### Task 9: Remove the old single-weight font file (gated)

**Files:**
- Delete: `shared/src/commonMain/composeResources/font/nunito.ttf`

- [ ] **Step 1: Prove no code references the old accessor** — must return zero before deleting:

```bash
grep -rnE "Res\.font\.nunito\b" /Users/merkost/AndroidStudioProjects/FishingNotes/shared/src --include='*.kt'
```
Expected: no matches (Task 8 swapped all four references to `nunito_regular/medium/semibold/bold`). If anything prints, stop and fix that reference first.

- [ ] **Step 2: Delete the file and regenerate accessors**:

```bash
git rm shared/src/commonMain/composeResources/font/nunito.ttf
./gradlew :shared:generateComposeResClass
```

- [ ] **Step 3: Verify (compile)** — `./gradlew :shared:compileCommonMainKotlinMetadata` → BUILD SUCCESSFUL (confirms nothing still binds `Res.font.nunito`).
- [ ] **Step 4: Commit** — `git add -A shared/src/commonMain/composeResources/font && git commit -m "Remove obsolete single-weight nunito.ttf"`

---

### Task 10: Unit tests for pure token values

**Files:**
- Create: `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/theme/TokensTest.kt`
- Test: `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/theme/TokensTest.kt`

The repo has an established `kotlin.test` `commonTest` source set (e.g. `utils/UtilFunctionsTest.kt`); there is **no** screenshot library (paparazzi/roborazzi absent from `libs.versions.toml`), so composables are verified by compile + the user's screenshot checkpoint, and only the pure `Dp`/`Color`/`Brush`/`Shape` token values get real assertions here.

- [ ] **Step 1: Create `TokensTest.kt`** asserting the load-bearing token values (catches accidental future drift):

```kotlin
package com.mobileprism.fishing.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class TokensTest {

    @Test
    fun spacingGridValuesAreOnFourDpGrid() {
        assertEquals(0.dp, Spacing.none)
        assertEquals(2.dp, Spacing.xxs)
        assertEquals(4.dp, Spacing.xs)
        assertEquals(8.dp, Spacing.sm)
        assertEquals(12.dp, Spacing.md)
        assertEquals(16.dp, Spacing.lg)
        assertEquals(24.dp, Spacing.xl)
        assertEquals(32.dp, Spacing.xxl)
        assertEquals(48.dp, Spacing.xxxl)
    }

    @Test
    fun spacingSemanticAliasesMatchSpec() {
        assertEquals(16.dp, Spacing.screenH)
        assertEquals(24.dp, Spacing.sectionGap)
        assertEquals(8.dp, Spacing.listItemGap)
        assertEquals(16.dp, Spacing.cardPadding)
        assertEquals(88.dp, Spacing.fabClearance)
    }

    @Test
    fun elevationLadderAndAliasesMatchSpec() {
        assertEquals(0.dp, Elevation.level0)
        assertEquals(1.dp, Elevation.level1)
        assertEquals(3.dp, Elevation.level2)
        assertEquals(6.dp, Elevation.level3)
        assertEquals(8.dp, Elevation.level4)
        assertEquals(12.dp, Elevation.level5)
        assertEquals(Elevation.level1, Elevation.card)
        assertEquals(Elevation.level2, Elevation.raisedCard)
        assertEquals(Elevation.level3, Elevation.dialog)
        assertEquals(Elevation.level4, Elevation.bottomSheet)
        assertEquals(Elevation.level3, Elevation.fab)
    }

    @Test
    fun emphasisAlphasMatchSpec() {
        assertEquals(0.38f, Emphasis.disabled)
        assertEquals(0.60f, Emphasis.medium)
        assertEquals(0.74f, Emphasis.hint)
        assertEquals(0.12f, Emphasis.divider)
        assertEquals(0.08f, Emphasis.pressedOverlay)
        assertEquals(0.32f, Emphasis.scrim)
    }

    @Test
    fun motionDurationsMatchSpec() {
        assertEquals(150, Motion.short)
        assertEquals(250, Motion.medium)
        assertEquals(400, Motion.long)
    }

    @Test
    fun shapeScaleIsCanonical() {
        assertEquals(RoundedCornerShape(2.dp), Shapes.extraSmall)
        assertEquals(RoundedCornerShape(8.dp), Shapes.small)
        assertEquals(RoundedCornerShape(12.dp), Shapes.medium)
        assertEquals(RoundedCornerShape(16.dp), Shapes.large)
        assertEquals(RoundedCornerShape(24.dp), Shapes.extraLarge)
    }

    @Test
    fun bottomSheetShapeIsTopRoundedOnly() {
        val expected = RoundedCornerShape(
            topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
        )
        assertEquals(expected, ShapeTokens.bottomSheet)
    }

    @Test
    fun errorRolesArePopulatedOnEveryScheme() {
        listOf(
            BlueLightColorScheme,
            BlueDarkColorScheme,
            GreenLightColorScheme,
            GreenDarkColorScheme,
        ).forEach { scheme ->
            assertEquals(false, scheme.error.value == 0UL, "error must be set")
            assertEquals(false, scheme.onError.value == 0UL, "onError must be set")
            assertEquals(false, scheme.errorContainer.value == 0UL, "errorContainer must be set")
            assertEquals(false, scheme.onErrorContainer.value == 0UL, "onErrorContainer must be set")
        }
    }

    @Test
    fun brandGradientsAreThemeDriven() {
        val light = BrandGradients.primaryVertical(BlueLightColorScheme)
        val dark = BrandGradients.primaryVertical(BlueDarkColorScheme)
        assertEquals(false, light == dark, "gradient must follow the color scheme")
    }
}
```

- [ ] **Step 2: Run the tests** — `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.theme.TokensTest"` → BUILD SUCCESSFUL, all assertions pass. (`testDebugUnitTest` is the confirmed unit-test task; `commonTest` runs under it for the Android target.)
- [ ] **Step 3: Commit** — `git add shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/theme/TokensTest.kt && git commit -m "Test token values: Spacing/Elevation/Emphasis/Motion/Shape/error roles/BrandGradients"`

---

### Task 11: Full build, install, and screenshot checkpoint

**Files:** none (verification only)

- [ ] **Step 1: Full Android compile** — `./gradlew :shared:compileDebugKotlinAndroid` → BUILD SUCCESSFUL.
- [ ] **Step 2: iOS source-set compile** — `./gradlew :shared:compileKotlinIosSimulatorArm64` → BUILD SUCCESSFUL (proves no Android-only API entered the new/changed `commonMain` files).
- [ ] **Step 3: Install on the running emulators** — `./gradlew :androidApp:installDebug` → installed id `com.merkost.fishingnotes.debug` on `emulator-5554` and `emulator-5556`.
- [ ] **Step 4: Screenshot checkpoint (USER ACTION).** The user launches the app and captures, in **light + dark**: the Login/Onboarding hero (Nunito weight/scale change), the Notes list cards (radius 8→12 from the canonical shape scale), a bottom sheet (top corners via `ShapeTokens.bottomSheet`), and any screen with body text (line-height/letter-spacing). Review against this plan: the **only** expected visual deltas are (a) crisper real font weights replacing faux-bold, (b) card corner radius 8→12, (c) slightly looser typographic rhythm. Anything else is a regression to investigate before proceeding to Plan 02.

---

## Verification & success criteria

- **Builds + installs:** `./gradlew :shared:compileDebugKotlinAndroid`, `./gradlew :androidApp:installDebug` both succeed; app launches on both emulators.
- **iOS source set still compiles:** `./gradlew :shared:compileKotlinIosSimulatorArm64` succeeds — confirming every new/changed `commonMain` file (Spacing, Elevation, Motion, Emphasis, Shape, Color, CustomColors, Type) is KMP/iOS-safe with no Android-only APIs.
- **Token unit tests pass:** `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.theme.TokensTest"` is green, locking the Spacing/Elevation/Emphasis/Motion/Shape values, the populated `error*` roles on all four schemes, and theme-driven `BrandGradients`.
- **Tokens exist and are consumable by later plans:** `Spacing`, `Elevation`, `Motion`, `Emphasis`, `ShapeTokens`, `BrandGradients` objects are present in `ui/theme/`; `Shapes` exposes `extraSmall`; `AppTypography()` uses four real Nunito weight files with a full display→label scale.
- **No regressions, one sanctioned visual delta:** behaviour is identical except the spec-approved card radius bump (8→12) and the real-vs-faux Nunito weights; both reviewed and approved at the Task 11 screenshot checkpoint (light + dark).
- **Cleanup staged for Plan 11:** dead `backgroundSecondaryColor` field deleted; zero-reference raw palette constants are `private`; externally-referenced legacy `val`s (`primaryFigmaColor`, `secondaryFigmaColor`, `primaryBlueColor`, `cardColor`) are `@Deprecated` so Plan 11's zero-reference grep can finalise their removal; old `nunito.ttf` removed once unreferenced.
