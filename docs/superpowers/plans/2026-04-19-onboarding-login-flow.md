# Onboarding → Login Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Re-route first-launch to Onboarding → Login → Home, fix root back-button to close the app, remove login-screen friction (forced delay, fullscreen loader, noisy errors), and visually blend onboarding with login.

**Architecture:** Routing is lifted out of per-composable state into `FishingNotesApp.kt`, which reads `userState` and `onboardingCompleted` together and dispatches to one of Splash / Onboarding / Login / Home. A platform `exitApp()` expect/actual closes the Activity on Android; iOS no-ops. MapScreen's root BackHandler uses `exitApp()` instead of the no-op `navigateUp()`.

**Tech Stack:** Kotlin Multiplatform (shared/), Compose Multiplatform (androidx.compose + JetBrains Nav), Firebase Auth (gitlive), Koin DI, kmpauth.google. Spec reference: `docs/superpowers/specs/2026-04-19-onboarding-login-flow-design.md`.

**Branch:** `compose_migration` (no worktree — executing in place per user preference).

**Note on TDD:** This plan prescribes ViewModel tests for routing decisions and LoginViewModel changes because those are testable. Pure composable edits (OnboardingScreen layout, LoginScreen visual blend) are verified via manual smoke tests documented at the end of each task — Compose UI testing isn't wired up in this module, and wiring it is explicitly out of scope.

---

## File Structure

| File | Role | Create/Modify |
|---|---|---|
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/utils/ExitApp.kt` | `expect fun exitApp()` | Create |
| `shared/src/androidMain/kotlin/com/mobileprism/fishing/utils/ExitApp.android.kt` | Android actual — finishes Activity | Create |
| `shared/src/iosMain/kotlin/com/mobileprism/fishing/utils/ExitApp.ios.kt` | iOS actual — no-op | Create |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapScreen.kt` | Root BackHandler calls `exitApp()` | Modify |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/MainViewModel.kt` | Expose combined routing state | Modify |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/OnboardingViewModel.kt` | No change (reuse `completeOnboarding()`) | — |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt` | Top-level routing: Splash/Onboarding/Login/Home | Modify |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt` | CTA label → "Continue"; completion triggers nav to Login | Modify |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt` | Blend layout, in-place spinner, trust copy, inline error | Modify |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LoginViewModel.kt` | Remove 2.5s delay; classify errors | Modify |
| `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/AppNavGraph.kt` | LoginRoute removed from this graph (now top-level) | Modify |
| `shared/src/androidUnitTest/.../LoginViewModelTest.kt` | New tests for success + cancelled + error paths | Create |
| `shared/src/androidUnitTest/.../MainViewModelRoutingTest.kt` | New tests for routing decision | Create |

---

## Task 1 — Platform `exitApp()`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/utils/ExitApp.kt`
- Create: `shared/src/androidMain/kotlin/com/mobileprism/fishing/utils/ExitApp.android.kt`
- Create: `shared/src/iosMain/kotlin/com/mobileprism/fishing/utils/ExitApp.ios.kt`

- [ ] **Step 1: Create the expect declaration**

`shared/src/commonMain/kotlin/com/mobileprism/fishing/utils/ExitApp.kt`:
```kotlin
package com.mobileprism.fishing.utils

import androidx.compose.runtime.Composable

@Composable
expect fun rememberExitApp(): () -> Unit
```

(A composable-returning function lets the Android actual capture the Activity via `LocalActivity` safely and return a lambda the call site invokes. iOS returns a lambda that does nothing.)

- [ ] **Step 2: Create the Android actual**

`shared/src/androidMain/kotlin/com/mobileprism/fishing/utils/ExitApp.android.kt`:
```kotlin
package com.mobileprism.fishing.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberExitApp(): () -> Unit {
    val context = LocalContext.current
    return {
        (context as? Activity)?.finish()
    }
}
```

- [ ] **Step 3: Create the iOS actual**

`shared/src/iosMain/kotlin/com/mobileprism/fishing/utils/ExitApp.ios.kt`:
```kotlin
package com.mobileprism.fishing.utils

import androidx.compose.runtime.Composable

@Composable
actual fun rememberExitApp(): () -> Unit {
    return { /* Apple HIG: apps do not exit programmatically */ }
}
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/utils/ExitApp.kt shared/src/androidMain/kotlin/com/mobileprism/fishing/utils/ExitApp.android.kt shared/src/iosMain/kotlin/com/mobileprism/fishing/utils/ExitApp.ios.kt
git commit -m "feat: add rememberExitApp expect/actual for platform app exit"
```

