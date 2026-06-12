---
# First-run & App Shell (Login, Onboarding, Home) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the new design-system tokens and components to the three first-run / shell surfaces — Login, Onboarding, and the Home scaffold + bottom navigation — so the app's entry experience reads as a coherent, accessible, branded whole.

**Architecture:** This is a **consumer** plan: it does not define any new tokens or shared components — it imports and calls the contracts built in Plans 01–03 (Spacing, Motion, BrandGradients, AppButton, GoogleSignInButton, InlineErrorBanner, ErrorState, errorToMessage, SlideUpFadeIn, StaggeredFadeInColumn, AppBottomNavigation, AppScaffold, AppTopBar). Each screen is rewritten to delegate visuals to those components, while fixing the entangled correctness/a11y bugs in scope (raw exception strings on Login, sub-48dp targets, uppercased nav labels, null content descriptions, Lottie placeholder/error). All edits stay in `commonMain` and remain KMP/iOS-safe; the legacy `utils/GoogleButton.kt` is wired out of use here but only **deleted** in Plan 11 after a zero-reference grep.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3, Koin, Compose Resources. All shared UI in commonMain (KMP/iOS-safe).

**Sprint:** S2 Primary Screens · **Plan 04 of 11**

**Depends on:** Plan 01 (foundation tokens: `Spacing`, `Motion`, `BrandGradients`, `Emphasis`), Plan 02 (core primitives & state: `AppButton`/`AppButtonStyle`, `InlineErrorBanner`, `ErrorState`, `errorToMessage`, `SlideUpFadeIn`, `StaggeredFadeInColumn`), Plan 03 (chrome & brand: `AppBottomNavigation`/`AppNavTab`/`AppBottomNavigationDefaults`, `AppScaffold`, `AppTopBar`, `GoogleSignInButton`, `AnimatedResource` placeholder/error enhancement). Blocks Plan 11 (final sweep — owns deletion of `utils/GoogleButton.kt`).

---
---

## Contract reference (owned by Plans 01–03 — DO NOT redefine here)

These names are the API contract this plan calls. Exact signatures live in the spec (§4 tokens, §5 components) and are implemented in Plans 01–03. This plan only consumes them.

| Name | Owning plan | Spec ref | Signature this plan relies on |
|---|---|---|---|
| `Spacing` | 01 | §4.1 | object with `xs=4, sm=8, md=12, lg=16, xl=24, xxl=32, xxxl=48` (Dp) + `screenH=16, sectionGap=24` |
| `Motion` | 01 | §4.4 | `Motion.medium` / `Motion.short` durations + `enterContent` spec; `Motion.navIndicator` spring |
| `BrandGradients` | 01/03 | §4.6, §5.4 | theme-driven `BrandGradients.primaryVertical(): Brush` (a `@Composable` reading colorScheme) |
| `AppButton` | 02 | §5.1 | `AppButton(text, onClick, modifier, style = AppButtonStyle.Filled, enabled = true, loading = false, leadingIcon = null)` |
| `AppButtonStyle` | 02 | §5.1 | enum `{ Filled, Tonal, Outlined, Text }` |
| `GoogleSignInButton` | 03 | §5.4 audit REUSE | `GoogleSignInButton(text, onClick, modifier, loading = false, enabled = true)` — full-width, ≥48dp, icon marked decorative, single merged label/role |
| `InlineErrorBanner` | 02 | §5.2 (`InlineBannerCard`/error) | `InlineErrorBanner(message, modifier, onRetry = null)` — errorContainer styling, alert icon, optional retry |
| `ErrorState` | 02 | §5.2 | `ErrorState(message, illustration, onRetry = null, modifier)` |
| `errorToMessage` | 02 | §5.5 | `@Composable errorToMessage(raw: String?): String` (or a non-composable mapper returning a `StringResource`) — friendly auth/network strings |
| `SlideUpFadeIn` | 02 | §5.5 | `Modifier.slideUpFadeIn(visible: Boolean, delayMillis: Int = 0)` entrance modifier |
| `StaggeredFadeInColumn` | 02 | §5.5 audit REUSE | `StaggeredFadeInColumn(modifier, horizontalAlignment, verticalArrangement, stepDelayMillis = 80, content)` — staggers child entrance |
| `AppBottomNavigation` | 03 | §5.3 | `AppBottomNavigation(items: List<AppNavTab>, currentRoute, onSelect, colors = AppBottomNavigationDefaults.colors())` — M3 NavigationBar base, custom filled active indicator, 48dp targets, `role = Tab`, no uppercase |
| `AppNavTab` | 03 | §5.3 audit REUSE | data contract `{ id, title: StringResource, icon: ImageVector, isSelected: (NavDestination) -> Boolean }` |
| `AppScaffold` | 03 | §5.3 | `AppScaffold(bottomBar = {}, snackbarHost = {}, syncBanner = {}, content)` — owns bottom-bar slot + snackbar host + sync-banner overlay + inset policy |
| `AppTopBar` | 03 | §5.3 | `AppTopBar(title, subtitle = null, navigationIcon = null, actions = {}, scrollBehavior = null)` — surface-colored |

> If any signature above differs when Plans 01–03 land, adjust the call sites here to match the actual signature — never fork or re-implement the component.

