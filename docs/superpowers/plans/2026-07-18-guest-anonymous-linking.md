# Guest (Anonymous) Auth + Google Linking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users use FishingNotes immediately via a silent Firebase anonymous account, and optionally upgrade to Google later — merging their guest data on collision — with clear guest/linked UI throughout.

**Architecture:** After onboarding, `MainViewModel` triggers `signInAnonymously()` and the app lands on Home (no login gate). The old `LoginScreen` becomes an optional, dismissible `LinkAccount` destination. A reactive `UserRepository.isAnonymous: Flow<Boolean>` (from `Firebase.auth.authStateChanged`) drives guest-vs-linked UI in Profile/EditProfile/Settings. Linking a new Google account keeps the same `uid` (data carries over free); linking a returning account triggers a dedupe-by-id merge of guest data into the existing account.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, gitlive Firebase (`firebase-auth:2.4.0`, `firebase-firestore`), KMPAuth 2.3.1 (`GoogleButtonUiContainer`), Koin, kotlinx-coroutines-test + MockK (tests in `shared/src/androidUnitTest`).

## Global Constraints

- **No code comments.** Do not add inline, block, or doc comments (project rule in CLAUDE.md).
- **All new user-facing strings need EN + RU** — EN in `shared/src/commonMain/composeResources/values/strings.xml`, RU in `shared/src/commonMain/composeResources/values-ru/strings.xml`. Escape apostrophes as `\'`.
- **CMP resources:** access via `import fishing.shared.generated.resources.Res` + `import fishing.shared.generated.resources.*`; `import org.jetbrains.compose.resources.stringResource`.
- **Firebase auth (commonMain):** `dev.gitlive.firebase.auth.*`. Collision exception is `FirebaseAuthUserCollisionException`.
- **Same uid on link:** `linkWithCredential` preserves `uid`; never migrate data for the happy path.
- **Merge must be idempotent** — dedupe by entity `id`; re-running copies nothing new.
- **Anonymous sign-in must complete before any Home/write path is reachable** (routing guarantees this).
- **Unit test command:** `./gradlew :shared:testDebugUnitTest --tests "<FQN>" --console=plain`
- **Compile check (UI, no unit test):** `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
- **Design source of truth:** `docs/superpowers/specs/2026-07-18-guest-anonymous-linking-ux-design.md`

## File Structure

**Create**
- `shared/src/commonMain/.../domain/repository/LinkOutcome.kt` — sealed result of a link attempt.
- `shared/src/commonMain/.../domain/use_cases/auth/GuestMergePlan.kt` — pure dedupe-by-id merge planner (unit-tested).
- `shared/src/commonMain/.../ui/viewmodels/LinkAccountViewModel.kt` — `LinkState` machine for the link/merge flow.
- `shared/src/commonMain/.../ui/LinkAccountScreen.kt` — the optional upgrade screen (repurposed `LoginScreen`).
- `shared/src/androidUnitTest/.../viewmodels/LinkAccountViewModelTest.kt`
- `shared/src/androidUnitTest/.../domain/use_cases/auth/GuestMergePlanTest.kt`

**Modify**
- `domain/repository/UserRepository.kt` — add `isAnonymous`, `signInAnonymously`, `linkWithGoogle`, `clearGuestData` (with default impls so test fakes keep compiling).
- `model/datasource/firebase/FirebaseUserRepositoryImpl.kt` — implement the four new members + merge.
- `viewmodels/MainViewModel.kt` — remove `RoutingDecision.Login`, add `AuthError`, trigger anonymous sign-in.
- `ui/FishingNotesApp.kt` — drop `Login` arm, add `AuthError` arm.
- `ui/NavigationDestinations.kt` — add `MainDestinations.LinkAccount`.
- `ui/AppNavGraph.kt` — add the `LinkAccount` composable.
- `di/CommonViewModelsModule.kt` — register `LinkAccountViewModel`.
- `ui/viewmodels/UserViewModel.kt` — expose `isAnonymous` + `clearGuestData` + `onReauthSignInFailed` (already added earlier).
- `viewmodels/EditProfileViewModel.kt` — expose `isAnonymous`.
- `ui/home/profile/Profile.kt` + `ProfileViews.kt` — guest/linked `UserNameSection`, `ProfileAppBar(isAnonymous)`, Tonal CTA.
- `ui/home/profile/EditProfile.kt` — email field only when linked + guest Info banner.
- `ui/home/settings/SettingsScreen.kt` — `AccountSettingsGroup` guest branch.
- `ui/onboarding/OnboardingScreen.kt` — subtle Text sign-in button on final slide.
- both `strings.xml` files — new keys + RU for `login_trust_copy`.

---

## Phase 1 — Auth foundation (guest entry works end to end)

### Task 1: Add anonymous-auth members to the repository contract

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/domain/repository/UserRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/domain/repository/LinkOutcome.kt`

**Interfaces:**
- Produces: `UserRepository.isAnonymous: Flow<Boolean>`, `suspend fun signInAnonymously(): Result<Unit>`, `suspend fun linkWithGoogle(idToken: String): Result<LinkOutcome>`, `suspend fun clearGuestData(): Result<Unit>`; `sealed interface LinkOutcome { data object Linked; data class Merged(catchesAdded, markersAdded, alreadyPresent) }`.

New members carry **default implementations** so the five inline test fakes (`LoginViewModelTest`, `MainViewModelTest`, `MainViewModelRoutingTest`, `UserViewModelTest`, `EditProfileViewModelTest`) keep compiling without edits. Only `FirebaseUserRepositoryImpl` overrides them.

- [ ] **Step 1: Create `LinkOutcome.kt`**

```kotlin
package com.mobileprism.fishing.domain.repository

sealed interface LinkOutcome {
    data object Linked : LinkOutcome
    data class Merged(
        val catchesAdded: Int,
        val markersAdded: Int,
        val alreadyPresent: Int,
    ) : LinkOutcome
}
```

- [ ] **Step 2: Add members to `UserRepository` with defaults**

In `UserRepository.kt`, add the import and the four members inside the interface (after `setNewProfileData`):

```kotlin
import kotlinx.coroutines.flow.flowOf
```

```kotlin
    val isAnonymous: Flow<Boolean>
        get() = flowOf(false)

    suspend fun signInAnonymously(): Result<Unit> = Result.success(Unit)

    suspend fun linkWithGoogle(idToken: String): Result<LinkOutcome> =
        Result.success(LinkOutcome.Linked)

    suspend fun clearGuestData(): Result<Unit> = Result.success(Unit)
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL (existing fakes unaffected by defaults).

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/domain/repository/UserRepository.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/domain/repository/LinkOutcome.kt
git commit -m "feat(auth): add anonymous + link repository contract"
```

---

### Task 2: Implement `isAnonymous` and `signInAnonymously` in the Firebase repository

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/firebase/FirebaseUserRepositoryImpl.kt`

**Interfaces:**
- Consumes: `Firebase.auth` (already captured as `fireBaseAuth`), `fireBaseAuth.authStateChanged`.
- Produces: overrides of `isAnonymous` and `signInAnonymously()`.

No unit test (needs live Firebase); verified via compile + the routing test in Task 3 (fake-backed).

- [ ] **Step 1: Add imports**

```kotlin
import com.mobileprism.fishing.domain.repository.LinkOutcome
import kotlinx.coroutines.flow.map
```
(`map` is already imported — confirm; do not duplicate.)

- [ ] **Step 2: Override `isAnonymous` (after the `currentUser` property, ~line 49)**

```kotlin
    override val isAnonymous: Flow<Boolean>
        get() = fireBaseAuth.authStateChanged.map { it?.isAnonymous ?: true }