---

## Task 2 — Root back-button closes the app

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapScreen.kt:504-512`

- [ ] **Step 1: Read current BackHandler code**

Run: `Read shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapScreen.kt offset=495 limit=30`
Note the exact block you'll replace.

- [ ] **Step 2: Replace the root BackHandler**

Locate the block around line 504:
```kotlin
PlatformBackHandler {
    if (navController.previousBackStackEntry != null) {
        navController.navigateUp()
    } else {
        val currentMillis = // ...
        if (currentMillis - lastPressed < Constants.TIME_TO_EXIT) {
            upPress()
        } else {
            // show snackbar
        }
    }
}
```

Replace with:
```kotlin
val exitApp = rememberExitApp()
PlatformBackHandler {
    val currentMillis = Clock.System.now().toEpochMilliseconds()
    if (currentMillis - lastPressed < Constants.TIME_TO_EXIT) {
        exitApp()
    } else {
        lastPressed = currentMillis
        snackbarManager.showInfo(stringResource(Res.string.press_back_again_to_exit))
    }
}
```

Add import: `import com.mobileprism.fishing.utils.rememberExitApp`.

Keep exact existing imports, snackbar manager, and `lastPressed` state — only the branching logic changes. The `navigateUp()` attempt is removed from the root handler because Map is the start destination and navigateUp is always a no-op there.

- [ ] **Step 3: Build and smoke-test**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL.

Manual smoke: Install on device, open app, sign in if needed, land on Map. Press back once → snackbar appears. Press back again within 2 seconds → app closes (returns to launcher). Wait > 2s, press back once → snackbar reappears; waiting times out resets.

- [ ] **Step 4: Commit**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/map/MapScreen.kt
git commit -m "fix: back button on root closes the app via rememberExitApp"
```

---

## Task 3 — MainViewModel exposes combined routing state

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/MainViewModel.kt`
- Create: `shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/MainViewModelRoutingTest.kt`

- [ ] **Step 1: Read the current MainViewModel**

Run: `Read shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/MainViewModel.kt`
Identify existing `userState` flow and how theme/preferences are read. Reuse that plumbing.

- [ ] **Step 2: Add a `RoutingDecision` sealed type in the same file**

At the top of `MainViewModel.kt` (still inside the file, above the class):
```kotlin
sealed interface RoutingDecision {
    data object Splash : RoutingDecision
    data object Onboarding : RoutingDecision
    data object Login : RoutingDecision
    data object Home : RoutingDecision
}
```

- [ ] **Step 3: Expose a `routing: StateFlow<RoutingDecision>` on MainViewModel**

Add inside the MainViewModel class:
```kotlin
val routing: StateFlow<RoutingDecision> = combine(
    userState,                       // existing: ContentState<User?> or similar
    onboardingPreferences.hasCompletedOnboarding, // existing source used by OnboardingViewModel
) { userSt, onboardingDone ->
    when {
        userSt.isLoading || onboardingDone == null -> RoutingDecision.Splash
        onboardingDone == false -> RoutingDecision.Onboarding
        userSt.hasData && userSt.data != null -> RoutingDecision.Home
        else -> RoutingDecision.Login
    }
}.stateIn(viewModelScope, SharingStarted.Eagerly, RoutingDecision.Splash)
```

Adjust `isLoading` / `hasData` / `data` to match the actual user-state API already in the file. If `onboardingPreferences` is not already injected, inject it (same binding OnboardingViewModel uses). Reuse — do not duplicate flows.

- [ ] **Step 4: Write the failing routing test**

`shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/MainViewModelRoutingTest.kt`:
```kotlin
package com.mobileprism.fishing.viewmodels

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MainViewModelRoutingTest {

    // Replace types below with the real types in the project:
    // - UserStateSource (what userState flows from) → ContentState<User?> flow
    // - OnboardingPreferences (what hasCompletedOnboarding is on)
    // Look up exact types before writing the test body. This skeleton shows intent.

    @Test
    fun `routing is Splash while user state is loading`() = runTest(StandardTestDispatcher()) {
        val userFlow = MutableStateFlow(loadingState())
        val onboardingFlow = MutableStateFlow<Boolean?>(null)
        val vm = buildVm(userFlow, onboardingFlow)

        vm.routing.test {
            assertEquals(RoutingDecision.Splash, awaitItem())
        }
    }

    @Test
    fun `routing is Onboarding when user loaded but onboarding not completed`() = runTest {
        val userFlow = MutableStateFlow(successState(user = null))
        val onboardingFlow = MutableStateFlow<Boolean?>(false)
        val vm = buildVm(userFlow, onboardingFlow)

        vm.routing.test {
            assertEquals(RoutingDecision.Onboarding, awaitItem())
        }
    }

    @Test
    fun `routing is Login when user is null and onboarding completed`() = runTest {
        val userFlow = MutableStateFlow(successState(user = null))
        val onboardingFlow = MutableStateFlow<Boolean?>(true)
        val vm = buildVm(userFlow, onboardingFlow)

        vm.routing.test {
            assertEquals(RoutingDecision.Login, awaitItem())
        }
    }

    @Test
    fun `routing is Home when user present and onboarding completed`() = runTest {
        val userFlow = MutableStateFlow(successState(user = mockk(relaxed = true)))
        val onboardingFlow = MutableStateFlow<Boolean?>(true)
        val vm = buildVm(userFlow, onboardingFlow)

        vm.routing.test {
            assertEquals(RoutingDecision.Home, awaitItem())
        }
    }

    // Helper builders below — fill with real constructors.
    private fun buildVm(
        userFlow: MutableStateFlow<*>,
        onboardingFlow: MutableStateFlow<Boolean?>,
    ): MainViewModel {
        // TODO at implementation time: instantiate MainViewModel with mocked dependencies.
        // Every dependency must be mocked via mockk(relaxed = true) except the two flows above.
        return mockk(relaxed = true) // placeholder — replace with real constructor
    }
    private fun loadingState(): Any = mockk(relaxed = true) // replace with real ContentState.Loading
    private fun successState(user: Any?): Any = mockk(relaxed = true) // replace with real ContentState.Success
}
```

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.MainViewModelRoutingTest"`
Expected: the test class fails to construct or assertions fail — good, that proves the tests are active.

