# CMP Migration Plan

## Current State (Updated Feb 2026)

- **commonMain**: ~83 files (domain entities, repository interfaces, use cases, utils, Koin module, Ktor networking, Room KMP database, gitlive Firebase repos, auth)
- **androidMain**: ~142 files (UI, ViewModels, paging/sync repos, datastore impls, DI)

### Completed Phases

| Phase | Scope | Status |
|-------|-------|--------|
| ~~0-5~~ | ~~Domain layer (entities, interfaces, use cases)~~ | ~~Done~~ |
| ~~6~~ (partial) | ~~Datastore interfaces: `WeatherPreferences`, `UserDatastore`~~ | ~~Done~~ |
| ~~7~~ (partial) | ~~Auth abstraction: `AuthRepository` + `FirebaseAuthRepository` in commonMain~~ | ~~Done~~ |
| ~~8~~ (partial) | ~~Model utilities: `SafeApiCall`, `RepositoryCollections`~~ | ~~Done~~ |
| ~~9~~ | ~~Retrofit → Ktor (3.1.1)~~ | ~~Done~~ |
| ~~10~~ | ~~Room → Room KMP (2.7+)~~ | ~~Done~~ |
| ~~11~~ | ~~Firebase → gitlive multiplatform SDK (2.1.0)~~ | ~~Done~~ |

### Remaining Work

| Item | Location | Blocker |
|------|----------|---------|
| `NotesPreferences` interface | androidMain → commonMain | Sort enums need splitting |
| `SaveNewCatchUseCase` | androidMain | ViewModel data classes (`Uri`), paged `CatchesRepository` |
| `GetPlaceNameUseCase` | androidMain | Android `Geocoder` — stays |
| `UserPreferences` | androidMain | Concrete class, Android Context — stays |
| `SyncStatusManager`, `SyncWorker`, `SyncScheduler` | androidMain | WorkManager — stays Android |
| Paging repos (`FirebaseCatchesPagedRepository`, etc.) | androidMain | Android Paging 3 — stays |
| 12 ViewModels | androidMain | Various Android deps |
| ~99 UI Composables | androidMain | Full CMP UI migration |

---

## Phase 12: ViewModels → commonMain (NEXT)

**Goal**: Move ViewModel logic to shared code using KMP-compatible ViewModel.

This is the next meaningful phase. Phases 6-8 have only minor leftovers that can be addressed
incrementally alongside this work. Phases 9-11 are complete.

---

### Pre-requisites (cleanup from Phases 6-8)

Complete these small items before or alongside ViewModel migration:

#### 12-pre-a: Move `NotesPreferences` interface to commonMain

The interface depends on `CatchesSortValues` / `PlacesSortValues` enums which have Android deps
(`@StringRes`, `R.string.*`, Firestore `Query.Direction`). Strategy: **split the enums**.

1. Create pure enum in commonMain (just enum values, no Android annotations):

```kotlin
// commonMain: domain/entity/common/SortValues.kt
enum class CatchesSortValues { Default, TimeAsc, TimeDesc, NameAsc, NameDesc, FishDesc }
enum class PlacesSortValues { Default, TimeAsc, TimeDesc, NameAsc, NameDesc, CatchesDesc }
```

2. Move `NotesPreferences` interface to commonMain, importing the new common enums
3. Keep the Android sort extensions (`sort()`, `toFirestoreOrder()`, `stringRes`) in androidMain
   as extension functions on the common enums:

```kotlin
// androidMain: ui/utils/enums/SortExtensions.kt
fun PlacesSortValues.sort(list: List<UserMapMarker>): List<UserMapMarker> { ... }
fun PlacesSortValues.toFirestoreOrder(): Pair<String, Query.Direction> { ... }
@get:StringRes val PlacesSortValues.stringRes: Int get() = when(this) { ... }
// Same for CatchesSortValues
```

4. Update `NotesPreferencesImpl` and UI files to use extensions instead of interface property
5. **Verification**: `./gradlew :shared:assembleDebug`

#### 12-pre-b: Move `BaseViewState` to commonMain

`BaseViewState<T>` is used by most ViewModels. Currently it uses `@Immutable` from
`androidx.compose.runtime` which is already in commonMain dependencies (`compose.runtime`).

1. Move `ui/viewstates/BaseViewState.kt` → `commonMain`
2. Update all ViewModel imports
3. **Verification**: `./gradlew :shared:assembleDebug`

#### 12-pre-c: Create `SnackbarManager` abstraction

Multiple ViewModels call `SnackbarManager.showMessage(R.string.*)`. For commonMain, create
an `expect`/`actual` or interface-based abstraction:

```kotlin
// commonMain: ui/SnackbarManager.kt
interface SnackbarEventSender {
    fun showMessage(messageKey: String)
}
```