```

- [ ] **Step 3: Override `signInAnonymously` (after `reauthenticateWithGoogle`)**

```kotlin
    override suspend fun signInAnonymously(): Result<Unit> {
        return try {
            fireBaseAuth.signInAnonymously()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
```

- [ ] **Step 4: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/firebase/FirebaseUserRepositoryImpl.kt
git commit -m "feat(auth): implement isAnonymous + signInAnonymously"
```

---

### Task 3: Routing — remove the login gate, sign in anonymously, handle failure

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/MainViewModel.kt`
- Test: `shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/MainViewModelRoutingTest.kt`

**Interfaces:**
- Consumes: `repository.signInAnonymously()`, `repository.currentUser`, `userPreferences.hasCompletedOnboarding`.
- Produces: `RoutingDecision` without `Login`, plus `RoutingDecision.AuthError`; `fun retryAnonymousSignIn()`.

The `else` case (onboarding done, no user) no longer means "show login" — it means "we are about to mint an anonymous user", i.e. stay on `Splash`. A separate collector performs the one-shot sign-in; failure (e.g. offline first launch) surfaces `AuthError` with a retry.

- [ ] **Step 1: Read the current routing test to learn its fake + helpers**

Run: `sed -n '1,80p' shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/MainViewModelRoutingTest.kt`
(You will reuse its fake `UserRepository` + `UserPreferences` construction. Note it currently asserts `RoutingDecision.Login` for the no-user case.)

- [ ] **Step 2: Write the failing tests**

Replace the no-user assertion and add sign-in + failure tests. Add to `MainViewModelRoutingTest.kt` (adapt fake names to those already in the file — `fakeUserRepository(...)`, `fakePreferences(onboarding = ...)`):

```kotlin
    @Test
    fun `no user after onboarding stays on Splash and triggers anonymous sign-in`() = runTest {
        var signInCalls = 0
        val repo = fakeUserRepository(
            currentUserFlow = flowOf(null),
            onSignInAnonymously = { signInCalls++; Result.success(Unit) },
        )
        val vm = MainViewModel(repo, fakeSyncStatusProvider(), fakePreferences(onboarding = true))
        advanceUntilIdle()

        assertEquals(RoutingDecision.Splash, vm.routing.value)
        assertEquals(1, signInCalls)
    }

    @Test
    fun `anonymous sign-in failure routes to AuthError`() = runTest {
        val repo = fakeUserRepository(
            currentUserFlow = flowOf(null),
            onSignInAnonymously = { Result.failure(RuntimeException("offline")) },
        )
        val vm = MainViewModel(repo, fakeSyncStatusProvider(), fakePreferences(onboarding = true))
        advanceUntilIdle()

        assertEquals(RoutingDecision.AuthError, vm.routing.value)
    }
```

If the file's fake `UserRepository` does not expose an `onSignInAnonymously` hook, extend that fake to override `signInAnonymously()` with a settable lambda (default `{ Result.success(Unit) }`), and to override `isAnonymous` returning `flowOf(true)`.

- [ ] **Step 3: Run tests to verify they fail**

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.MainViewModelRoutingTest" --console=plain`
Expected: FAIL (compile error on `RoutingDecision.AuthError` / assertion mismatch).

- [ ] **Step 4: Update `MainViewModel.kt`**

Replace the `RoutingDecision` sealed interface:

```kotlin
sealed interface RoutingDecision {
    data object Splash : RoutingDecision
    data object Onboarding : RoutingDecision
    data object AuthError : RoutingDecision
    data object Home : RoutingDecision
}
```

Add a failure flag and change the routing + add the sign-in effect. Add field near `_userState`:

```kotlin
    private val _anonSignInFailed = MutableStateFlow(false)
```

Replace the `routing` block to combine three flows and drop `Login`:

```kotlin
    val routing: StateFlow<RoutingDecision> = combine(
        userState,
        userPreferences.hasCompletedOnboarding,
        _anonSignInFailed,
    ) { userSt, onboardingDone, anonFailed ->
        when {
            userSt is BaseViewState.Loading -> RoutingDecision.Splash
            !onboardingDone -> RoutingDecision.Onboarding
            userSt is BaseViewState.Success && userSt.data != null -> RoutingDecision.Home
            anonFailed -> RoutingDecision.AuthError
            else -> RoutingDecision.Splash
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RoutingDecision.Splash)
```

Add to `init`:

```kotlin
    init {
        loadCurrentUser()
        ensureAnonymousUser()
    }
```

Add the two functions:

```kotlin
    private var signingInAnonymously = false

    private fun ensureAnonymousUser() {
        viewModelScope.launch {
            combine(
                userState,
                userPreferences.hasCompletedOnboarding,
            ) { userSt, onboardingDone -> userSt to onboardingDone }
                .collectLatest { (userSt, onboardingDone) ->
                    val needsAnon = onboardingDone &&
                        userSt is BaseViewState.Success && userSt.data == null
                    if (needsAnon && !signingInAnonymously) {
                        signingInAnonymously = true
                        _anonSignInFailed.value = false
                        val result = repository.signInAnonymously()
                        signingInAnonymously = false
                        _anonSignInFailed.value = result.isFailure
                    }
                }
        }
    }

    fun retryAnonymousSignIn() {
        _anonSignInFailed.value = false
    }
```

`retryAnonymousSignIn()` clears the flag; the still-null user re-triggers `ensureAnonymousUser`'s collector (it re-evaluates whenever `_anonSignInFailed`-driven routing recomposition happens — but to guarantee a re-attempt, also call the sign-in directly). Implement `retryAnonymousSignIn` as:

```kotlin
    fun retryAnonymousSignIn() {
        viewModelScope.launch {
            _anonSignInFailed.value = false
            val result = repository.signInAnonymously()
            _anonSignInFailed.value = result.isFailure
        }
    }
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.MainViewModelRoutingTest" --console=plain`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/MainViewModel.kt \
        shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/MainViewModelRoutingTest.kt
git commit -m "feat(auth): silent anonymous entry, drop login gate"
```

---

### Task 4: Wire the new routing into `FishingNotesApp`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt`

**Interfaces:**
- Consumes: `RoutingDecision.{Splash,Onboarding,AuthError,Home}`, `mainViewModel.retryAnonymousSignIn()`.

- [ ] **Step 1: Replace the `Login` arm with an `AuthError` arm**

In the `when (decision)` block, remove `RoutingDecision.Login -> LoginScreen()` and add:

```kotlin
                RoutingDecision.AuthError -> AuthErrorScreen(
                    onRetry = { mainViewModel.retryAnonymousSignIn() },
                )
```

Update the `transitionSpec` condition that referenced `RoutingDecision.Login` — change it to plain fade (remove the `Onboarding -> Login` special case):

```kotlin
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
```

Remove now-unused imports (`slideInVertically`). Remove the `import ...ui.LoginScreen` if present (LoginScreen becomes `LinkAccountScreen` in Task 8; keep the file until then).

- [ ] **Step 2: Add a minimal `AuthErrorScreen` composable**

Add at the bottom of `FishingNotesApp.kt` (reuses existing components; strings added in Task 18 — for now use literals then swap, OR add the two strings here). Add strings `auth_error_message` / `retry` (retry already exists). Add EN+RU for `auth_error_message` per Global Constraints, then:

```kotlin
@Composable
private fun AuthErrorScreen(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            modifier = Modifier.padding(Spacing.xxl),
        ) {
            AppText(
                text = stringResource(Res.string.auth_error_message),
                style = AppTextStyle.Body,
                textAlign = TextAlign.Center,
            )
            AppButton(
                text = stringResource(Res.string.retry),
                onClick = onRetry,
                style = AppButtonStyle.Filled,
            )
        }
    }
}
```
Add the required imports (`Box`, `Column`, `Arrangement`, `Alignment`, `Modifier`, `fillMaxSize`, `systemBarsPadding`, `padding`, `Spacing`, `AppText`, `AppTextStyle`, `AppButton`, `AppButtonStyle`, `TextAlign`, `Res`, `stringResource`).

- [ ] **Step 3: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/FishingNotesApp.kt \
        shared/src/commonMain/composeResources/values/strings.xml \
        shared/src/commonMain/composeResources/values-ru/strings.xml
git commit -m "feat(auth): route splash/auth-error, remove login arm"
```

---

## Phase 2 — Link Account screen + happy-path linking

### Task 5: Add the `LinkAccount` navigation destination

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/NavigationDestinations.kt`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/AppNavGraph.kt`

**Interfaces:**
- Produces: `MainDestinations.LinkAccount` (data object route); an `AppNavGraph` `composable<MainDestinations.LinkAccount>` entry rendering `LinkAccountScreen` (created in Task 8).

- [ ] **Step 1: Add the destination** in `NavigationDestinations.kt` inside `object MainDestinations`:

```kotlin
    @Serializable
    data object LinkAccount
```

- [ ] **Step 2: Add the composable** in `AppNavGraph.kt` (after the `EditProfile` composable). Temporarily render a placeholder until Task 8:

```kotlin
    composable<MainDestinations.LinkAccount> {
        LinkAccountScreen(onBack = upPress, onLinked = upPress)
    }
```
Add import: `import com.mobileprism.fishing.ui.LinkAccountScreen`. This will not compile until Task 8 creates `LinkAccountScreen`; do Task 8 in the same commit or land Task 8 first. To keep this task independently green, defer the import + composable body wiring to Task 8's step and here only add the `NavigationDestinations` entry.

**Revised Step 2 (keep this task green):** add only the destination (Step 1). The `AppNavGraph` composable entry is added in Task 8. Skip the AppNavGraph edit here.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/NavigationDestinations.kt
git commit -m "feat(nav): add LinkAccount destination"
```

---

### Task 6: Implement happy-path `linkWithGoogle` (no merge yet)

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/firebase/FirebaseUserRepositoryImpl.kt`

**Interfaces:**
- Consumes: `fireBaseAuth.currentUser`, `GoogleAuthProvider.credential`, `addNewUser`, `setUserListener`.
- Produces: override `linkWithGoogle(idToken)` returning `Result<LinkOutcome.Linked>` on success; on `FirebaseAuthUserCollisionException` returns `Result.failure` for now (merge added in Task 11).

- [ ] **Step 1: Add imports**

```kotlin
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
```

- [ ] **Step 2: Override `linkWithGoogle`**

```kotlin
    override suspend fun linkWithGoogle(idToken: String): Result<LinkOutcome> {
        val user = fireBaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("No signed-in user"))
        return try {
            val result = user.linkWithCredential(GoogleAuthProvider.credential(idToken, null))
            val linked = result.user ?: fireBaseAuth.currentUser
            if (linked != null) addNewUser(linked.toUser())
            Result.success(LinkOutcome.Linked)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
```
Note: keep the `FirebaseAuthUserCollisionException` catch **above** the generic `Exception` catch so Task 11 can slot the merge in without reordering.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/firebase/FirebaseUserRepositoryImpl.kt
git commit -m "feat(auth): link anonymous account to Google (happy path)"
```

---

### Task 7: `LinkAccountViewModel` — Idle/Linking/Success/Error/Cancelled

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LinkAccountViewModel.kt`
- Create: `shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/LinkAccountViewModelTest.kt`

**Interfaces:**
- Consumes: `UserRepository.linkWithGoogle(idToken)`, `AnalyticsTracker`.
- Produces: `LinkState` sealed interface; `LinkAccountViewModel` with `uiState: StateFlow<LinkState>`, `fun linkWithGoogle(idToken: String)`, `fun onSignInCancelled()`, `fun confirmMerge()`, `fun dismissMerge()`, `fun retry()`. (Merge states are defined now but only exercised in Task 12.)

- [ ] **Step 1: Write the failing test**

```kotlin
package com.mobileprism.fishing.viewmodels

import com.mobileprism.fishing.domain.repository.LinkOutcome
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.viewmodels.LinkAccountViewModel
import com.mobileprism.fishing.ui.viewmodels.LinkState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LinkAccountViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val analytics: AnalyticsTracker = mockk(relaxed = true)

    @BeforeTest fun setUp() { Dispatchers.setMain(dispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `successful link ends in Success`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        coEvery { repo.linkWithGoogle("tok") } returns Result.success(LinkOutcome.Linked)
        val vm = LinkAccountViewModel(repo, analytics)

        vm.linkWithGoogle("tok")
        advanceUntilIdle()

        assertEquals(LinkState.Success, vm.uiState.value)
    }

    @Test
    fun `null result is a cancel, not an error`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        val vm = LinkAccountViewModel(repo, analytics)

        vm.onSignInCancelled()
        advanceUntilIdle()

        assertEquals(LinkState.Idle, vm.uiState.value)
    }

    @Test
    fun `thrown link error ends in Error`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        coEvery { repo.linkWithGoogle("tok") } returns Result.failure(RuntimeException("net"))
        val vm = LinkAccountViewModel(repo, analytics)

        vm.linkWithGoogle("tok")
        advanceUntilIdle()

        assertIs<LinkState.Error>(vm.uiState.value)
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.LinkAccountViewModelTest" --console=plain`
Expected: FAIL (unresolved `LinkAccountViewModel`/`LinkState`).

- [ ] **Step 3: Create `LinkAccountViewModel.kt`**

```kotlin
package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.repository.LinkOutcome
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kimplify.cedar.logging.Cedar

sealed interface LinkState {
    data object Idle : LinkState
    data object Linking : LinkState
    data class Merging(val progress: Float?) : LinkState
    data class MergeSuccess(
        val catchesAdded: Int,
        val markersAdded: Int,
        val alreadyPresent: Int,
    ) : LinkState
    data object Success : LinkState
    data object Error : LinkState
}

class LinkAccountViewModel(
    private val repository: UserRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LinkState>(LinkState.Idle)
    val uiState = _uiState.asStateFlow()

    private var pendingIdToken: String? = null

    fun linkWithGoogle(idToken: String) {
        pendingIdToken = idToken
        viewModelScope.launch {
            _uiState.value = LinkState.Linking
            repository.linkWithGoogle(idToken).fold(
                onSuccess = { outcome -> onLinkOutcome(outcome) },
                onFailure = { onLinkError(it) },
            )
        }
    }

    fun onSignInCancelled() {
        _uiState.value = LinkState.Idle
    }

    fun confirmMerge() { /* implemented in Task 12 */ }

    fun dismissMerge() {
        _uiState.value = LinkState.Idle
    }

    fun retry() {
        val token = pendingIdToken
        if (token != null) linkWithGoogle(token) else _uiState.value = LinkState.Idle
    }

    private fun onLinkOutcome(outcome: LinkOutcome) {
        _uiState.value = when (outcome) {
            is LinkOutcome.Linked -> LinkState.Success
            is LinkOutcome.Merged -> LinkState.MergeSuccess(
                outcome.catchesAdded, outcome.markersAdded, outcome.alreadyPresent,
            )
        }
    }

    private fun onLinkError(error: Throwable) {
        _uiState.value = LinkState.Error
        runCatching { analyticsTracker.logEvent(AnalyticsEvent.SignInError(error.message)) }
        runCatching { Cedar.e(error.message ?: "Link failed") }
    }
}
```

- [ ] **Step 4: Run to verify it passes**

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.LinkAccountViewModelTest" --console=plain`
Expected: PASS.

- [ ] **Step 5: Register in Koin** — in `di/CommonViewModelsModule.kt` add import `import com.mobileprism.fishing.ui.viewmodels.LinkAccountViewModel` and a binding:

```kotlin
    viewModel { LinkAccountViewModel(repository = get(), analyticsTracker = get()) }
```

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LinkAccountViewModel.kt \
        shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/LinkAccountViewModelTest.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/di/CommonViewModelsModule.kt
git commit -m "feat(auth): LinkAccountViewModel state machine"
```

---

### Task 8: `LinkAccountScreen` (repurpose LoginScreen) + AppNavGraph wiring

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LinkAccountScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/AppNavGraph.kt`
- Delete: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt` (superseded)
- Modify: strings (Task 18 keys `link_account_title`, `link_account_subtitle`, `link_benefit_backup`, `link_benefit_sync`, `link_benefit_profile`, `link_maybe_later`, `linking_in_progress`, `link_success`) — add the ones this screen references now.

**Interfaces:**
- Consumes: `LinkAccountViewModel`, `LinkState`, `GoogleButtonUiContainer`, `SnackbarManager`.
- Produces: `@Composable fun LinkAccountScreen(onBack: () -> Unit, onLinked: () -> Unit)`.

- [ ] **Step 1: Create `LinkAccountScreen.kt`** based on the existing `LoginScreen` body, wrapped in a top bar with back, benefit rows, "Maybe later", loading dialog, and error banner:

```kotlin
package com.mobileprism.fishing.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.home.views.AppButtonStyle
import com.mobileprism.fishing.ui.home.views.AppText
import com.mobileprism.fishing.ui.home.views.AppTextStyle
import com.mobileprism.fishing.ui.home.views.AppTopBar
import com.mobileprism.fishing.ui.home.views.BannerTone
import com.mobileprism.fishing.ui.home.views.InlineBannerCard
import com.mobileprism.fishing.ui.home.views.ModalLoadingDialog
import com.mobileprism.fishing.ui.theme.BrandGradients
import com.mobileprism.fishing.ui.theme.FishingTheme
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.viewmodels.LinkAccountViewModel
import com.mobileprism.fishing.ui.viewmodels.LinkState
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LinkAccountScreen(onBack: () -> Unit, onLinked: () -> Unit) {
    val vm: LinkAccountViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is LinkState.Success) onLinked()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "",
                navigationIcon = { AppTopBarBackButton(onBack) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandGradients.surfaceVertical(FishingTheme.colorScheme))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_launcher),
                contentDescription = null,
                modifier = Modifier.padding(top = Spacing.xxl).size(120.dp).clip(FishingTheme.shapes.extraLarge),
            )
            Text(
                text = stringResource(Res.string.link_account_title),
                style = FishingTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.lg),
            )
            Text(
                text = stringResource(Res.string.link_account_subtitle),
                style = FishingTheme.typography.bodyLarge,
                color = FishingTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            Column(
                modifier = Modifier.padding(top = Spacing.xl).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                BenefitRow(Icons.Outlined.CloudDone, stringResource(Res.string.link_benefit_backup))
                BenefitRow(Icons.Outlined.Devices, stringResource(Res.string.link_benefit_sync))
                BenefitRow(Icons.Outlined.Person, stringResource(Res.string.link_benefit_profile))
            }

            GoogleButtonUiContainer(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                onGoogleSignInResult = { googleUser ->
                    val idToken = googleUser?.idToken
                    if (idToken != null) vm.linkWithGoogle(idToken) else vm.onSignInCancelled()
                },
            ) {
                AppButton(
                    text = stringResource(Res.string.sign_with_google),
                    leadingIcon = painterResource(Res.drawable.ic_google_logo),
                    onClick = { this@GoogleButtonUiContainer.onClick() },
                    style = AppButtonStyle.Filled,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text(
                text = stringResource(Res.string.login_trust_copy),
                style = FishingTheme.typography.bodySmall,
                color = FishingTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.lg),
            )

            AnimatedVisibility(visible = state is LinkState.Error) {
                InlineBannerCard(
                    tone = BannerTone.Error,
                    icon = Icons.Outlined.Warning,
                    title = stringResource(Res.string.sign_in_generic_error),
                    actionLabel = stringResource(Res.string.retry),
                    onClick = { vm.retry() },
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg),
                )
            }

            AppButton(
                text = stringResource(Res.string.link_maybe_later),
                onClick = onBack,
                style = AppButtonStyle.Text,
                modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.xxl),
            )
        }
    }

    ModalLoadingDialog(
        visible = state is LinkState.Linking,
        text = stringResource(Res.string.linking_in_progress),
    )
}