**Implementation note:** the test helpers above are intentionally skeletal. Before marking this task done, replace the `TODO at implementation time` parts with real constructor calls by looking at how `MainViewModelTest.kt` (already in `androidUnitTest`) builds the VM.

- [ ] **Step 5: Make the tests pass**

Run the tests again after your routing implementation is complete. Expected: all four pass.
Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.MainViewModelRoutingTest"`

- [ ] **Step 6: Commit**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/MainViewModel.kt shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/MainViewModelRoutingTest.kt
git commit -m "feat: expose RoutingDecision StateFlow on MainViewModel"
```

---

## Task 4 — Top-level routing in FishingNotesApp

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/AppNavGraph.kt` (remove LoginRoute if it lives here)

- [ ] **Step 1: Rewrite `FishingNotesApp()` to switch on RoutingDecision**

Replace the current `FishingNotesApp()` body (the Crossfade on `onboardingCompleted`) with:
```kotlin
@Composable
fun FishingNotesApp() {
    val analyticsTracker: AnalyticsTracker = koinInject()
    val mainViewModel: MainViewModel = koinViewModel()
    val onboardingViewModel: OnboardingViewModel = koinViewModel()
    val routing by mainViewModel.routing.collectAsState()

    CompositionLocalProvider(LocalAnalytics provides analyticsTracker) {
        AnimatedContent(
            targetState = routing,
            transitionSpec = {
                if (initialState is RoutingDecision.Onboarding && targetState is RoutingDecision.Login) {
                    (slideInVertically { it } + fadeIn(tween(250))) togetherWith
                        (fadeOut(tween(150)))
                } else {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                }
            },
            label = "root-routing",
        ) { decision ->
            when (decision) {
                RoutingDecision.Splash -> Unit // native splash stays visible; nothing to draw
                RoutingDecision.Onboarding -> OnboardingScreen(
                    onFinished = { onboardingViewModel.completeOnboarding() },
                )
                RoutingDecision.Login -> LoginScreen(
                    onLoggedIn = { /* no nav action needed — routing flips to Home on its own */ },
                )
                RoutingDecision.Home -> FishingNotesMainContent()
            }
        }
    }
}
```