---

## File Structure

| File | Action | Responsibility |
|---|---|---|
| `shared/src/commonMain/composeResources/values/strings.xml` | Modify | Add `onboarding_get_started`, `sign_in_cancelled`, `sign_in_generic_error` (used by `errorToMessage` fallback + final-page CTA copy) |
| `shared/src/commonMain/composeResources/values-ru/strings.xml` | Modify | Russian translations of the three new strings |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/AnimatedResource.kt` | Modify | Add `contentDescription` param (default decorative `null`) + placeholder/error fallback so hero Lottie reserves size and degrades gracefully |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt` | Modify | Rewrite to use `BrandGradients`, `Spacing`, `StaggeredFadeInColumn`, `GoogleSignInButton`, `InlineErrorBanner` + `errorToMessage`; delete the screen-local `GoogleSignInButton`; decorative hero |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt` | Modify | Replace inline CTA `Button` with `AppButton`, magic spacers with `Spacing`, stagger via `SlideUpFadeIn`; ≥48dp Skip; decorative hero with description; final CTA copy → `onboarding_get_started` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/Home.kt` | Modify | Replace the ~250-line bespoke `FishingNotesBottomBar` + Jetsnack layout with a thin wrapper that builds `List<AppNavTab>` from `HomeSections` and renders `AppBottomNavigation`; remove `.uppercase()`, null descriptions, sub-48dp targets |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt` | Modify | `FishingNotesMainContent` consumes `AppScaffold` so the sync banner overlays instead of reflowing the NavHost; keep snackbar host |
| `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/HomeSectionsNavTabTest.kt` | Create (Test) | Pure-logic test: `HomeSections.entries` maps to a non-empty `List<AppNavTab>` with the right ids/titles and selection predicates |

---

## Tasks

### Task 1: Add the three new string resources (logic-free, build-safe foundation)

**Files:**
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`
- Modify: `shared/src/commonMain/composeResources/values-ru/strings.xml`

- [ ] **Step 1: Add English strings.** Open `shared/src/commonMain/composeResources/values/strings.xml` and add these three entries inside the `<resources>` element (place them next to the existing `onboarding_continue` / `signin_error` entries):
  ```xml
  <string name="onboarding_get_started">Get Started</string>
  <string name="sign_in_cancelled">Sign-in was cancelled. Please try again.</string>
  <string name="sign_in_generic_error">Couldn\'t sign you in. Please check your connection and try again.</string>
  ```

- [ ] **Step 2: Add Russian strings.** Open `shared/src/commonMain/composeResources/values-ru/strings.xml` and add the matching entries:
  ```xml
  <string name="onboarding_get_started">Начать</string>
  <string name="sign_in_cancelled">Вход отменён. Попробуйте ещё раз.</string>
  <string name="sign_in_generic_error">Не удалось войти. Проверьте подключение и попробуйте снова.</string>
  ```

- [ ] **Step 3: Verify** — regenerate Compose resource accessors and compile the metadata source set:
  ```
  ./gradlew :shared:compileCommonMainKotlinMetadata
  ```
  Expected: BUILD SUCCESSFUL. The generated `Res.string.onboarding_get_started`, `Res.string.sign_in_cancelled`, and `Res.string.sign_in_generic_error` accessors now exist. (If the IDE/codegen needs a kick, `./gradlew :shared:generateComposeResClass` then re-run the compile.)

- [ ] **Step 4: Commit:**
  ```
  git add shared/src/commonMain/composeResources/values/strings.xml shared/src/commonMain/composeResources/values-ru/strings.xml
  git commit -m "Add first-run strings (get started, sign-in cancelled/generic error)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 2: Give `AnimatedResource` an accessibility description + placeholder/error fallback

The hero Lottie currently hardcodes `contentDescription = "Lottie animation"` (read literally by TalkBack/VoiceOver) and renders nothing while the JSON decodes or if `Res.readBytes` throws on a missing/corrupt file. Make the description a caller-provided param defaulting to decorative `null`, reserve layout size with a placeholder, and swallow read failures gracefully.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/AnimatedResource.kt`

- [ ] **Step 1: Replace the whole file** with this version (adds `contentDescription` param, a try/catch around the byte read, and a reserved-size empty `Box` placeholder so the layout never jumps):
  ```kotlin
  package com.mobileprism.fishing.ui.utils

  import androidx.compose.foundation.Image
  import androidx.compose.foundation.layout.Box
  import androidx.compose.runtime.Composable
  import androidx.compose.runtime.LaunchedEffect
  import androidx.compose.runtime.getValue
  import androidx.compose.runtime.mutableStateOf
  import androidx.compose.runtime.remember
  import androidx.compose.runtime.setValue
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.layout.ContentScale
  import fishing.shared.generated.resources.Res
  import io.github.alexzhirkevich.compottie.LottieCompositionSpec
  import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
  import io.github.alexzhirkevich.compottie.rememberLottieComposition
  import io.github.alexzhirkevich.compottie.rememberLottiePainter
  import org.jetbrains.compose.resources.ExperimentalResourceApi

  @OptIn(ExperimentalResourceApi::class)
  @Composable
  fun AnimatedResource(
      resName: String,
      modifier: Modifier = Modifier,
      contentDescription: String? = null,
      iterations: Int = Int.MAX_VALUE,
      contentScale: ContentScale = ContentScale.Fit,
  ) {
      var jsonString by remember(resName) { mutableStateOf<String?>(null) }
      var failed by remember(resName) { mutableStateOf(false) }
      LaunchedEffect(resName) {
          runCatching { Res.readBytes("files/$resName.json").decodeToString() }
              .onSuccess { jsonString = it }
              .onFailure { failed = true }
      }
      val json = jsonString
      if (json != null && !failed) {
          val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
          val progress by animateLottieCompositionAsState(composition, iterations = iterations)
          Image(
              modifier = modifier,
              contentScale = contentScale,
              painter = rememberLottiePainter(
                  composition = composition,
                  progress = { progress },
              ),
              contentDescription = contentDescription,
          )
      } else {
          Box(modifier = modifier)
      }
  }
  ```
  Notes: the reserved-size `Box(modifier)` keeps layout stable during load and on failure (callers pass a sized `modifier`). `contentDescription` defaults to `null` so existing decorative call sites stay decorative; Login/Onboarding will pass either `null` (decorative) explicitly or a localized string.