Or keep `SnackbarManager` in androidMain and let VMs that use it stay in androidMain
(simpler approach — `UserCatchViewModel`, `UserPlaceViewModel`, `MapViewModel` stay).

**Recommended**: Keep VMs with `R.string` / `SnackbarManager` in androidMain for now,
migrate them together with Phase 13 (UI) when `R.string` gets replaced with CMP resources.

---

### Approach

Add `androidx.lifecycle:lifecycle-viewmodel` KMP artifact to commonMain:

```kotlin
// shared/build.gradle.kts
commonMain.dependencies {
    implementation(libs.lifecycle.viewmodel)  // KMP since 2.8+
}
```

This provides `ViewModel` + `viewModelScope` in commonMain. No `expect`/`actual` needed.

---

### Migration Tiers

#### Tier 1: No blockers — move directly to commonMain

These ViewModels depend only on interfaces/use cases already in commonMain.

| ViewModel | Dependencies | Action |
|-----------|-------------|--------|
| `StatisticsViewModel` | `GetCatchStatisticsUseCase` (commonMain) | Move directly; replace `Log.e` with `println` or common logger |
| `EditProfileViewModel` | `UserDatastore` (commonMain), `UserRepository` (commonMain) | Move directly |

**Steps for each**:
1. Move file from `androidMain` → `commonMain` (same package)
2. Replace `android.util.Log` with `println()` or a common logging abstraction
3. Verify imports resolve to commonMain types
4. Update Koin module registration if needed
5. **Verification**: `./gradlew :shared:assembleDebug`

---

#### Tier 2: Require minor abstraction — move with small changes

| ViewModel | Dependencies | Blocker | Resolution |
|-----------|-------------|---------|------------|
| `UserViewModel` | `UserRepository`, `UserDatastore`, `OfflineRepository`, `GetUserCatchesUseCase` | `findBestCatch()` / `findFavoritePlace()` from `ProfileUtils.kt` | Move the util functions to commonMain (pure Kotlin logic on domain entities) |
| `MainViewModel` | `UserRepository`, `SyncStatusManager` | `SyncStatusManager` is in androidMain | Create `SyncStatusProvider` interface in commonMain with `globalSyncState: StateFlow<SyncState>`. Impl stays in androidMain |

**Steps for `UserViewModel`**:
1. Move `findBestCatch()` and `findFavoritePlace()` from `ui/home/profile/ProfileUtils.kt` → commonMain
2. Move `UserViewModel` to commonMain
3. **Verification**: `./gradlew :shared:assembleDebug`

**Steps for `MainViewModel`**:
1. Create interface in commonMain:
```kotlin
// commonMain: domain/repository/SyncStatusProvider.kt
interface SyncStatusProvider {
    val globalSyncState: StateFlow<SyncState>
}
```
2. Make `SyncStatusManager` implement this interface
3. Move `MainViewModel` to commonMain, depend on `SyncStatusProvider`
4. Update Koin binding
5. **Verification**: `./gradlew :shared:assembleDebug`

---

#### Tier 3: Keep in androidMain — heavy platform dependencies

These ViewModels have deep Android dependencies that make them impractical to move
until Phase 13 (UI migration) addresses `R.string`, `SnackbarManager`, `Uri`, Google Maps, etc.

| ViewModel | Platform deps | Stays until |
|-----------|--------------|-------------|
| `MapViewModel` | Google Maps `LatLng`, `UserPreferences` (concrete Android class), `LocationManager`, `SnackbarManager`, `R.string` | Phase 13h (Map) |
| `LoginViewModel` | `FirebaseAuth` (Google SDK), `R.string` | Phase 13i (Login) |
| `WeatherViewModel` | `LocationManager` | Phase 13 |
| `UserCatchViewModel` | `android.net.Uri`, `R.string`, `SnackbarManager` | Phase 13e (Catch detail) |
| `UserPlaceViewModel` | `R.string`, `SnackbarManager`, paged `CatchesRepository` | Phase 13f (Place detail) |
| `UserCatchesViewModel` | `android.util.Log`, paged `CatchesRepository` | Phase 13d (Notes) |
| `UserPlacesViewModel` | `android.util.Log`, paged `MarkersRepositoryPaged` | Phase 13d (Notes) |
| `NewCatchMasterViewModel` | `android.net.Uri`, `java.util.Date`, `@Immutable`, multiple state classes | Phase 13g (New catch) |

---

### Koin DI Changes

After moving Tier 1 + 2 ViewModels:

```kotlin
// commonMain: di/CommonUseCasesModule.kt (rename to CommonModule.kt or add new module)
val commonViewModelsModule = module {
    viewModel { StatisticsViewModel(get()) }
    viewModel { EditProfileViewModel(get(), get()) }
    viewModel { UserViewModel(get(), get(), get(), get()) }
    viewModel { MainViewModel(get(), get()) }
}
```

```kotlin
// androidMain: di/KoinModules.kt
// Remove migrated VMs from androidMain module, keep Tier 3 VMs
```