Add imports: `AnimatedContent`, `slideInVertically`, `fadeIn`, `fadeOut`, `togetherWith`, `tween`, `RoutingDecision`, `LoginScreen`.

- [ ] **Step 2: Remove Login from the in-app nav graph**

In `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/AppNavGraph.kt`, remove the `LoginRoute` composable and any `navigate(HomeGraph) { popUpTo(0) }` call from `LoginScreen`'s callers — Login is now a top-level destination controlled by `RoutingDecision`, not a nav-graph entry. Leave HomeGraph wiring unchanged.

If `LoginScreen` has a parameter like `onLoginSuccess` that navigates, change its signature to match the new call site (a no-arg `onLoggedIn` callback or remove the param entirely if `LoginViewModel` just updates state and the routing flips automatically).

- [ ] **Step 3: Build and verify**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL.

Manual smoke sequence (on a device with cleared app data):
1. Launch → Onboarding appears
2. Swipe through; tap **Continue** on the last page → Login appears with a slide-up + fade
3. Sign in → Home appears
4. Logout → Login reappears (no Onboarding replay)
5. Clear app data, launch, tap Continue, kill the app from recents → relaunch → Login (not Onboarding)

- [ ] **Step 4: Commit**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/AppNavGraph.kt
git commit -m "feat: top-level routing between Splash/Onboarding/Login/Home"
```

---

## Task 5 — Onboarding "Continue" CTA and eager completion

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt`

- [ ] **Step 1: Locate the last-page CTA**

Run: `Read shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt`
Find the button labeled "Get Started" (or the current final-page primary button). Note its current `stringResource(Res.string.<key>)` call.

- [ ] **Step 2: Change the label**

Replace the button's text with the existing string resource if one already exists for "Continue" (check `composeResources/values/strings.xml`); otherwise add one:
```xml
<!-- shared/src/commonMain/composeResources/values/strings.xml -->
<string name="onboarding_continue">Continue</string>
```

And in the composable: `Text(stringResource(Res.string.onboarding_continue))`.

- [ ] **Step 3: Confirm `onFinished` is invoked on tap**

The current call site already invokes `onboardingViewModel.completeOnboarding()` via `onFinished` in Task 4's `FishingNotesApp`. No further change needed here — eager completion (before login succeeds) is already the behavior because `completeOnboarding()` runs on tap, and routing flips to Login immediately afterward.

- [ ] **Step 4: Build and smoke-test**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL.

Smoke: on a cleared device, onboarding's last page now shows "Continue". Tapping it transitions into Login.

- [ ] **Step 5: Commit**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt shared/src/commonMain/composeResources/values/strings.xml
git commit -m "feat: rename onboarding final CTA to Continue"
```

---

## Task 6 — LoginViewModel: remove forced delay, classify errors

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LoginViewModel.kt`
- Create: `shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/LoginViewModelTest.kt`

- [ ] **Step 1: Read current LoginViewModel**

Run: `Read shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LoginViewModel.kt`
Identify: the 2500ms `delay()` call, the `onGoogleSignInFailed()` function, how errors are emitted (SnackbarManager vs state), and the current `LoginState` type.

- [ ] **Step 2: Introduce a richer state type**

Inside `LoginViewModel.kt` (or adjacent), define:
```kotlin
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Signing : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}
```

Replace whatever the current `LoginState` is with `LoginUiState`. Update the exposed `StateFlow<LoginUiState>`.

- [ ] **Step 3: Remove the 2500ms delay**

In the sign-in flow (currently around line 44–56 per the spec), delete the `delay(2500)` call and the Lottie confetti overlay emission. The flow becomes:
```kotlin
fun firebaseSignInWithGoogle(idToken: String) {
    viewModelScope.launch {
        _uiState.value = LoginUiState.Signing
        try {
            val user = auth.signInWithCredential(GoogleAuthProvider.credential(idToken, null)).user
            requireNotNull(user)
            repository.addNewUser(user.toUserDomain())
            _uiState.value = LoginUiState.Success
            // No manual navigation — MainViewModel.routing flips to Home automatically.
        } catch (e: Exception) {
            _uiState.value = LoginUiState.Error(e.userFacingMessage())
        }
    }
}
```

`toUserDomain()` / the existing mapping helper — keep the project's current name; don't rename it.