@Composable
private fun BenefitRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = FishingTheme.colorScheme.primary)
        AppText(text = text, style = AppTextStyle.Body, modifier = Modifier.padding(start = Spacing.md))
    }
}
```

Confirm the exact `AppTopBar` navigation-icon parameter name by reading `ui/home/views/AppTopBar.kt` / `AppBar.kt`; if it exposes `onNavigationClick`/`navigationIcon = ArrowBack` differently, adapt `AppTopBarBackButton`. If `AppTopBar` already renders a back arrow given an `upPress`/`onBack` lambda, use that instead of a custom `AppTopBarBackButton` (delete the helper).

- [ ] **Step 2: Wire the composable in `AppNavGraph.kt`** (after the `EditProfile` composable):

```kotlin
    composable<MainDestinations.LinkAccount> {
        LinkAccountScreen(
            onBack = upPress,
            onLinked = upPress,
        )
    }
```
Add import: `import com.mobileprism.fishing.ui.LinkAccountScreen`.

- [ ] **Step 3: Delete `LoginScreen.kt`** and remove any remaining references.

Run: `grep -rn "LoginScreen" shared/src --include=*.kt | grep -v build`
Expected: no references remain (the `FishingNotesApp` arm was removed in Task 4).
Then: `git rm shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt`

- [ ] **Step 4: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL. (Add the referenced strings from Task 18 first if the build reports missing `Res.string.*`.)

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LinkAccountScreen.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/AppNavGraph.kt
git rm shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LoginScreen.kt
git commit -m "feat(auth): LinkAccount screen replaces login gate"
```