- [ ] **Step 2: Verify** — compile the Android variant of the shared module:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. No call site breaks: the new `contentDescription` param is optional and inserted before the existing optional params, and every existing caller uses named or positional-then-named args (`resName`, `modifier`). If positional-arg call sites exist that now resolve to `contentDescription`, grep `AnimatedResource(` and switch them to named `modifier =`.

- [ ] **Step 3: Verify iOS source set still compiles:**
  ```
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  ```
  Expected: BUILD SUCCESSFUL (no Android-only API introduced).

- [ ] **Step 4: Commit:**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/utils/AnimatedResource.kt
  git commit -m "AnimatedResource: a11y description param + placeholder/error fallback

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 3: Rewrite `LoginScreen` onto the design system

Adopt `BrandGradients`, `Spacing`, `StaggeredFadeInColumn` (entrance motion), `GoogleSignInButton` (full-width ≥48dp CTA), and `InlineErrorBanner` + `errorToMessage` (friendly localized error with retry). Delete the screen-local `GoogleSignInButton` (lines 145–175) and stop showing the raw exception string. Mark the hero Lottie decorative.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt`

- [ ] **Step 1: Replace the whole file** with this version. Note: `signing` drives `GoogleSignInButton(loading = ...)`; the error path renders `InlineErrorBanner` (errorContainer styling + alert icon + Retry) wrapped in `AnimatedVisibility`; the raw `error.message` is mapped through `errorToMessage` so technical Firebase strings never reach users. The container gradient comes from `BrandGradients.primaryVertical()` and vertical rhythm comes from `StaggeredFadeInColumn` + `Spacing`.
  ```kotlin
  package com.mobileprism.fishing.ui

  import androidx.compose.animation.AnimatedVisibility
  import androidx.compose.foundation.background
  import androidx.compose.foundation.layout.Arrangement
  import androidx.compose.foundation.layout.Box
  import androidx.compose.foundation.layout.WindowInsets
  import androidx.compose.foundation.layout.fillMaxSize
  import androidx.compose.foundation.layout.fillMaxWidth
  import androidx.compose.foundation.layout.padding
  import androidx.compose.foundation.layout.size
  import androidx.compose.foundation.layout.systemBarsPadding
  import androidx.compose.material3.MaterialTheme
  import androidx.compose.material3.Scaffold
  import androidx.compose.material3.Text
  import androidx.compose.runtime.Composable
  import androidx.compose.runtime.collectAsState
  import androidx.compose.runtime.getValue
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.style.TextAlign
  import com.mmk.kmpauth.google.GoogleButtonUiContainer
  import com.mobileprism.fishing.ui.components.InlineErrorBanner
  import com.mobileprism.fishing.ui.motion.StaggeredFadeInColumn
  import com.mobileprism.fishing.ui.theme.BrandGradients
  import com.mobileprism.fishing.ui.theme.Spacing
  import com.mobileprism.fishing.ui.utils.AnimatedResource
  import com.mobileprism.fishing.ui.utils.errorToMessage
  import com.mobileprism.fishing.ui.views.GoogleSignInButton
  import com.mobileprism.fishing.ui.viewmodels.LoginUiState
  import com.mobileprism.fishing.ui.viewmodels.LoginViewModel
  import fishing.shared.generated.resources.Res
  import fishing.shared.generated.resources.login_headline
  import fishing.shared.generated.resources.login_subtitle
  import fishing.shared.generated.resources.login_trust_copy
  import fishing.shared.generated.resources.sign_with_google
  import fishing.shared.generated.resources.signing_in
  import org.jetbrains.compose.resources.stringResource
  import org.koin.compose.koinInject

  @Composable
  fun LoginScreen() {
      val loginViewModel: LoginViewModel = koinInject()
      val uiState by loginViewModel.uiState.collectAsState()
      val signing = uiState is LoginUiState.Signing
      val rawError = (uiState as? LoginUiState.Error)?.message
      val errorMessage = rawError?.let { errorToMessage(it) }

      Scaffold(contentWindowInsets = WindowInsets(0)) { paddingValues ->
          Box(
              modifier = Modifier
                  .fillMaxSize()
                  .background(BrandGradients.primaryVertical())
                  .padding(paddingValues)
                  .systemBarsPadding(),
          ) {
              StaggeredFadeInColumn(
                  modifier = Modifier
                      .fillMaxSize()
                      .padding(horizontal = Spacing.xxl),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center,
              ) {
                  AnimatedResource(
                      resName = "walking_fish",
                      contentDescription = null,
                      modifier = Modifier.size(180.dp),
                  )

                  Text(
                      text = stringResource(Res.string.login_headline),
                      style = MaterialTheme.typography.headlineSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(top = Spacing.sm),
                  )

                  Text(
                      text = stringResource(Res.string.login_subtitle),
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(top = Spacing.md),
                  )

                  GoogleButtonUiContainer(
                      modifier = Modifier
                          .fillMaxWidth()
                          .padding(top = Spacing.xl),
                      onGoogleSignInResult = { googleUser ->
                          val idToken = googleUser?.idToken
                          if (idToken != null) {
                              loginViewModel.firebaseSignInWithGoogle(idToken)
                          } else {
                              loginViewModel.onGoogleSignInCancelled()
                          }
                      },
                  ) {
                      GoogleSignInButton(
                          text = if (signing) {
                              stringResource(Res.string.signing_in)
                          } else {
                              stringResource(Res.string.sign_with_google)
                          },
                          loading = signing,
                          enabled = !signing,
                          onClick = { this@GoogleButtonUiContainer.onClick() },
                          modifier = Modifier.fillMaxWidth(),
                      )
                  }

                  Text(
                      text = stringResource(Res.string.login_trust_copy),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(top = Spacing.lg),
                  )

                  AnimatedVisibility(visible = errorMessage != null) {
                      InlineErrorBanner(
                          message = errorMessage.orEmpty(),
                          onRetry = { loginViewModel.onGoogleSignInCancelled() },
                          modifier = Modifier
                              .fillMaxWidth()
                              .padding(top = Spacing.lg),
                      )
                  }
              }
          }
      }
  }
  ```
  Notes:
  - The local private `GoogleSignInButton` and its now-unused imports (`Card`, `CardDefaults`, `CircularProgressIndicator`, `Image`, `Row`, `Brush`, `RoundedCornerShape`, `painterResource`, `ic_google_logo`, `alpha`, `height`, `Spacer`, `Column`) are gone — they are replaced by the shared component and `StaggeredFadeInColumn`.
  - `import androidx.compose.ui.unit.dp` is still needed for `size(180.dp)`. Keep it.
  - `GoogleSignInButton` lives in `com.mobileprism.fishing.ui.views` per Plan 03 (§5 "new components live in `ui/home/views/` and `ui/components/`"). If Plan 03 placed it under `ui.components`, change the import to `com.mobileprism.fishing.ui.components.GoogleSignInButton`. Resolve against the actual Plan 03 location — do not create a second copy.
  - The click guard `if (!signing)` is folded into `enabled = !signing` on the button, so the visual no longer looks pressable while signing.

- [ ] **Step 2: Verify** — compile the Android variant:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. If the build fails on an unresolved import, confirm the exact package/signature of `BrandGradients.primaryVertical`, `StaggeredFadeInColumn`, `GoogleSignInButton`, `InlineErrorBanner`, or `errorToMessage` against the Plan 01–03 implementations and fix only the import/argument names here.

- [ ] **Step 3: Verify iOS source set:**
  ```
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  ```
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit:**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt
  git commit -m "Login: adopt design system (brand gradient, GoogleSignInButton, inline error, staggered entrance)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 4: Adopt `AppButton`, `Spacing`, and entrance motion in `OnboardingScreen`

Replace the inline `Button` CTA with `AppButton` (no forced casing, ≥48dp baked in), swap the magic-number Spacers/paddings for `Spacing` tokens, give the Skip control a ≥48dp target, mark the hero Lottie decorative (or per-page localized) via the new `contentDescription`, and change the final-page label to `onboarding_get_started`. Keep the existing per-page gradient + page-indicator behavior (those are owned/cleaned in later phases per the spec); this task only lands the shell-level component/token + a11y + casing fixes.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt`