- [ ] **Step 4: Distinguish cancelled from errors**

Cancelled sign-ins arrive via a separate entry point (the Google auth lib returns null or calls a dedicated callback — verify with the existing LoginScreen `onGoogleSignInResult = { ... }` handler). Replace the body of the cancelled callback with:
```kotlin
fun onGoogleSignInCancelled() {
    _uiState.value = LoginUiState.Idle  // no snackbar, no error text
}
```

Remove any existing `onGoogleSignInFailed()` that treats cancel as an error. Rename the remaining one (if any) to `onGoogleSignInError(throwable)` and route it to `LoginUiState.Error`.

At the LoginScreen call site, `onGoogleSignInResult` receives either a GoogleUser or null:
```kotlin
onGoogleSignInResult = { user ->
    if (user == null) loginViewModel.onGoogleSignInCancelled()
    else loginViewModel.firebaseSignInWithGoogle(user.idToken)
}
```

- [ ] **Step 5: Write tests**

`shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/LoginViewModelTest.kt`:
```kotlin
package com.mobileprism.fishing.viewmodels

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginViewModelTest {

    @Test
    fun `cancelled sign-in returns to Idle without error`() = runTest {
        val vm = buildVm()
        vm.onGoogleSignInCancelled()
        vm.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())
        }
    }

    @Test
    fun `sign-in error surfaces as Error state`() = runTest {
        val auth = mockk<FirebaseAuthApi>()
        coEvery { auth.signInWithCredential(any()) } throws RuntimeException("boom")
        val vm = buildVm(auth)
        vm.firebaseSignInWithGoogle("fake-id-token")
        vm.uiState.test {
            assertEquals(LoginUiState.Signing, awaitItem())
            val next = awaitItem()
            assert(next is LoginUiState.Error) { "expected Error, got $next" }
        }
    }

    @Test
    fun `successful sign-in ends in Success with no forced delay`() = runTest {
        val auth = mockk<FirebaseAuthApi>(relaxed = true)
        val repo = mockk<UserRepository>(relaxed = true)
        // coEvery { auth.signInWithCredential(any()) } returns a result with a non-null user.
        val vm = buildVm(auth, repo)
        val start = testScheduler.currentTime
        vm.firebaseSignInWithGoogle("fake-id-token")
        testScheduler.advanceUntilIdle()
        val elapsed = testScheduler.currentTime - start
        assert(elapsed < 2000) { "no forced delay expected; elapsed=$elapsed" }
        assertEquals(LoginUiState.Success, vm.uiState.value)
        coVerify { repo.addNewUser(any()) }
    }

    private fun buildVm(
        auth: FirebaseAuthApi = mockk(relaxed = true),
        repo: UserRepository = mockk(relaxed = true),
    ): LoginViewModel {
        // TODO at implementation time: use the real LoginViewModel constructor; check the existing
        // LoginViewModel for its actual dependencies (analytics tracker, snackbar manager, etc.)
        return mockk(relaxed = true) // replace with real constructor
    }
}
```

The type names `FirebaseAuthApi` / `UserRepository` are placeholders for the real types the current LoginViewModel depends on — look them up before completing the test.

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.LoginViewModelTest"`
Expected: all three pass after step 3 is complete.

- [ ] **Step 6: Commit**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LoginViewModel.kt shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/LoginViewModelTest.kt
git commit -m "feat: remove 2.5s login delay and classify cancelled vs error"
```

---

## Task 7 — LoginScreen visual blend

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt`

- [ ] **Step 1: Match background and typography to onboarding**

Open `OnboardingScreen.kt`, note the root Box/Column's background color or Brush and the typography used for headlines. Apply the same to LoginScreen's root container. If the values aren't named constants, extract them into a small shared composable or theme token inside `ui/theme/` only if doing so is trivial (one constant); otherwise copy the literal value and leave a short inline comment referencing the source of truth — **and only in this case** is a comment permitted (the CLAUDE.md no-comments rule yields to cross-screen design consistency; reviewer decides).