---

## Phase 3 — Returning-account merge

### Task 9: Pure merge planner (dedupe by id)

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/domain/use_cases/auth/GuestMergePlan.kt`
- Create: `shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/domain/use_cases/auth/GuestMergePlanTest.kt`

**Interfaces:**
- Produces: `data class GuestMergePlan(markersToCopy, catchesToCopy, alreadyPresent)`; `fun planGuestMerge(guestMarkers, existingMarkers, guestCatches, existingCatches): GuestMergePlan`.

- [ ] **Step 1: Write the failing test**

```kotlin
package com.mobileprism.fishing.domain.use_cases.auth

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import kotlin.test.Test
import kotlin.test.assertEquals

class GuestMergePlanTest {
    private fun marker(id: String) = UserMapMarker(id = id)
    private fun catch(id: String) = UserCatch(id = id)

    @Test
    fun `copies only ids not already present`() {
        val plan = planGuestMerge(
            guestMarkers = listOf(marker("m1"), marker("m2")),
            existingMarkers = listOf(marker("m2")),
            guestCatches = listOf(catch("c1")),
            existingCatches = emptyList(),
        )
        assertEquals(listOf("m1"), plan.markersToCopy.map { it.id })
        assertEquals(listOf("c1"), plan.catchesToCopy.map { it.id })
        assertEquals(1, plan.alreadyPresent)
    }