- [ ] **Step 1: Update the final-page CTA copy.** In the `pages` list (third `OnboardingPageData`, around lines 78–84) change:
  ```kotlin
          buttonTextRes = Res.string.onboarding_continue,
  ```
  to:
  ```kotlin
          buttonTextRes = Res.string.onboarding_get_started,
  ```

- [ ] **Step 2: Add the design-system imports.** Add these imports to the import block (after the existing `com.mobileprism.fishing.ui.utils.AnimatedResource` import):
  ```kotlin
  import com.mobileprism.fishing.ui.theme.Spacing
  import com.mobileprism.fishing.ui.views.AppButton
  import com.mobileprism.fishing.ui.views.AppButtonStyle
  ```
  (Use `com.mobileprism.fishing.ui.components.*` instead if Plan 02 placed `AppButton`/`AppButtonStyle` there — match the actual location.)

- [ ] **Step 3: Replace the inline CTA `Button` with `AppButton`.** In `OnboardingTextContent` (lines 361–377) replace the entire `Button(...) { Text(...) }` block:
  ```kotlin
          Button(
              onClick = onButtonClick,
              modifier = Modifier
                  .fillMaxWidth()
                  .height(if (compact) 52.dp else 56.dp),
              shape = RoundedCornerShape(28.dp),
              colors = ButtonDefaults.buttonColors(
                  containerColor = Color.White,
                  contentColor = data.gradientColors.last(),
              ),
          ) {
              Text(
                  text = stringResource(data.buttonTextRes),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
              )
          }
  ```
  with:
  ```kotlin
          AppButton(
              text = stringResource(data.buttonTextRes),
              onClick = onButtonClick,
              style = AppButtonStyle.Filled,
              modifier = Modifier.fillMaxWidth(),
          )
  ```
  Notes: `AppButton` enforces ≥48dp height and applies no `.uppercase()`. It uses theme colors; the per-page white-on-gradient color is intentionally dropped in favor of the system button — this is the consistency win the spec calls for (Login + Onboarding share one CTA). The `Button`, `ButtonDefaults`, `RoundedCornerShape` (if unused elsewhere), and `Color`-for-button imports become unused; remove only the ones no longer referenced (the `Color` import is still used by `gradientColors` / `Color.White` text/dots, so keep it).