(If in doubt: copy the color literal, no comment. Keep CLAUDE.md's rule.)

- [ ] **Step 2: Replace the loading and error UI**

Current LoginScreen likely has:
```kotlin
when (state) {
    LoginState.Loading -> FullScreenLoader()
    LoginState.Idle -> SignInButton(...)
    LoginState.Error -> { /* snackbar elsewhere */ }
}
```

Change to a single persistent layout:
```kotlin
Column(
    modifier = Modifier.fillMaxSize().background(...).padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
) {
    AppLogo() // existing or placeholder — a small icon
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(Res.string.login_headline),
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.height(48.dp))
    GoogleSignInButton(
        loading = uiState is LoginUiState.Signing,
        onClick = { /* launch google auth */ },
        enabled = uiState !is LoginUiState.Signing,
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(Res.string.login_trust_copy),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (uiState is LoginUiState.Error) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = uiState.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
```

`GoogleSignInButton` should display a small `CircularProgressIndicator` in place of its icon when `loading = true` and be disabled (`enabled = !loading`) to prevent double taps. Implement this as a local composable if one doesn't exist.

- [ ] **Step 3: Add string resources**

`shared/src/commonMain/composeResources/values/strings.xml`:
```xml
<string name="login_headline">Keep your catches safe</string>
<string name="login_trust_copy">We only use your email to sync your catches. No posts, no ads.</string>
<string name="press_back_again_to_exit">Press back again to exit</string>
```

Only add any key that doesn't already exist. Reuse existing keys where possible.

- [ ] **Step 4: Remove any remaining `SnackbarManager` error calls for cancelled sign-ins**

In LoginScreen / LoginViewModel, grep for `snackbarManager.show` or `showError` in the auth path; ensure cancel → no call. Real errors already flow through `LoginUiState.Error` and the inline Text above.

Run: `Grep pattern "snackbar" path shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt`

- [ ] **Step 5: Build and smoke test**

Run: `./gradlew :shared:compileDebugKotlinAndroid :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL.

Smoke on a device:
1. Go to Login
2. Tap Google sign-in, dismiss the account chooser → no error shown, button returns to idle
3. Disable network, tap sign-in → inline error text appears under the trust line; button becomes tappable again
4. Re-enable network, sign in → Home appears immediately (no 2.5s confetti delay)

- [ ] **Step 6: Commit**
```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt shared/src/commonMain/composeResources/values/strings.xml
git commit -m "feat: blend login screen with onboarding; in-place spinner; inline errors"
```

---

## Task 8 — End-to-end verification

- [ ] **Step 1: Full device smoke**

Uninstall the app (or clear app data). Reinstall debug build. Walk through:
1. First launch → Onboarding (3 pages) → tap Continue → Login slides up
2. Cancel Google account picker → no error UI; can retry immediately
3. Sign in → Home opens without 2.5s pause
4. Logout → Login (not Onboarding)
5. Press back on Home map → "Press back again to exit" snackbar → press back again → app closes

- [ ] **Step 2: Unit tests green**

Run: `./gradlew :shared:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, all existing tests plus the new LoginViewModelTest and MainViewModelRoutingTest pass.

- [ ] **Step 3: Shared module builds on both platforms**

Run: `./gradlew :shared:build`
Expected: BUILD SUCCESSFUL (or, at worst, only the iOS framework link task times out — which is an environment issue tracked separately, not a code failure).

- [ ] **Step 4: Final commit if anything shifted during verification**

If smoke testing surfaced minor fixes (copy tweaks, padding), amend in a new commit:
```bash
git add <paths>
git commit -m "fix: onboarding→login flow polish from smoke test"
```

---

## Self-review notes

**Spec coverage:**
- Flow change (§1 of spec) → Task 3, Task 4
- Visual blend (§2 of spec) → Task 5, Task 7
- Back-button fix (§3 of spec) → Task 1, Task 2
- Login cleanup (§4 of spec) → Task 6, Task 7
- Minor future improvements (§5 of spec) → explicitly out of scope, not implemented here

**Known plan assumptions (verify at implementation time, fix inline if wrong):**
- The exact types of `MainViewModel.userState` and `onboardingPreferences.hasCompletedOnboarding` are used as-is from existing code; Task 3 Step 3's `when` branches may need small adjustments.
- `LoginViewModel`'s exact dependencies (for Task 6 Step 5 tests) come from its existing constructor — look them up before mocking.
- `LoginScreen` currently uses a fullscreen loader and snackbars — the rewrite replaces them entirely, not incrementally.
- `AppNavGraph.kt` currently contains a LoginRoute — if not, Task 4 Step 2 becomes a no-op for that file.