    @Test
    fun `idempotent when everything already present`() {
        val plan = planGuestMerge(
            guestMarkers = listOf(marker("m1")),
            existingMarkers = listOf(marker("m1")),
            guestCatches = listOf(catch("c1")),
            existingCatches = listOf(catch("c1")),
        )
        assertEquals(emptyList(), plan.markersToCopy)
        assertEquals(emptyList(), plan.catchesToCopy)
        assertEquals(2, plan.alreadyPresent)
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.domain.use_cases.auth.GuestMergePlanTest" --console=plain`
Expected: FAIL (unresolved reference).

- [ ] **Step 3: Implement `GuestMergePlan.kt`**

```kotlin
package com.mobileprism.fishing.domain.use_cases.auth

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker

data class GuestMergePlan(
    val markersToCopy: List<UserMapMarker>,
    val catchesToCopy: List<UserCatch>,
    val alreadyPresent: Int,
)

fun planGuestMerge(
    guestMarkers: List<UserMapMarker>,
    existingMarkers: List<UserMapMarker>,
    guestCatches: List<UserCatch>,
    existingCatches: List<UserCatch>,
): GuestMergePlan {
    val existingMarkerIds = existingMarkers.mapTo(HashSet()) { it.id }
    val existingCatchIds = existingCatches.mapTo(HashSet()) { it.id }
    val markersToCopy = guestMarkers.filter { it.id !in existingMarkerIds }
    val catchesToCopy = guestCatches.filter { it.id !in existingCatchIds }
    val alreadyPresent = (guestMarkers.size - markersToCopy.size) +
        (guestCatches.size - catchesToCopy.size)
    return GuestMergePlan(markersToCopy, catchesToCopy, alreadyPresent)
}
```

- [ ] **Step 4: Run to verify it passes**

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.domain.use_cases.auth.GuestMergePlanTest" --console=plain`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/domain/use_cases/auth/GuestMergePlan.kt \
        shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/domain/use_cases/auth/GuestMergePlanTest.kt
git commit -m "feat(auth): pure dedupe-by-id guest merge planner"
```

---

### Task 10: Merge implementation in `linkWithGoogle` collision path

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/firebase/FirebaseUserRepositoryImpl.kt`

**Interfaces:**
- Consumes: `planGuestMerge(...)`, `GoogleAuthProvider.credential`, `fireBaseAuth.signInWithCredential`, `dbCollections` (Firestore), `RepositoryCollections` marker/catch read+write.
- Produces: `LinkOutcome.Merged(catchesAdded, markersAdded, alreadyPresent)` from the collision branch; deletes the orphan anonymous user data afterward.

Before writing, read `model/datasource/utils/RepositoryCollections.kt` to learn the exact read/write API (e.g. `getUserMapMarkersCollection()`, `getUserCatchesCollection(markerId)`, `.get().documents`, `.document(id).set(obj)`), matching the pattern already used in `deleteAccount()`.

- [ ] **Step 1: Add imports**

```kotlin
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.use_cases.auth.planGuestMerge
```

- [ ] **Step 2: Snapshot helper (private)** — collect the current (anonymous) account's markers + catches before switching:

```kotlin
    private suspend fun snapshotCurrentData(): Pair<List<UserMapMarker>, List<UserCatch>> {
        val markerDocs = dbCollections.getUserMapMarkersCollection().get().documents
        val markers = markerDocs.mapNotNull { runCatching { it.data<UserMapMarker>() }.getOrNull() }
        val catches = markerDocs.flatMap { markerDoc ->
            dbCollections.getUserCatchesCollection(markerDoc.id).get().documents
                .mapNotNull { runCatching { it.data<UserCatch>() }.getOrNull() }
        }
        return markers to catches
    }
```

- [ ] **Step 3: Replace the collision catch** in `linkWithGoogle` with the merge:

```kotlin
        } catch (e: FirebaseAuthUserCollisionException) {
            runCatching { mergeGuestIntoExisting(idToken) }
                .getOrElse { Result.failure(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
```

- [ ] **Step 4: Add `mergeGuestIntoExisting`**

```kotlin
    private suspend fun mergeGuestIntoExisting(idToken: String): Result<LinkOutcome> {
        val anonUid = fireBaseAuth.currentUser?.uid
        val (guestMarkers, guestCatches) = snapshotCurrentData()

        fireBaseAuth.signInWithCredential(GoogleAuthProvider.credential(idToken, null))
        val linked = fireBaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("Sign-in failed during merge"))

        val (existingMarkers, existingCatches) = snapshotCurrentData()
        val plan = planGuestMerge(guestMarkers, existingMarkers, guestCatches, existingCatches)

        plan.markersToCopy.forEach { marker ->
            dbCollections.getUserMapMarkersCollection().document(marker.id)
                .set(marker.copy(userId = linked.uid))
        }
        plan.catchesToCopy.forEach { catch ->
            dbCollections.getUserCatchesCollection(catch.userMarkerId).document(catch.id)
                .set(catch.copy(userId = linked.uid))
        }

        addNewUser(linked.toUser())

        if (anonUid != null && anonUid != linked.uid) {
            runCatching { deleteAnonymousUserData(anonUid) }
        }

        return Result.success(
            LinkOutcome.Merged(
                catchesAdded = plan.catchesToCopy.size,
                markersAdded = plan.markersToCopy.size,
                alreadyPresent = plan.alreadyPresent,
            )
        )
    }

    private suspend fun deleteAnonymousUserData(anonUid: String) {
        dbCollections.getUsersCollection().document(anonUid).delete()
    }
```

Adapt the exact write API (`getUserMapMarkersCollection()` may be current-user scoped via `getCurrentUserId()`; after `signInWithCredential` it resolves to the new uid — confirm in `RepositoryCollections.kt`, and use an explicit uid path if the collection helper does not re-read the current user). If the collection helpers hard-bind the uid at construction, add uid-parameterized read/write variants in `RepositoryCollections.kt` as part of this task.

- [ ] **Step 5: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/firebase/FirebaseUserRepositoryImpl.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/utils/RepositoryCollections.kt
git commit -m "feat(auth): merge guest data into existing account on collision"
```

---

### Task 11: Merge UI states in `LinkAccountViewModel` + screen dialogs

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LinkAccountViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LinkAccountScreen.kt`
- Modify: `shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/LinkAccountViewModelTest.kt`

Note: with the Task 10 design, `repository.linkWithGoogle` performs the merge internally and returns `LinkOutcome.Merged`, so the ViewModel does not need a separate `MergeConfirm` round-trip to the repository. To honor the spec's **confirm-before-merge** UX, `linkWithGoogle` in the repo must NOT auto-merge; instead the collision should surface to the VM for confirmation. **Revised approach:** split the repo into `linkWithGoogle(idToken)` (happy path; returns `Result.failure(FirebaseAuthUserCollisionException)` on collision — do NOT merge) and `mergeGuestIntoExisting(idToken)` as a **public** repository method the VM calls after the user confirms.

- [ ] **Step 1: Make merge explicit in the contract** — in `UserRepository.kt` add (with default):

```kotlin
    suspend fun mergeGuestIntoGoogle(idToken: String): Result<LinkOutcome> =
        Result.success(LinkOutcome.Linked)
```
In `FirebaseUserRepositoryImpl.kt`, make `mergeGuestIntoExisting` an `override suspend fun mergeGuestIntoGoogle(idToken: String)`, and revert `linkWithGoogle`'s collision branch to `Result.failure(e)` (no auto-merge).

- [ ] **Step 2: Write the failing VM tests**

```kotlin
    @Test
    fun `collision moves to MergeConfirm`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        coEvery { repo.linkWithGoogle("tok") } returns
            Result.failure(dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException("exists"))
        val vm = LinkAccountViewModel(repo, analytics)

        vm.linkWithGoogle("tok")
        advanceUntilIdle()

        assertIs<LinkState.MergeConfirm>(vm.uiState.value)
    }

    @Test
    fun `confirmMerge ends in MergeSuccess with counts`() = runTest {
        val repo = mockk<UserRepository>(relaxed = true)
        coEvery { repo.linkWithGoogle("tok") } returns
            Result.failure(dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException("exists"))
        coEvery { repo.mergeGuestIntoGoogle("tok") } returns
            Result.success(LinkOutcome.Merged(catchesAdded = 3, markersAdded = 1, alreadyPresent = 2))
        val vm = LinkAccountViewModel(repo, analytics)

        vm.linkWithGoogle("tok")
        advanceUntilIdle()
        vm.confirmMerge()
        advanceUntilIdle()

        val s = vm.uiState.value
        assertIs<LinkState.MergeSuccess>(s)
        assertEquals(3, s.catchesAdded)
    }
```

- [ ] **Step 3: Run to verify they fail**

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.LinkAccountViewModelTest" --console=plain`
Expected: FAIL (`LinkState.MergeConfirm` unresolved).

- [ ] **Step 4: Update `LinkAccountViewModel`** — add `MergeConfirm` state and wire confirm:

Add to `LinkState`:
```kotlin
    data object MergeConfirm : LinkState
```
Change `onLinkError` to branch on the collision type:
```kotlin
    private fun onLinkError(error: Throwable) {
        if (error is dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException) {
            _uiState.value = LinkState.MergeConfirm
            return
        }
        _uiState.value = LinkState.Error
        runCatching { analyticsTracker.logEvent(AnalyticsEvent.SignInError(error.message)) }
        runCatching { Cedar.e(error.message ?: "Link failed") }
    }
```
Implement `confirmMerge`:
```kotlin
    fun confirmMerge() {
        val token = pendingIdToken ?: return
        viewModelScope.launch {
            _uiState.value = LinkState.Merging(progress = null)
            repository.mergeGuestIntoGoogle(token).fold(
                onSuccess = { onLinkOutcome(it) },
                onFailure = { _uiState.value = LinkState.Error },
            )
        }
    }
```

- [ ] **Step 5: Run to verify they pass**

Run: `./gradlew :shared:testDebugUnitTest --tests "com.mobileprism.fishing.viewmodels.LinkAccountViewModelTest" --console=plain`
Expected: PASS.

- [ ] **Step 6: Add merge dialogs to `LinkAccountScreen`** — after the `ModalLoadingDialog(Linking)`:

```kotlin
    val mergeState = state
    if (mergeState is LinkState.MergeConfirm) {
        DefaultDialog(
            primaryText = stringResource(Res.string.merge_confirm_title),
            secondaryText = stringResource(Res.string.merge_confirm_message),
            positiveButtonText = stringResource(Res.string.merge_confirm_positive),
            onPositiveClick = { vm.confirmMerge() },
            negativeButtonText = stringResource(Res.string.cancel),
            onNegativeClick = { vm.dismissMerge() },
            onDismiss = { vm.dismissMerge() },
        )
    }
    ModalLoadingDialog(
        visible = mergeState is LinkState.Merging,
        text = stringResource(Res.string.merge_in_progress) + "\n" + stringResource(Res.string.merge_progress_detail),
        progress = (mergeState as? LinkState.Merging)?.progress,
    )
    if (mergeState is LinkState.MergeSuccess) {
        val message = if (mergeState.alreadyPresent > 0) {
            stringResource(
                Res.string.merge_done_message_deduped,
                mergeState.catchesAdded + mergeState.markersAdded,
                mergeState.alreadyPresent,
            )
        } else {
            stringResource(Res.string.merge_done_message, mergeState.catchesAdded, mergeState.markersAdded)
        }
        DefaultDialog(
            primaryText = stringResource(Res.string.merge_done_title),
            secondaryText = message,
            positiveButtonText = stringResource(Res.string.merge_view_log),
            onPositiveClick = { onLinked() },
            onDismiss = { onLinked() },
        )
    }
```
Add imports for `DefaultDialog`. (`DefaultDialog` requires `onDismiss`; `negativeButtonText`/`onNegativeClick` are optional — omit them on the success dialog so only the positive button shows.)

- [ ] **Step 7: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL (add the merge strings from Task 18 first if missing).

- [ ] **Step 8: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/LinkAccountViewModel.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/LinkAccountScreen.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/domain/repository/UserRepository.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/firebase/FirebaseUserRepositoryImpl.kt \
        shared/src/androidUnitTest/kotlin/com/mobileprism/fishing/viewmodels/LinkAccountViewModelTest.kt
git commit -m "feat(auth): confirm-before-merge flow with dedupe counts"
```

---

## Phase 4 — Guest UI (Profile / EditProfile / Settings)

### Task 12: Surface `isAnonymous` into `UserViewModel` and `EditProfileViewModel`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/UserViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/EditProfileViewModel.kt`

**Interfaces:**
- Consumes: `userRepository.isAnonymous: Flow<Boolean>`.
- Produces: `UserViewModel.isAnonymous: StateFlow<Boolean>`, `EditProfileViewModel.isAnonymous: StateFlow<Boolean>`, `UserViewModel.clearGuestData()`.

- [ ] **Step 1: Add `isAnonymous` + `clearGuestData` to `UserViewModel`**

Add imports (`stateIn`, `SharingStarted`, `map` if needed) and:

```kotlin
    val isAnonymous: StateFlow<Boolean> = userRepository.isAnonymous
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun clearGuestData() {
        viewModelScope.launch {
            userRepository.clearGuestData()
        }
    }
```

- [ ] **Step 2: Add `isAnonymous` to `EditProfileViewModel`**

```kotlin
    val isAnonymous: StateFlow<Boolean> = userRepository.isAnonymous
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
```
(Confirm `EditProfileViewModel` already holds a `userRepository` reference — it is constructed with `userRepository = get()` in Koin. If it only has `userDatastore`, add the `userRepository` constructor param and update the Koin binding.)

- [ ] **Step 3: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Implement `clearGuestData` in the repository** — in `FirebaseUserRepositoryImpl.kt`:

```kotlin
    override suspend fun clearGuestData(): Result<Unit> {
        return try {
            val anonUid = fireBaseAuth.currentUser?.uid
            withContext(NonCancellable) { clearLocalUserData() }
            if (anonUid != null) {
                runCatching { dbCollections.getUsersCollection().document(anonUid).delete() }
                runCatching { fireBaseAuth.currentUser?.delete() }
            }
            signInAnonymously()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
```

- [ ] **Step 5: Verify compile + existing tests still pass**

Run: `./gradlew :shared:testDebugUnitTest --console=plain`
Expected: BUILD SUCCESSFUL, all tests PASS.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/viewmodels/UserViewModel.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/viewmodels/EditProfileViewModel.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/firebase/FirebaseUserRepositoryImpl.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/di/CommonViewModelsModule.kt
git commit -m "feat(auth): expose isAnonymous + clearGuestData to viewmodels"
```

---

### Task 13: Profile guest/linked variants + Tonal CTA

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/profile/Profile.kt`
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/profile/ProfileViews.kt`

**Interfaces:**
- Consumes: `UserViewModel.isAnonymous`, `MainDestinations.LinkAccount`, `IconStatChip`, `AppButton(Tonal)`.

- [ ] **Step 1: `ProfileAppBar` hides logout for guests** — change signature to `ProfileAppBar(navController: NavController, isAnonymous: Boolean)` and wrap the logout `IconButton` + `LogoutDialog` in `if (!isAnonymous) { ... }`.

- [ ] **Step 2: Collect `isAnonymous` in `Profile`** and pass down:

```kotlin
    val isAnonymous by viewModel.isAnonymous.collectAsState()
    ...
    topBar = { ProfileAppBar(navController = navController, isAnonymous = isAnonymous) },
    ...
    UserNameSection(user, isAnonymous)
    if (isAnonymous) {
        AppButton(
            text = stringResource(Res.string.guest_link_cta),
            leadingIcon = painterResource(Res.drawable.ic_google_logo),
            onClick = { navController.navigate(MainDestinations.LinkAccount) },
            style = AppButtonStyle.Tonal,
            modifier = Modifier.padding(horizontal = Spacing.lg).fillMaxWidth(),
        )
    }
```
Add imports: `AppButton`, `AppButtonStyle`, `fillMaxWidth`.

- [ ] **Step 3: `UserNameSection(user, isAnonymous)` variants**

```kotlin
@Composable
private fun UserNameSection(user: User, isAnonymous: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        Text(
            text = if (user.displayName.isEmpty()) stringResource(Res.string.anonymous) else user.displayName,
            style = FishingTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        if (isAnonymous) {
            IconStatChip(
                icon = Icons.Outlined.CloudOff,
                label = stringResource(Res.string.guest_chip_label),
            )
            AppText(
                text = stringResource(Res.string.guest_status_subtitle),
                style = AppTextStyle.Body,
                color = FishingTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            AppText(
                text = stringResource(Res.string.register_date_value, user.registerDate.toDateTextMonth()),
                style = AppTextStyle.Body,
                color = FishingTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CloudDone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = FishingTheme.colorScheme.primary,
                )
                AppText(
                    text = stringResource(Res.string.profile_backed_up),
                    style = AppTextStyle.Body,
                    color = FishingTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = Spacing.xs),
                )
            }
        }
    }
}
```
Add imports: `IconStatChip`, `Icons.Outlined.CloudOff`, `Icons.Outlined.CloudDone`, `Row`, `Icon`, `Modifier.size`.

- [ ] **Step 4: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL (add strings from Task 18 first if missing).

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/profile/Profile.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/profile/ProfileViews.kt
git commit -m "feat(profile): guest/linked variants + link CTA"
```

---

### Task 14: EditProfile — hide email for guests + Info banner

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/profile/EditProfile.kt`

**Interfaces:**
- Consumes: `EditProfileViewModel.isAnonymous`, `MainDestinations.LinkAccount`, `InlineBannerCard(Info)`.

Read `EditProfile.kt` fully first (the email `FormTextField` is around lines 180-185; the enclosing `Column` starts ~line 134).

- [ ] **Step 1: Collect `isAnonymous`** near the other state in `EditProfile`:

```kotlin
    val isAnonymous by viewModel.isAnonymous.collectAsState()
```
(`EditProfile` currently takes `onBack`; it needs a `NavController` to navigate to `LinkAccount`. If it lacks one, add `navController: NavController` to `EditProfile(...)` and pass it from `AppNavGraph`'s `EditProfile(upPress)` composable — update that call site to `EditProfile(upPress, navController)`.)

- [ ] **Step 2: Guard the email field**

```kotlin
            if (!isAnonymous) {
                FormTextField(
                    value = currentUser.email,
                    onValueChange = { },
                    label = stringResource(Res.string.email_hint),
                    leadingIcon = rememberVectorPainter(Icons.Default.Email),
                    readOnly = true,
                )
            }
```
(Match the existing `FormTextField` parameters exactly as in the current file.)

- [ ] **Step 3: Add the guest Info banner** at the end of the `Column`:

```kotlin
            if (isAnonymous) {
                InlineBannerCard(
                    tone = BannerTone.Info,
                    icon = Icons.Default.CloudUpload,
                    title = stringResource(Res.string.editprofile_guest_note),
                    actionLabel = stringResource(Res.string.link_action),
                    onClick = { navController.navigate(MainDestinations.LinkAccount) },
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg),
                )
            }