- [ ] **Step 4: Tokenize the CTA-area spacing.** Still in `OnboardingTextContent`, replace the literal spacers around the CTA so the rhythm comes from `Spacing`:
  - The leading `Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))` (line 336) → `Spacer(modifier = Modifier.height(if (compact) Spacing.sm else Spacing.md))`
  - The `Spacer(modifier = Modifier.height(if (!compact) ...))` at line 317 (`Spacer(modifier = Modifier.height(16.dp))`) → `Spacer(modifier = Modifier.height(Spacing.lg))`
  - The `Spacer(modifier = Modifier.height(24.dp))` in the compact branch (line 356) → `Spacer(modifier = Modifier.height(Spacing.xl))`
  - The trailing `Spacer(modifier = Modifier.height(32.dp))` (line 380) → `Spacer(modifier = Modifier.height(Spacing.xxl))`

- [ ] **Step 5: Mark the hero Lottie decorative.** In `OnboardingAnimation` (lines 289–298) pass an explicit decorative description through to `AnimatedResource`:
  ```kotlin
  @Composable
  private fun OnboardingAnimation(
      resName: String,
      modifier: Modifier = Modifier,
  ) {
      AnimatedResource(
          resName = resName,
          contentDescription = null,
          modifier = modifier,
      )
  }
  ```

- [ ] **Step 6: Give the Skip button a ≥48dp target and full-opacity color.** Replace the Skip `TextButton` block (lines 125–131) with:
  ```kotlin
                  TextButton(
                      onClick = onFinished,
                      modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                  ) {
                      Text(
                          text = stringResource(Res.string.onboarding_skip),
                          color = Color.White,
                          style = MaterialTheme.typography.labelLarge,
                      )
                  }
  ```
  Add the import `import androidx.compose.foundation.layout.defaultMinSize` to the layout import group. (Full-opacity white plus the 48dp min target addresses the low-contrast / tiny-tap-target finding; the per-gradient scrim refinement is deferred to a later visual pass.)

- [ ] **Step 7: Verify** — compile the Android variant:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. Remove any now-unused imports the compiler flags (e.g. `ButtonDefaults`, `Button` if no longer referenced). Keep `RoundedCornerShape` only if still used by the page indicator (`CircleShape` is used there, so `RoundedCornerShape` is likely now removable).

- [ ] **Step 8: Verify iOS source set:**
  ```
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  ```
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit:**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt
  git commit -m "Onboarding: AppButton + Spacing tokens, 48dp skip, decorative hero, Get Started CTA

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 5: Test the `HomeSections` → `AppNavTab` mapping (pure logic, TDD)

Before swapping the bottom bar internals, lock the data mapping with a unit test in the existing `commonTest` source set (uses `kotlin.test`, which the module already declares). This guards the Task 6 rewrite: the four sections must produce four tabs with correct ids/titles and working selection predicates, and the labels must NOT be uppercased.

The mapping under test is a small pure helper `homeNavTabs()` added in Task 6 (`shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/Home.kt`). Write the test first; it will fail to compile until Task 6 adds the helper — that is the intended red state.

**Files:**
- Create (Test): `shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/HomeSectionsNavTabTest.kt`

