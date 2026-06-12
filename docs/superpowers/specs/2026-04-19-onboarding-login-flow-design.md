# Onboarding → Login Flow Redesign

**Date:** 2026-04-19
**Branch:** `compose_migration`
**Goal:** Reduce login-screen drop-offs, fix back-button on Home, and make onboarding → login feel like one continuous flow.

## Problem

1. Back button on the Home/Map screen does not close the app. `upPress()` resolves to `navController.navigateUp()`, which is a no-op at the root destination.
2. Analytics show significant drop-off on the login screen. Login is shown *before* onboarding, and the screen offers no context ("why should I sign in?").
3. Login screen blocks the user with:
   - A fullscreen loading state during auth
   - A forced 2.5s success animation before navigating to Home
   - Red error snackbars for user-cancelled sign-ins (which aren't errors)

## Scope (in)

- Onboarding shows **before** login on first launch
- Visual blend between the final onboarding page and the login screen (shared palette/typography, motion continuity)
- Back-button on Home closes the app (keeping double-press-to-exit UX)
- Remove 2.5s success delay
- In-place loading spinner on the sign-in button instead of fullscreen loading
- Inline, quiet error handling (user-cancelled sign-in = no UI)
- Trust copy under the sign-in button

## Scope (out)

- Login remains **required** (Firebase user account). No anonymous mode, no local-only mode, no additional auth methods (email/password, Apple, etc.).
- No onboarding redesign — keeping the current 3 pages, animations, and pager structure.
- No analytics/funnel instrumentation work in this change (flagged in §5 for a future pass).

## Design

### 1. Navigation flow

| Condition | Flow |
|---|---|
| First launch (no user, onboarding not completed) | Splash → Onboarding → Login → Home |
| Returning user, signed in | Splash → Home |
| Returning user, signed out, onboarding already completed | Splash → Login |

- Onboarding gate runs **before** the auth gate on first launch. Today it runs only *after* sign-in inside `FishingNotesApp.kt:36–50`; that composable-level gate is removed.
- Splash stays visible until `userState`, `onboardingCompleted`, and theme are all loaded; the landing destination is decided in one place (app-level routing) based on those three signals.
- `onboardingCompleted` is set to `true` the moment the user taps "Continue" on the last onboarding page, not after successful sign-in. If they abandon sign-in and return later, they land on Login (not Onboarding).
- The existing DataStore key `UserPreferencesImpl.ONBOARDING_COMPLETED_KEY` is user-agnostic (stored under device, not per-user) and is reused as-is. Signing out does not reset it.

### 2. Visual blend

Shared between final onboarding page and Login:
- Background color/gradient (matches current onboarding)
- Typography (family, weights, sizes)
- Primary button style (pill/shape/color)

Transition:
- Final onboarding page's primary CTA label: **"Continue"**
- Tap navigates to Login with a slide-up + fade motion (~250ms)
- `popUpTo(Onboarding) { inclusive = true }` — user cannot swipe back into onboarding from Login

Login layout (from top):
- Small app icon/logo
- Short value-prop headline (e.g., "Keep your catches safe")
- Google sign-in button (single primary CTA)
- Trust copy beneath button: *"We only use your email to sync your catches. No posts, no ads."*

### 3. Back-button fix on Home

**Today:**
- `MapScreen.kt:504–512` → `PlatformBackHandler` → calls `navController.navigateUp()`, else snackbar "Press again to exit" → on second press calls `upPress()` which is also `navController.navigateUp()`.
- At root, `navigateUp()` is a no-op. Back is swallowed. App never closes.

**New:**
- Introduce platform-specific `exitApp()` (expect/actual):
  - `androidMain`: finishes the hosting Activity. Implementation must obtain the Activity reference through an existing pattern in the codebase (e.g., `LocalContext.current` cast to `Activity`, or a ComponentActivity-aware composable-local). The exact mechanism is chosen at implementation time, not specified here.
  - `iosMain`: no-op (iOS has no hardware back button and Apple HIG discourages programmatic app exit).
- On root destinations inside HomeGraph, BackHandler logic becomes:
  1. First press → show "Press again to exit" snackbar, arm a timer (using the existing `TIME_TO_EXIT` constant from `Constants.kt`).
  2. Second press within the window → call `exitApp()`.
  3. Timer elapses → reset state so a subsequent back press re-arms the flow.
- Remove the `navigateUp()` first-try branch on root destinations — it is always a no-op there.
- `upPress()` is still the correct call for non-root screens inside HomeGraph.

### 4. Login screen cleanup

- **Remove the 2500ms delay** in `LoginViewModel.kt:47–50`. On successful sign-in, navigate to Home immediately via the existing `popUpTo(0)` call. The Lottie confetti is dropped; if we decide later that a welcome animation is worth keeping, we add it on the Home screen (out of scope for this change).
- **In-place button spinner** replaces fullscreen loading in `LoginScreen.kt`. The screen layout stays visible; only the Google button content swaps to a spinner while `state == Loading`.
- **Error handling:**
  - User-cancelled (no ID token returned) → do nothing. Return to idle state silently.
  - Transient/recoverable errors (network, Firebase auth) → inline error text under the button, quiet styling, dismissible.
  - Unexpected errors → fall back to snackbar (existing `SnackbarManager` path).
- **Trust copy** under the Google button (single line, muted style).

### 5. Minor improvements flagged for later (out of scope here)

- Funnel analytics: instrument onboarding completion, login impression, sign-in attempt, sign-in success/failure. Needed to actually *measure* whether these changes reduce drop-off.
- Welcome animation on first Home entry (if we want to retain the celebratory feel the 2.5s delay was trying to provide).
- Consider Apple Sign-In on iOS (required by App Store review for apps that offer social sign-in).
- Crashlytics breadcrumbs on the sign-in code path.

## Files to modify

| File | Change |
|---|---|
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt` | Move onboarding gate into top-level routing; add Login destination |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/MainViewModel.kt` | Expose `onboardingCompleted` + `userState` as a combined routing decision |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt` | CTA label → "Continue"; mark `onboardingCompleted` on tap; navigate to Login |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingViewModel.kt` | `completeOnboarding()` called on tap regardless of sign-in outcome |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt` | Blend layout, in-place button spinner, trust copy, inline error text |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LoginViewModel.kt` | Remove 2.5s delay; distinguish cancelled vs real errors |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/AppNavGraph.kt` | Onboarding and Login routes at top level; slide-up transition spec for Onboarding → Login |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/utils/PlatformBackHandler.kt` (+ `androidMain`, `iosMain` actuals) | Add `exitApp()` expect/actual |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapScreen.kt` | Replace root BackHandler logic with double-press-to-exit → `exitApp()` |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/utils/AppStateHolder.kt` | Leave `upPress()` unchanged; it's only used for non-root |
| `androidApp/src/main/java/com/mobileprism/fishing/activity/MainActivity.kt` | Ensure splash stays visible until `onboardingCompleted` is also loaded (if not already) |

## Testing plan

- First-launch smoke: clear app data → launch → Onboarding → tap Continue on last page → Login → sign in → Home.
- Abandoned sign-in: clear app data → Onboarding → Continue → back out of the app → relaunch → Login (no Onboarding replay).
- Signed-out-after-logout: sign in → logout → expect Login (no Onboarding replay).
- Back on Home: single press shows snackbar; second press within window closes the app; waiting past the window and pressing once reshows the snackbar.
- Back on non-root screens inside HomeGraph: returns to Map.
- Sign-in cancelled by user: no error UI, no snackbar, button returns to idle.
- Sign-in network error: inline error text under the button.

## Risks

- The splash screen currently waits for `userState` and theme. Adding `onboardingCompleted` to the wait set must not introduce a race or indefinite hang (DataStore reads should be fast and reliably complete).
- The `exitApp()` actual on Android depends on access to the Activity. Confirm a reliable handle exists in the shared composable layer (there are existing usages — this should be a straight port).
- Removing the fullscreen loading state means the login screen must correctly disable input on the sign-in button while `state == Loading`, to prevent double-taps.