```
Add imports: `InlineBannerCard`, `BannerTone`, `Icons.Default.CloudUpload`, `MainDestinations`.

- [ ] **Step 4: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/profile/EditProfile.kt \
        shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/AppNavGraph.kt
git commit -m "feat(profile): guest EditProfile hides email, adds link nudge"
```

---

### Task 15: Settings Account group — guest branch

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes: `UserViewModel.isAnonymous`, `UserViewModel.clearGuestData()`, `MainDestinations.LinkAccount`, `SettingsNavLink`, `DefaultDialog`.

`AccountSettingsGroup` (around `SettingsScreen.kt:372-436`) currently shows only "Delete account". It already obtains `koinViewModel<UserViewModel>()`. `SettingsScreen` receives `navController`.

- [ ] **Step 1: Branch the group on `isAnonymous`**

Inside `AccountSettingsGroup`, collect the flag and split. Add a `navController` param to `AccountSettingsGroup` if it doesn't have one (pass from `SettingsScreen`):

```kotlin
    val isAnonymous by viewModel.isAnonymous.collectAsState()
    var isClearDataDialogOpen by remember { mutableStateOf(false) }

    if (isClearDataDialogOpen) {
        DefaultDialog(
            primaryText = stringResource(Res.string.guest_clear_data_title),
            secondaryText = stringResource(Res.string.guest_clear_data_message),
            negativeButtonText = stringResource(Res.string.cancel),
            onNegativeClick = { isClearDataDialogOpen = false },
            positiveButtonText = stringResource(Res.string.delete),
            onPositiveClick = {
                isClearDataDialogOpen = false
                viewModel.clearGuestData()
            },
            onDismiss = { isClearDataDialogOpen = false },
        )
    }

    SettingsGroup(title = stringResource(Res.string.settings_account)) {
        if (isAnonymous) {
            SettingsNavLink(
                title = stringResource(Res.string.settings_sign_in_title),
                subtitle = stringResource(Res.string.settings_sign_in_subtitle),
                icon = Icons.Default.CloudUpload,
                onClick = { navController.navigate(MainDestinations.LinkAccount) },
            )
            SettingsNavLink(
                title = stringResource(Res.string.guest_clear_data),
                subtitle = null,
                icon = Icons.Default.DeleteForever,
                onClick = { isClearDataDialogOpen = true },
            )
        } else {
            SettingsNavLink(
                title = stringResource(Res.string.delete_account),
                subtitle = stringResource(Res.string.delete_account_subtitle),
                icon = Icons.Default.DeleteForever,
                onClick = { isConfirmDialogOpen = true },
            )
        }
    }
```
Keep the existing delete/reauth dialogs (the `isConfirmDialogOpen`, `DeleteAccountState.ReauthRequired`, `ModalLoadingDialog`) unchanged — they only apply to the linked branch. Confirm `SettingsNavLink`'s `subtitle` accepts null; if it requires a String, pass `""`.