- [ ] **Step 1: Write the failing test.** Create the file:
  ```kotlin
  package com.mobileprism.fishing.ui.home

  import com.mobileprism.fishing.ui.HomeTabs
  import com.mobileprism.fishing.ui.MainDestinations
  import kotlin.test.Test
  import kotlin.test.assertEquals
  import kotlin.test.assertTrue

  class HomeSectionsNavTabTest {

      @Test
      fun homeNavTabs_hasOneTabPerSection_inOrder() {
          val tabs = homeNavTabs()
          assertEquals(HomeSections.entries.size, tabs.size)
          assertEquals(
              HomeSections.entries.map { it.name },
              tabs.map { it.id },
          )
      }

      @Test
      fun homeNavTabs_titlesMatchSectionTitleResources() {
          val tabs = homeNavTabs()
          HomeSections.entries.forEachIndexed { index, section ->
              assertEquals(section.title, tabs[index].title)
              assertEquals(section.icon, tabs[index].icon)
          }
      }

      @Test
      fun homeNavTabs_selectionPredicate_matchesSectionRoute() {
          val tabs = homeNavTabs()
          val mapTab = tabs.first { it.id == HomeSections.MAP.name }
          val mapDestination = fakeDestinationFor { it.hasRoute<MainDestinations.Map>() }
          assertTrue(mapTab.isSelected(mapDestination))
      }
  }
  ```
  Note: `fakeDestinationFor` is a tiny test seam. If constructing a real `NavDestination` is impractical in `commonTest`, assert instead that each tab's `isSelected` is referentially the same lambda the section exposes:
  ```kotlin
      @Test
      fun homeNavTabs_selectionPredicate_isSectionPredicate() {
          val tabs = homeNavTabs()
          HomeSections.entries.forEachIndexed { index, section ->
              assertEquals(section.hasRoute, tabs[index].isSelected)
          }
      }
  ```
  Use the second form (`assertEquals(section.hasRoute, tabs[index].isSelected)`) and delete the first selection test + the `fakeDestinationFor` reference if `NavDestination` cannot be instantiated in `commonTest`. Keep exactly one selection test.

- [ ] **Step 2: Confirm red.** Run the test task (this is the unit-test task this module exposes):
  ```
  ./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.home.HomeSectionsNavTabTest"
  ```
  Expected: COMPILATION FAILURE (unresolved reference `homeNavTabs`). This confirms the test targets code that does not exist yet. Do NOT commit yet — proceed to Task 6, which makes it green.

---

### Task 6: Replace the bespoke bottom bar with `AppBottomNavigation`

Delete the ~250-line Jetsnack-derived `FishingNotesBottomBar` + `FishingNotesBottomNavLayout` + item/indicator/measure machinery and the private file-level vals. Replace with: (a) the `HomeSections` enum kept as the tab descriptor source, (b) a pure `homeNavTabs()` mapper to `List<AppNavTab>` (the function the Task 5 test pins), and (c) a thin `FishingNotesBottomBar` wrapper that renders the shared `AppBottomNavigation`. This drops `.uppercase()`, gives every item 48dp targets + `role = Tab` semantics + per-item content descriptions, and a filled M3 active indicator — all inherited from the shared component.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/Home.kt`

- [ ] **Step 1: Replace the entire file** `Home.kt` with this version. It keeps the `HomeSections` enum (the spec's "formalize HomeSections as the data contract"), adds the `homeNavTabs()` pure mapper, and reduces `FishingNotesBottomBar` to a delegating wrapper. All the bespoke `Layout`/`Animatable`/`Placeable`/`MeasureScope` code and the private nav vals are removed.
  ```kotlin
  package com.mobileprism.fishing.ui.home

  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.graphics.vector.ImageVector
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.outlined.Map
  import androidx.compose.material.icons.outlined.Menu
  import androidx.compose.material.icons.outlined.Person
  import androidx.compose.material.icons.outlined.WbSunny
  import androidx.navigation.NavDestination
  import androidx.navigation.NavDestination.Companion.hasRoute
  import com.mobileprism.fishing.ui.HomeTabs
  import com.mobileprism.fishing.ui.MainDestinations
  import com.mobileprism.fishing.ui.views.AppBottomNavigation
  import com.mobileprism.fishing.ui.views.AppNavTab
  import fishing.shared.generated.resources.Res
  import fishing.shared.generated.resources.map
  import fishing.shared.generated.resources.notes
  import fishing.shared.generated.resources.profile
  import fishing.shared.generated.resources.weather
  import org.jetbrains.compose.resources.StringResource

  enum class HomeSections(
      val title: StringResource,
      val icon: ImageVector,
      val hasRoute: (NavDestination) -> Boolean,
  ) {
      MAP(Res.string.map, Icons.Outlined.Map, { it.hasRoute<MainDestinations.Map>() }),
      NOTES(Res.string.notes, Icons.Outlined.Menu, { it.hasRoute<HomeTabs.NotesTab>() }),
      WEATHER(Res.string.weather, Icons.Outlined.WbSunny, { it.hasRoute<HomeTabs.WeatherTab>() }),
      PROFILE(Res.string.profile, Icons.Outlined.Person, { it.hasRoute<HomeTabs.ProfileTab>() }),
  }

  fun homeNavTabs(): List<AppNavTab> =
      HomeSections.entries.map { section ->
          AppNavTab(
              id = section.name,
              title = section.title,
              icon = section.icon,
              isSelected = section.hasRoute,
          )
      }

  @Composable
  fun FishingNotesBottomBar(
      modifier: Modifier,
      tabs: Array<HomeSections>,
      currentSection: HomeSections,
      navigateToRoute: (HomeSections) -> Unit,
  ) {
      val navTabs = homeNavTabs().filter { tab -> tabs.any { it.name == tab.id } }
      AppBottomNavigation(
          items = navTabs,
          currentRoute = currentSection.name,
          onSelect = { tab ->
              HomeSections.entries
                  .firstOrNull { it.name == tab.id }
                  ?.let(navigateToRoute)
          },
          modifier = modifier,
      )
  }
  ```
  Notes:
  - `AppBottomNavigation` / `AppNavTab` resolve from Plan 03's location (`com.mobileprism.fishing.ui.views` here; switch to `...ui.components` if Plan 03 placed them there). `AppNavTab.id` is a `String` route key; we use the section `name` so the existing `FishingNotesApp` call site (which passes `HomeSections` + `currentSection`) keeps working with no changes to the caller's signature.
  - `currentRoute` is matched against `AppNavTab.id` inside `AppBottomNavigation` per its Plan 03 contract. If Plan 03's `AppBottomNavigation` instead consumes a live `NavDestination` and uses each tab's `isSelected` predicate, drop `currentRoute = currentSection.name` and pass the destination it expects — match the real Plan 03 signature; do not re-add bespoke layout code.
  - `.uppercase()`, the null `contentDescription`s, the sub-48dp `selectable` Box, the hairline-border indicator, and the inline `SpringSpec` are all gone — they now live (correctly) inside the shared `AppBottomNavigation` (filled active indicator, `Motion.navIndicator` spring, `role = Tab`, per-item description from `title`).

- [ ] **Step 2: Make the Task 5 test green.** Run:
  ```
  ./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.ui.home.HomeSectionsNavTabTest"
  ```
  Expected: BUILD SUCCESSFUL, 3 tests pass. If the `isSelected` equality test was kept, it passes because `homeNavTabs()` reuses `section.hasRoute` by reference.

- [ ] **Step 3: Verify** the Android variant compiles (catches any caller mismatch in `FishingNotesApp`):
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. `FishingNotesApp.kt` still calls `FishingNotesBottomBar(modifier, tabs, currentSection, navigateToRoute)` — that signature is unchanged, so no caller edit is required by this task (the scaffold change is Task 7).

- [ ] **Step 4: Verify iOS source set:**
  ```
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  ```
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit** (tests + implementation together now that it's green):
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/Home.kt shared/src/commonTest/kotlin/com/mobileprism/fishing/ui/home/HomeSectionsNavTabTest.kt
  git commit -m "Home: replace bespoke bottom bar with AppBottomNavigation (48dp, role=Tab, no uppercase)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 7: Route the Home content through `AppScaffold` (sync banner overlays, not reflows)

Today `FishingNotesMainContent` stacks `SyncStatusIndicator` as the first child of a `Column` above the `NavHost`, so every sync animation pushes all screens down. Move to the shared `AppScaffold`, which owns the bottom-bar slot, snackbar host, and a sync-banner overlay slot + centralized inset policy — so the banner overlays instead of reflowing content.

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt`