**Note**: `viewModel { }` DSL requires `koin-core` 4.x (already in use). For commonMain,
use `org.koin.core.module.dsl.viewModel` from `koin-core`.

---

### Phase 12 Implementation Order

```
12-pre-a  Move sort enums + NotesPreferences to commonMain        (~4 files)
12-pre-b  Move BaseViewState to commonMain                        (~1 file)
12-pre-c  Decide SnackbarManager strategy                         (design decision)
12.1      Move StatisticsViewModel to commonMain                  (Tier 1 — simplest)
12.2      Move EditProfileViewModel to commonMain                 (Tier 1)
12.3      Move ProfileUtils + UserViewModel to commonMain         (Tier 2)
12.4      Create SyncStatusProvider + move MainViewModel          (Tier 2)
12.5      Update Koin modules                                     (DI cleanup)
12.6      Verify full build                                       (./gradlew :androidApp:assembleDebug)
```

**Est. files changed**: 12-15
**Difficulty**: Medium
**Result**: 4 ViewModels in commonMain, 8 remain in androidMain (moved with their screens in Phase 13)

---

## Phase 13: UI Layer — Compose Multiplatform

**Goal**: Migrate UI composables from Android Compose to JetBrains Compose Multiplatform.

This is the largest phase (~85 files). Proceed screen-by-screen.

### Prerequisites
- Phases 6-12 substantially complete
- Replace `R.string` with Compose Multiplatform resources (`composeResources/`)
- Replace Lottie with a KMP alternative or `expect`/`actual`

### Migration order (by dependency)

| Step | Screens | Blockers |
|------|---------|----------|
| 13a | Shared views (`Buttons`, `Cards`, `DefaultViews`, `AppBar`, `Counters`) | `R.string` → resources |
| 13b | Profile (`Profile`, `EditProfile`, `ProfileViews`) | Coil → Coil KMP |
| 13c | Settings (`SettingsScreen`, `AboutApp`) | Minimal |
| 13d | Notes/Lists (`Notes`, `UserCatchesScreen`, `UserPlacesScreen`, `StatisticsScreen`) | Paging Compose |
| 13e | Catch detail (`Catch`, `CatchScreenDialogs`) | Coil for photos |
| 13f | Place detail (`Place`, `PlaceViews`) | Google Maps embed |
| 13g | New Catch flow (`NewCatchMaster`, pages, weather icons) | Photo picker, camera |
| 13h | Map screen (`MapScreen`, `MapViews`, `NewPlaceDialog`, `MarkerInfoDialog`) | Google Maps → KMP maps |
| 13i | Login (`LoginScreen`) | KMPAuth (already multiplatform) |
| 13j | App shell (`FishingNotesApp`, `Home`, navigation) | Navigation Compose KMP |

### Key replacements

| Android | KMP Alternative |
|---------|----------------|
| `R.string.*` | `composeResources/values/strings.xml` + `stringResource()` |
| `R.drawable.*` | `composeResources/drawable/` |
| Lottie Compose | `compottie` or `expect`/`actual` |
| Coil | `io.coil-kt.coil3:coil-compose` (KMP since Coil 3) |
| Google Maps Compose | `expect`/`actual` with platform-specific map |
| Accompanist Permissions | Custom `expect`/`actual` permission handling |
| Accompanist Pager | Built-in `HorizontalPager` (Compose 1.5+) |
| AdMob views | `expect`/`actual`, keep ads Android-only |
| `android.net.Uri` photo picker | `expect`/`actual` photo picker |

### Verification
- `./gradlew :androidApp:assembleDebug`
- Full visual regression test on device

---

## Phase Summary

| Phase | Scope | Est. files | Status |
|-------|-------|-----------|--------|
| ~~0-5~~ | ~~Domain layer~~ | ~~49~~ | ~~Done~~ |
| ~~6~~ | ~~Datastore interfaces~~ | ~~4~~ | ~~Mostly done (NotesPreferences remaining)~~ |
| ~~7~~ | ~~Auth abstraction~~ | ~~4~~ | ~~Done (AuthRepository + FirebaseAuthRepository in commonMain)~~ |
| ~~8~~ | ~~Model utilities~~ | ~~3~~ | ~~Mostly done (SafeApiCall, RepositoryCollections in commonMain)~~ |
| ~~9~~ | ~~Retrofit → Ktor~~ | ~~6~~ | ~~Done~~ |
| ~~10~~ | ~~Room → Room KMP~~ | ~~12~~ | ~~Done~~ |
| ~~11~~ | ~~Firebase → gitlive~~ | ~~8~~ | ~~Done~~ |
| **12** | **ViewModels → commonMain** | **12-15** | **Next** |
| 13 | UI Composables → CMP | 85+ | Pending |

**Current focus**: Phase 12 (includes cleanup of remaining Phase 6/8 items)