- [ ] **Step 2: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/home/settings/SettingsScreen.kt
git commit -m "feat(settings): guest sign-in + clear-data, linked delete"
```

---

## Phase 5 — Onboarding entry + strings

### Task 16: Onboarding final-slide sign-in button

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt`

**Interfaces:**
- Consumes: `onFinished` (already completes onboarding → routing mints the anonymous user). The Google Text button also finishes onboarding, then navigates to `LinkAccount` once Home is shown. Simplest MVP: the Text button just calls `onFinished()` (same as Start); linking is then one tap away on Profile. To route straight to LinkAccount would require passing a nav callback through onboarding — out of scope. Implement the button as a second, low-emphasis finish affordance.

- [ ] **Step 1: Add a Text button on the last page** — inside `OnboardingPage` (or the last-page branch), when `isCurrentPage && isLastPage`, render under the primary button:

```kotlin
                if (isLastPage) {
                    AppButton(
                        text = stringResource(Res.string.sign_with_google),
                        onClick = onButtonClick,
                        style = AppButtonStyle.Text,
                    )
                }
```
Where `isLastPage` is derived from the page index (thread it into `OnboardingPage` as a `Boolean` param if not present). Since both buttons currently call `onButtonClick`/`onFinished`, this simply offers a labeled alternative; wiring a direct LinkAccount jump is deferred (documented). If `OnboardingPage` does not already know it is the last page, add an `isLastPage: Boolean` parameter and pass `pageIndex == pages.size - 1`.

- [ ] **Step 2: Verify compile**

Run: `./gradlew :shared:compileDebugKotlinAndroid --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/onboarding/OnboardingScreen.kt
git commit -m "feat(onboarding): optional sign-in affordance on final slide"
```

---

### Task 17: All new string resources (EN + RU)