- [ ] **Step 1: Replace `FishingNotesMainContent`** (lines 66–106) with the `AppScaffold` version. Keep the `FishingNotesApp` routing composable (lines 34–64) unchanged.
  ```kotlin
  @Composable
  private fun FishingNotesMainContent() {
      val appStateHolder = rememberAppStateHolder()
      val mainViewModel: MainViewModel = koinViewModel()
      val syncState by mainViewModel.syncState.collectAsState()

      AppScaffold(
          bottomBar = {
              if (appStateHolder.shouldShowBottomBar) {
                  val currentSection = appStateHolder.currentSection() ?: HomeSections.MAP
                  FishingNotesBottomBar(
                      modifier = Modifier,
                      tabs = appStateHolder.bottomBarTabs,
                      currentSection = currentSection,
                      navigateToRoute = appStateHolder::navigateToBottomBarRoute,
                  )
              }
          },
          snackbarHost = {
              SnackbarHost(
                  hostState = appStateHolder.snackbarHostState,
                  snackbar = { snackbarData -> AppSnackbar(snackbarData) },
              )
          },
          syncBanner = { SyncStatusIndicator(syncState = syncState) },
      ) { contentModifier ->
          NavHost(
              modifier = contentModifier,
              navController = appStateHolder.navController,
              startDestination = HomeGraph,
          ) {
              AppNavGraph(
                  navController = appStateHolder.navController,
                  upPress = appStateHolder::upPress,
              )
          }
      }
  }
  ```
  Notes:
  - The content lambda receives a `contentModifier` from `AppScaffold` that already carries the correct content insets (bottom-bar height + window insets), so the per-screen `Column { SyncStatusIndicator(); NavHost() }` and the manual `Modifier.padding(innerPaddingModifier)` are removed. The sync banner now overlays via the `syncBanner` slot rather than occupying a row above the NavHost.
  - The snackbar host no longer applies its own `Modifier.systemBarsPadding()` — `AppScaffold` owns inset policy per the spec (§5.3). If Plan 03's `AppScaffold.snackbarHost` expects the bare `SnackbarHost` (it does), this is correct; otherwise pass the modifier the actual signature documents.
  - `AppScaffold`'s signature is `AppScaffold(bottomBar, snackbarHost, syncBanner, content: @Composable (Modifier) -> Unit)`. If Plan 03 instead exposes `content: @Composable (PaddingValues) -> Unit`, apply `Modifier.padding(it)` to the `NavHost` accordingly — match the real signature.

- [ ] **Step 2: Fix imports.** In the import block: add `import com.mobileprism.fishing.ui.AppScaffold` (or the actual package Plan 03 uses, e.g. `com.mobileprism.fishing.ui.views.AppScaffold`); remove the now-unused `import androidx.compose.foundation.layout.Column`, `import androidx.compose.foundation.layout.padding`, `import androidx.compose.foundation.layout.systemBarsPadding`, `import androidx.compose.foundation.layout.WindowInsets`, and `import androidx.compose.material3.Scaffold` only if nothing else in the file references them (the `FishingNotesApp` routing composable does not). Keep `SnackbarHost`, `Modifier`, `NavHost`, `collectAsState`, `getValue`, `koinViewModel`, `koinInject`.

- [ ] **Step 3: Verify** the Android variant compiles:
  ```
  ./gradlew :shared:compileDebugKotlinAndroid
  ```
  Expected: BUILD SUCCESSFUL. Resolve any `AppScaffold` signature mismatch against the actual Plan 03 implementation (content modifier vs padding values; presence of a `syncBanner` slot) — adjust only the call here.

- [ ] **Step 4: Verify iOS source set:**
  ```
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  ```
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit:**
  ```
  git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt
  git commit -m "Home shell: route content through AppScaffold so sync banner overlays instead of reflowing

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

### Task 8: Full build, install, and screenshot checkpoint (Login + Onboarding + Home shell)

**Files:** none (verification only)

- [ ] **Step 1: Run the unit tests** for the shared module to confirm nothing regressed:
  ```
  ./gradlew :shared:testDebugUnitTest
  ```
  Expected: BUILD SUCCESSFUL, including `HomeSectionsNavTabTest`.

- [ ] **Step 2: Assemble + install the debug app** on both running emulators:
  ```
  ./gradlew :androidApp:installDebug
  ```
  Expected: BUILD SUCCESSFUL, `Installed on 2 devices` (installed id `com.merkost.fishingnotes.debug`).

- [ ] **Step 3: Screenshot checkpoint (USER action).** The user launches the app on `emulator-5554` and `emulator-5556` and captures, in **light and dark** themes:
  - **Onboarding** — all three pages; verify the CTA is the `AppButton` (no uppercase, ≥48dp), the Skip control is tappable/legible, page indicator unchanged, final page reads "Get Started".
  - **Login** — verify the full-width `GoogleSignInButton` is the dominant CTA, staggered entrance plays, trust copy present; force an error path (airplane mode + tap) and confirm the `InlineErrorBanner` shows a friendly localized message with a Retry affordance (not a raw Firebase string).
  - **Home shell** — verify the bottom nav shows title-case labels (Map/Notes/Weather/Profile), a filled active indicator, and that switching tabs animates; trigger a sync state and confirm the sync banner **overlays** the top of content without pushing the whole screen down.

- [ ] **Step 4: Review & iterate.** Compare against the audit findings for Login / Onboarding / Home scaffold. Fix any visual regressions by adjusting call sites only (never by re-implementing shared components). Re-run Step 2 and re-screenshot until approved.

- [ ] **Step 5: Mark progress.** Update `docs/superpowers/plans/00-PROGRESS.md`: set Login, Onboarding, and "Home scaffold / bottom nav" rows to ✅ and Plan 04 impl status to ✅ once screenshots are approved. Commit:
  ```
  git add docs/superpowers/plans/00-PROGRESS.md
  git commit -m "Mark Plan 04 (first-run & shell) screens verified

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
  ```

---

## Verification & success criteria

This plan is complete when **all** of the following hold:

1. **Builds + installs:** `./gradlew :shared:testDebugUnitTest`, `./gradlew :shared:compileDebugKotlinAndroid`, and `./gradlew :androidApp:installDebug` all succeed; the app installs as `com.merkost.fishingnotes.debug` on both emulators.
2. **iOS source set still compiles:** `./gradlew :shared:compileKotlinIosSimulatorArm64` succeeds — no Android-only API entered the changed `commonMain` files (all edits use M3 / Compose Resources / the KMP-safe shared components).
3. **Screenshots reviewed & approved** (light + dark) for Login, all three Onboarding pages (including the error path on Login), and the Home shell / bottom nav, per Task 8.
4. **Design-system adoption landed:**
   - Login uses `BrandGradients`, `Spacing`, `StaggeredFadeInColumn`, `GoogleSignInButton`, and `InlineErrorBanner` + `errorToMessage`; the screen-local `GoogleSignInButton` is deleted and raw exception strings no longer reach users.
   - Onboarding's CTA is `AppButton` (no `.uppercase()`, ≥48dp), spacers use `Spacing`, the final CTA says "Get Started", the hero is decorative, and Skip has a ≥48dp target.
   - Home's bottom bar is `AppBottomNavigation` driven by `homeNavTabs()`; the ~250-line bespoke layout is gone; labels are title-case with `role = Tab` semantics, per-item content descriptions, 48dp targets, and a filled active indicator.
   - The Home content routes through `AppScaffold`, so the sync banner overlays rather than reflowing the NavHost.
5. **Tests:** `HomeSectionsNavTabTest` passes, pinning the `HomeSections → AppNavTab` mapping.
6. **No premature deletion:** `utils/GoogleButton.kt` is left in the tree (no longer referenced by Login) and is deleted only in **Plan 11** after a grep proves zero references — do not delete it here.