**Files:**
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`
- Modify: `shared/src/commonMain/composeResources/values-ru/strings.xml`

Add these keys to BOTH files (RU column into `values-ru`). Also add RU for the existing EN-only `login_trust_copy`. Do this task **first in practice** if any earlier task reports missing `Res.string.*`; it is listed last only because it has no test cycle.

- [ ] **Step 1: Add to `values/strings.xml`**

```xml
<string name="auth_error_message">Couldn\'t start the app. Check your connection and try again.</string>
<string name="guest_chip_label">Guest</string>
<string name="guest_status_subtitle">Your catches are saved on this device</string>
<string name="profile_backed_up">Backed up &amp; syncing</string>
<string name="guest_link_cta">Sign in to back up</string>
<string name="link_action">Sign in</string>
<string name="settings_sign_in_title">Sign in with Google</string>
<string name="settings_sign_in_subtitle">Back up and sync your catches</string>
<string name="link_account_title">Back up your catches</string>
<string name="link_account_subtitle">Sign in with Google to sync across devices and never lose a catch.</string>
<string name="link_benefit_backup">Never lose a catch or spot</string>
<string name="link_benefit_sync">Sync across your devices</string>
<string name="link_benefit_profile">Get a real profile</string>
<string name="link_maybe_later">Maybe later</string>
<string name="linking_in_progress">Linking your account…</string>
<string name="link_success">Signed in. Your catches are backed up.</string>
<string name="merge_confirm_title">This account already has data</string>
<string name="merge_confirm_message">This Google account has its own catches and places. We\'ll combine everything into one account — nothing gets deleted.</string>
<string name="merge_confirm_positive">Combine</string>
<string name="merge_in_progress">Combining your catches…</string>
<string name="merge_progress_detail">Keep the app open.</string>
<string name="merge_done_title">Welcome back!</string>
<string name="merge_done_message">%1$d catches and %2$d places are now in your account.</string>
<string name="merge_done_message_deduped">%1$d added, %2$d were already in your account.</string>
<string name="merge_view_log">View my log</string>
<string name="merge_error">Couldn\'t finish combining. Your catches are safe — please try again.</string>
<string name="editprofile_guest_note">Sign in with Google to back up your profile</string>
<string name="guest_clear_data">Clear all data</string>
<string name="guest_clear_data_title">Clear all data?</string>
<string name="guest_clear_data_message">You\'re using the app as a guest, so there\'s no backup. This permanently erases every catch and spot on this device.</string>
<string name="login_trust_copy">We only use your email to sync your catches. No posts, no ads.</string>
```
If `login_trust_copy` already exists in `values/strings.xml`, do not duplicate it — only add its RU counterpart in Step 2.

- [ ] **Step 2: Add to `values-ru/strings.xml`**

```xml
<string name="auth_error_message">Не удалось запустить приложение. Проверьте подключение и попробуйте снова.</string>
<string name="guest_chip_label">Гость</string>
<string name="guest_status_subtitle">Ваши уловы сохранены на этом устройстве</string>
<string name="profile_backed_up">Сохранено и синхронизируется</string>
<string name="guest_link_cta">Войти и сохранить</string>
<string name="link_action">Войти</string>
<string name="settings_sign_in_title">Войти через Google</string>
<string name="settings_sign_in_subtitle">Сохраняйте и синхронизируйте уловы</string>
<string name="link_account_title">Сохраните свои уловы</string>
<string name="link_account_subtitle">Войдите через Google, чтобы синхронизировать данные и не потерять ни одного улова.</string>
<string name="link_benefit_backup">Не потеряете ни улова, ни точки</string>
<string name="link_benefit_sync">Синхронизация на всех устройствах</string>
<string name="link_benefit_profile">Настоящий профиль</string>
<string name="link_maybe_later">Позже</string>
<string name="linking_in_progress">Связываем аккаунт…</string>
<string name="link_success">Готово. Ваши уловы сохранены.</string>
<string name="merge_confirm_title">В этом аккаунте уже есть данные</string>
<string name="merge_confirm_message">В этом аккаунте Google уже есть уловы и места. Мы объединим всё в один аккаунт — ничего не удалится.</string>
<string name="merge_confirm_positive">Объединить</string>
<string name="merge_in_progress">Объединяем уловы…</string>
<string name="merge_progress_detail">Не закрывайте приложение.</string>
<string name="merge_done_title">С возвращением!</string>
<string name="merge_done_message">%1$d уловов и %2$d мест теперь в вашем аккаунте.</string>
<string name="merge_done_message_deduped">%1$d добавлено, %2$d уже были в аккаунте.</string>
<string name="merge_view_log">Открыть дневник</string>
<string name="merge_error">Не удалось завершить объединение. Ваши уловы в безопасности — попробуйте ещё раз.</string>
<string name="editprofile_guest_note">Войдите через Google, чтобы сохранить профиль</string>
<string name="guest_clear_data">Очистить данные</string>
<string name="guest_clear_data_title">Очистить все данные?</string>
<string name="guest_clear_data_message">Вы используете приложение как гость, резервной копии нет. Это навсегда удалит все уловы и точки на этом устройстве.</string>
<string name="login_trust_copy">Мы используем вашу почту только для синхронизации уловов. Без публикаций и рекламы.</string>
```

- [ ] **Step 3: Verify the whole module builds + all tests pass**

Run: `./gradlew :shared:testDebugUnitTest --console=plain`
Expected: BUILD SUCCESSFUL, all tests PASS.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/composeResources/values/strings.xml \
        shared/src/commonMain/composeResources/values-ru/strings.xml
git commit -m "feat(i18n): guest + linking strings (EN/RU)"
```

---

## Phase 6 — Verification

### Task 18: End-to-end verification build + manual smoke

**Files:** none (verification only).

- [ ] **Step 1: Full build + tests**

Run: `./gradlew :shared:testDebugUnitTest :androidApp:assembleDebug --console=plain`
Expected: BUILD SUCCESSFUL; all unit tests pass.

- [ ] **Step 2: Manual smoke (use the `run` skill / an emulator)** — verify each spec §16 item:
  - Fresh install → onboarding → lands on Map with no login (guest). No `userId="Anonymous"` rows created (check no writes before auth).
  - Profile shows "Guest" chip + "saved on this device" + Tonal "Sign in to back up"; logout icon hidden.
  - Link a brand-new Google account → catches/markers/photos intact, uid unchanged, Profile flips to "Backed up & syncing".
  - Link a returning Google account → confirm dialog → progress → "Welcome back!" with correct counts; re-running adds nothing (idempotent).
  - Cancel Google sheet → no error banner. Force a thrown error (airplane mode mid-link) → error banner + Retry.
  - Guest → Settings → Clear all data → wiped, fresh anonymous user, back on Map.
  - Verify light + dark on Profile, LinkAccount, EditProfile, Settings.

- [ ] **Step 3: Verify Firebase Storage read rule** for `markerImages/**` is not per-uid (so merged catches keep photos). If it is per-uid, add a follow-up task to re-upload photos during merge.

- [ ] **Step 4: Update `RELEASES.md`** with the guest-auth feature under the next version and commit.

---

## Self-Review Notes

- **Spec coverage:** §1–§10 mapped to Tasks 1–17; §16 verification → Task 18. Copy table (§11) → Task 17. Component reuse (§12) honored (no new composables except the `LinkAccountScreen`/`AuthErrorScreen`, both assembled from existing parts). Plumbing (§13) → Tasks 1,2,6,10,11,12. Edge cases (§14): reactive `isAnonymous` (Task 12), idempotent merge (Tasks 9–11), photos (Task 18 Step 3), null=cancel (Task 7), orphan cleanup (Task 10), offline first-launch (Task 3 `AuthError`), RU gap (Task 17).
- **Deviations from spec (intentional):** (1) `isAnonymous` is a reactive repo flow only — the `User` model is not modified (avoids persisting a stale auth flag). (2) Added `RoutingDecision.AuthError` for offline first-launch, which the spec did not cover. (3) Merge is confirm-then-`mergeGuestIntoGoogle()` (explicit method) rather than auto-merge inside `linkWithGoogle`, to honor the confirm-before-merge UX.
- **Type consistency:** `LinkOutcome.Merged(catchesAdded, markersAdded, alreadyPresent)` is produced by the repo (Task 10) and consumed by the VM (Tasks 7, 11) and screen (Task 11) with the same field names. `LinkState` names are stable across Tasks 7 and 11.
- **Open confirmations for the implementer** (verify while coding, adjust locally): exact `AppTopBar` back-arrow API; `RepositoryCollections` uid-scoping after `signInWithCredential`; `EditProfileViewModel` already holds `userRepository`; `SettingsNavLink.subtitle` nullability.
