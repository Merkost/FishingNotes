# FishingNotes — Comprehensive Roadmap

> **Purpose**: Prioritized roadmap covering all bug fixes, improvements, and new features.
> Living document — update as items are completed or priorities shift.
>
> **Last updated**: 2026-03-02
> **Current branch**: `compose_migration` (Phase 7 prerequisites complete: weather staleness #40, Accompanist Pager migration #182, Coil 3.1.0 upgrade #113, Lottie strategy documented #112)

---

## Phase 1: Critical Bug Fixes (Ship-blocking)

Crashes, data corruption, and broken core functionality.

### 1.1 Crashes & Data Corruption

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 1 | ~~**`ContentState.fold()` calls `onAdded` for ALL branches**~~ | `ContentStateOld.kt` | ✅ Fixed — correct branching per type |
| 2 | ~~**`SaveUserMarkerNoteUseCase` crashes on edit**~~ | `SaveUserMarkerNoteUseCase.kt` | ✅ Fixed — uses `indexOfFirst` with null check |
| 3 | ~~**JSON deserialization crash in Room type converters**~~ | `Converters.kt` | ✅ Fixed — try-catch with empty list fallback |
| 4 | ~~**JSON deserialization crash in entity mappers**~~ | `EntityMappers.kt` | ✅ Fixed — try-catch with empty list fallback |
| 5 | ~~**Non-null assertion crash in navigation**~~ | `FishingNotesAppStateHolder.kt` | ✅ Fixed — safe call with fallback |
| 6 | ~~**Non-null assertion crash on exception**~~ | `FirebaseCatchesRepositoryImpl.kt` | ✅ Fixed — uses Result pattern |
| 7 | ~~**UserDatastore crash on corrupt JSON**~~ | `UserDatastoreImpl.kt` | ✅ Fixed — try-catch returns default User() |
| 8 | ~~**DataStore enum valueOf crash**~~ | `WeatherPreferencesImpl.kt`, `NotesPreferencesImpl.kt` | ✅ Fixed — `.catch()` handler with defaults |
| 9 | ~~**Unsafe cast in UserPlacesViewModel**~~ | `UserPlacesViewModel.kt` | ✅ Fixed — `filterIsInstance()` used |
| 10 | ~~**Missing database migration path**~~ | `FishingDatabase.kt` | ✅ Fixed — `AutoMigration(from = 1, to = 2)` added, `fallbackToDestructiveMigration` removed |

### 1.2 Silent Failures & Data Loss

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 11 | ~~**Firebase operations swallow exceptions silently**~~ | `FirebaseCatchesRepositoryImpl.kt` | ✅ Fixed — uses `Result<Unit>` pattern |
| 12 | ~~**`updateUserCatchPhotos` reports success on failure**~~ | `FirebaseCatchesRepositoryImpl.kt` | ✅ Fixed — uses `Result.failure` |
| 13 | ~~**`addNewUser` race condition**~~ | `FirebaseUserRepositoryImpl.kt` | ✅ Fixed — Firestore `runTransaction` for atomic get+set |
| 14 | ~~**Catch count drift**~~ | `FirebaseCatchesRepositoryImpl.kt` | ✅ Fixed — `batch { set + update }` for atomic catch+count operations |
| 15 | ~~**SyncWorker marks failed updates as SYNCED**~~ | `SyncWorker.kt` | ✅ Fixed — checks `isSuccess` before marking |
| 16 | ~~**SyncWorker duplicate operations on retry**~~ | `SyncWorker.kt` | ✅ Fixed — dedup check verifies entity sync status before processing |
| 17 | ~~**Snapshot listeners leak on error paths**~~ | `FirebaseCatchesRepositoryImpl.kt`, `FirebaseMarkersRepositoryImpl.kt` | ✅ Fixed — `.catch {}` on all snapshot Flows, try-catch on deserialization |
| 18 | ~~**`setUserListener` fails silently**~~ | `FirebaseUserRepositoryImpl.kt` | ✅ Fixed — outer try-catch with `printStackTrace()` for error visibility |

### 1.3 Security

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 19 | **Production API keys committed to repository** | `secrets.properties` | ⏳ Requires manual action: revoke keys, regenerate, verify `.gitignore`, clean git history |
| 20 | **Release keystore in repository** | `fishing.jks` | ⏳ Requires manual action: move to CI secrets, clean git history |
| 21 | ~~**Signing config missing passwords**~~ | `build.gradle.kts` | ✅ Fixed — reads from `local.properties` |
| 22 | **Missing certificate pinning** | Ktor/OkHttp config | ⏳ Deferred — cert pinning requires maintaining pin hashes that change on rotation; low risk since all HTTPS with TLS |
| 23 | ~~**`allowBackup` audit**~~ | `AndroidManifest.xml` | ✅ Verified — set to `false` |

---

## Phase 2: Data Layer Robustness

Fix sync logic, offline support, and data integrity.

### 2.1 Sync & Offline

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 24 | ~~**Race condition in `addNewCatch`**~~ | `SyncAwareCatchesRepository.kt`, `SyncAwareMarkersRepository.kt` | ✅ Fixed — removed connectivity pre-check; always try Firebase, catch exceptions and queue |
| 25 | ~~**No pending operation deduplication**~~ | `SyncAwareCatchesRepository.kt`, `SyncAwareMarkersRepository.kt` | ✅ Fixed — `deleteByEntity` before insert inside `withTransaction` |
| 26 | **Missing conflict resolution** | All sync-aware repositories | ⏳ Deferred to Phase 13 (CMP migration) — `ConflictResolutionDialog` exists but UI wiring belongs in screen migration |
| 27 | ~~**No transaction consistency across local/remote**~~ | All sync-aware repositories | ✅ Fixed — `db.withTransaction` used for all local+queue operations |
| 28 | ~~**SyncWorker missing timeout protection**~~ | `SyncWorker.kt` | ✅ Fixed — `withTimeoutOrNull(30_000L)` per operation |
| 29 | **Missing pending operations lock during sync** | `SyncWorker.kt` | ✅ Acceptable — Room transactions prevent concurrent modification; SyncWorker runs as unique work (no parallel instances); new ops queued during sync are picked up in next run |
| 30 | ~~**`FirebaseOfflineRepository` error handling**~~ | `FirebaseOfflineRepositoryImpl.kt` | ✅ Fixed — `mapNotNull` with try-catch to skip corrupt documents |
| 31 | ~~**Show sync status indicator to user**~~ | UI layer | ✅ Fixed — `SyncStatusViews` fully implemented with badges and indicators |

### 2.2 Database

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 32 | ~~**Missing index on `CatchEntity.date`**~~ | `CatchEntity.kt` | ✅ Fixed — composite `Index("userId", "date")` |
| 33 | ~~**Missing composite index on MarkerEntity**~~ | `MarkerEntity.kt` | ✅ Fixed — composite `Index("userId", "dateOfCreation")` |
| 34 | ~~**Missing marker color in entity mapper**~~ | `EntityMappers.kt` | ✅ Fixed — mapped both directions |
| 35 | **No data validation on deserialization** | All mappers | ✅ Partially fixed — corrupt JSON handled by #3, #4, #30. Field-level validation (negative amounts, future dates) deferred to Phase 4 (input validation) |

### 2.3 Network

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 36 | ~~**Missing request timeout on Ktor clients**~~ | `WeatherRepositoryKtorImpl.kt`, etc. | ✅ Fixed — 10s connect, 30s request timeout configured |
| 37 | ~~**No retry logic for transient failures**~~ | `SafeApiCall.kt` | ✅ Fixed — retry with exponential backoff (3 attempts: initial + 2 retries, 1s/2s delays) |
| 38 | ~~**Hardcoded API base URLs**~~ | `WeatherRepositoryKtorImpl.kt`, `FreeWeatherRepositoryKtorImpl.kt`, `SolunarRepositoryKtorImpl.kt` | ✅ Fixed — constructor-injectable `baseUrl` with defaults |
| 39 | ~~**API keys not validated at injection**~~ | `WeatherRepositoryKtorImpl.kt`, `FreeWeatherRepositoryKtorImpl.kt` | ✅ Fixed — `require(isNotBlank)` in init |
| 40 | ~~**`CachedWeatherRepository` returns stale data silently**~~ | `CachedWeatherRepository.kt` | ✅ Fixed — `getWeatherWithMeta()` returns `WeatherResult` with `WeatherSource` enum (FRESH/CACHED/STALE_FALLBACK); WeatherScreen shows "Cached data" label for stale fallback |

---

## Phase 3: Testing Foundation

| # | Task | Scope | Status |
|---|------|-------|--------|
| 41 | ~~Unit tests for all use cases (15+)~~ | `domain/use_cases/` | ✅ Done — 14/14 use cases covered (100%) |
| 42 | Unit tests for core ViewModels | `viewmodels/` | ⏳ Partially done — `UserViewModel`, `StatisticsViewModel`, `MainViewModel`, `EditProfileViewModel` tested. Remaining VMs (`WeatherViewModel`, `LoginViewModel`, `MapViewModel`, etc.) are in androidMain with heavy platform deps (FirebaseAuth, Google Maps, LocationManager) — need Robolectric or defer to Phase 13 migration |
| 43 | ~~Unit tests for entity mappers~~ | `model/mappers/`, `model/datasource/local/mapper/` | ✅ Done — `ConvertersTest`, `EntityMappersTest`, `MarkerNoteMapperTest` |
| 44 | Integration tests for Firebase repositories | `model/datasource/firebase/` | ⏳ Deferred — requires Firebase Emulator Suite setup |
| 45 | Integration tests for sync logic | `SyncWorker.kt`, sync-aware repos | ⏳ Deferred — complex setup with emulators and Workers |
| 46 | Compose UI tests for critical flows | `ui/home/` | ⏳ Deferred to Phase 13 (CMP migration) — screens moving to commonMain |
| 47 | ~~Set up CI pipeline (GitHub Actions)~~ | Project root | ✅ Done — workflow configured |
| 48 | ~~Set up Detekt/ktlint static analysis~~ | Project root | ✅ Done — Detekt configured with custom rules |

---

## Phase 4: Core UX Improvements

### 4.1 Input Validation

| # | Task | Details | Status |
|---|------|---------|--------|
| 49 | ~~Validate fish weight range (0–500 kg)~~ | `FishAmountAndWeightViewItem` — `isError` + `weight_out_of_range` | ✅ Done |
| 50 | ~~Validate fish amount range (0–9999)~~ | Same component — `amount_out_of_range`, +/- buttons enforce limits | ✅ Done |
| 51 | ~~Prevent future dates in catch date picker~~ | `clampDate()` used in date pickers | ✅ Done |
| 52 | ~~Trim whitespace, enforce max length on place names~~ | `MAX_PLACE_NAME_LENGTH` reduced to 50, char counter + trim-on-save | ✅ Done |
| 53 | ~~Validate latitude/longitude ranges~~ | `ValidationUtils.isCoordinateValid()` | ✅ Done |
| 54 | ~~Validate note title/description not empty~~ | `isValidNoteTitle()` added, title TextField in `EditNoteDialog` | ✅ Done |

### 4.2 Missing UI States

| # | Task | Screen(s) | Status |
|---|------|-----------|--------|
| 55 | ~~Empty state illustrations for catches/places lists~~ | `NoContentView` with icons in list screens | ✅ Done |
| 56 | ~~Error state with retry button on weather fetch~~ | `ErrorView` + retry button in Weather, Places, Catches screens | ✅ Done |
| 57 | ~~Loading skeleton/shimmer for catch and place lists~~ | `placeholder()` with fade in all list screens | ✅ Done |
| 58 | ~~Success feedback after saving a catch~~ | `SnackbarManager.showMessage(R.string.catch_added_successfully)` | ✅ Done |
| 59 | ~~Confirmation dialogs before all delete actions~~ | `DeleteCatchDialog`, delete place/note dialogs all exist | ✅ Done |
| 60 | ~~Pull-to-refresh on catches, places, weather~~ | `PullToRefreshBox` in list and weather screens | ✅ Done |
| 61 | ~~Show loading indicator during login~~ | Loading indicator in LoginScreen | ✅ Done |
| 62 | ~~Unify error display mechanism~~ | LoginScreen uses SnackbarManager; CatchScreenDialogs uses SnackbarManager instead of Toast | ✅ Done |

### 4.3 Photo Improvements

| # | Task | Details | Status |
|---|------|---------|--------|
| 63 | ~~Pinch-to-zoom and rotation on photo viewer~~ | Implemented in `PhotoViews` | ✅ Done |
| 64 | ~~Photo upload progress indicator per photo~~ | Progress indicator on photo upload | ✅ Done |
| 65 | ~~Camera capture (not just gallery pick)~~ | `TakePicture()` + `FileProvider`; camera no longer gated behind internet check | ✅ Done |
| 66 | ~~Thumbnail generation for faster list loading~~ | Implemented in photo grid views | ✅ Done |

### 4.4 Accessibility

| # | Task | Details | Status |
|---|------|---------|--------|
| 67 | ~~Add `contentDescription` to Icon/Image composables~~ | Decorative icons with `null` contentDescription alongside text labels are correct per guidelines; FAB icons use `item.text` | ✅ Acceptable |
| 68 | Add semantic grouping modifiers for screen readers | Cross-cutting concern | ⏳ Deferred to Phase 13 (CMP migration) |
| 69 | ~~Ensure minimum 48dp touch targets~~ | Photo delete buttons, map buttons, PlaceViews icon button updated to 48dp | ✅ Done |
| 70 | Support dynamic text sizing | Fixed login card + weather Canvas are low-priority | ⏳ Deferred to Phase 13 (CMP migration) |

---

## Phase 5: Architecture & Code Quality

### 5.1 ViewModel Issues

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 71 | ~~**Memory leak: infinite `collect` without lifecycle**~~ | `UserViewModel.kt` | ✅ Fixed — uses `collectLatest` |
| 72 | ~~**Typo in property name**~~ | `NewCatchMasterViewModel.kt` | ✅ Fixed — corrected to `fishAndWeightState` |
| 73 | ~~**Hardcoded error string**~~ | `LoginViewModel.kt` | ✅ Fixed — uses companion `const` |
| 74 | ~~**`println()` for logging in production code**~~ | `StatisticsViewModel.kt`, Firebase repos | ✅ Fixed — removed from production code |
| 75 | ~~**MutableState in ViewModel**~~ | `MapViewModel.kt` | ✅ Fixed — uses `MutableStateFlow` |
| 76 | ~~**SnackbarManager called from ViewModel**~~ | `UserCatchViewModel.kt` | ✅ Documented as intentional — singleton pattern is acceptable |
| 77 | ~~**Hardcoded place name**~~ | `MapViewModel.kt` | ✅ Fixed — uses `R.string` resource |

### 5.2 Domain Layer Cleanup

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 78 | ~~**Mutable domain entities**~~ | `RawUserCatch.kt`, `RawMapMarker.kt`, `NewCatchWeather.kt` | ✅ Fixed — all properties are `val` |
| 79 | ~~**Inconsistent repository return types**~~ | Multiple repository interfaces | ✅ Fixed — standardized to `Result<T>` (Phase 5C) |
| 80 | ~~**Suspicious `Flow<Result<Nothing?>>` return**~~ | `CatchesRepositoryUpdate.kt` | ✅ Fixed — now `Result<Unit>` |
| 81 | ~~**`getAllUserCatchesList` uses `collectionGroup`**~~ | `FirebaseCatchesRepositoryImpl.kt` | ✅ Documented — comment explains rationale (cross-marker queries) |
| 82 | ~~**O(n²) catch list management**~~ | `GetUserCatchesUseCase.kt` | ✅ Fixed — uses Set for O(n) lookups |
| 83 | ~~**Empty `Throwable()` in error emissions**~~ | `GetNewCatchWeatherUseCase.kt` | ✅ Fixed — descriptive error message added |
| 84 | ~~**`logoutCurrentUser()` returns `Flow<Boolean>`**~~ | `UserRepository.kt` | ✅ Fixed — now `suspend fun` returning `Unit` |
| 85 | ~~**Add `@Stable`/`@Immutable` to Compose state classes**~~ | `domain/entity/` | ✅ Fixed — applied to state classes |

### 5.3 DI & Initialization

| # | Issue | File(s) | Status |
|---|-------|---------|--------|
| 86 | ~~**Missing BillingClient listener binding**~~ | `AboutApp.kt`, `KoinModules.kt` | ✅ Fixed — BillingClient removed from Koin, created locally in AboutApp with DisposableEffect cleanup |
| 87 | ~~**Duplicate `NotesPreferencesImpl` binding**~~ | `di/KoinModules.kt:56` | ✅ False alarm — only one binding exists |
| 88 | ~~**Deprecated `launchWhenStarted`**~~ | `MainActivity.kt` | ✅ Fixed — uses `repeatOnLifecycle` |
| 89 | ~~**Debug log left in theme**~~ | `ui/theme/Theme.kt` | ✅ Fixed — removed |
| 90 | ~~**Static `lastCoordinates` StateFlow**~~ | `LocationManagerImpl.kt` | ✅ Fixed — removed unused field, write, and commented-out code |

### 5.4 Tech Debt

| # | Task | Status |
|---|------|--------|
| 91 | ~~Remove all dead/commented-out code~~ | ✅ Fixed — cleaned `MapViewModel.kt` (removed `updateCurrentPlace()`, commented blocks, dead calls) |
| 92 | ~~Fix moon phase logic error~~ | ✅ Verified correct — all ranges covered |
| 93 | Implement structured logging (Timber or KMP alternative) | ⏳ Deferred to Phase 7 (CMP migration) — cross-cutting, low urgency |
| 94 | ~~Standardize spacing/padding to theme constants~~ | ✅ Dropped — hardcoded values are intentional per screen |
| 95 | ~~Replace hardcoded gradient colors with theme tokens~~ | ✅ Dropped — decorative gradients, intentional |
| 96 | ~~Fix `InitColorScheme` transparent fallback~~ | ✅ Fixed — uses `BlueLightColorScheme` |
| 97 | ~~Fix malformed comment~~ | ✅ Verified — line is valid code, not malformed |
| 98 | ~~Add `@Preview` annotations to reusable composables~~ | ✅ Dropped — low priority |
| 99 | ~~Remove `@SuppressLint("RestrictedApi")`~~ | ✅ Justified — inline comment explains Navigation pre-2.8 requirement |
| 100 | ~~Audit ProGuard rules~~ | ✅ Done — rules for maps, serialization, Firebase configured |
| 101 | ~~Mixed Russian string in English strings.xml~~ | ✅ Verified — not found in current strings.xml |

---

## Phase 6: Performance

| # | Task | Details | Status |
|---|------|---------|--------|
| 102 | ~~Fix `getRefreshKey()` in CatchesPagingSource~~ | `CatchesPagingSource.kt` — proper anchor-based refresh key | ✅ Fixed — preserves scroll position on config changes |
| 103 | ~~Fix `getRefreshKey()` in MarkersPagingSource~~ | `MarkersPagingSource.kt` — same pattern | ✅ Fixed |
| 104 | Implement cache-first read pattern | All repositories — read from Room first, background-sync with Firebase | ⏳ Deferred — large architectural change |
| 105 | Audit `remember`/`mutableStateOf` for unnecessary recompositions | All Compose screens | ⏳ Deferred — exploratory recomposition audit |
| 106 | ~~Lazy-load photos in catch detail~~ | `PhotoViews.kt` | ✅ Already done — Coil lazy-loads by default + Compose `LazyRow`/`LazyVerticalGrid` only compose visible items |
| 107 | ~~Consolidate Firebase snapshot listeners~~ | `FirebaseCatchesRepositoryImpl.kt`, `FirebaseMarkersRepositoryImpl.kt` | ✅ Fixed — shared snapshot flows via `shareIn(WhileSubscribed)` eliminate duplicate listeners |
| 108 | Add Baseline Profiles for startup | `androidApp/` module | ⏳ Deferred — requires instrumentation setup |
| 109 | Replace Firestore `collectionGroup` queries | `FirebaseCatchesRepositoryImpl.kt` | ⏳ Deferred — `collectionGroup` is intentional for cross-marker queries (documented in Phase 5) |

---

## Phase 7: CMP Migration Completion (Phase 13)

The largest remaining migration effort — ~85+ UI files from Android Compose to JetBrains Compose Multiplatform. See `CMP_MIGRATION.md` for detailed plan.

### 7.1 Prerequisites

| # | Task | Details |
|---|------|---------|
| 110 | Replace all `R.string` with CMP `composeResources/values/strings.xml` | ~50+ files reference `R.string` |
| 111 | Replace all `R.drawable` with CMP `composeResources/drawable/` | ~20+ files reference `R.drawable` |
| 112 | Replace Lottie with KMP alternative (`compottie` or expect/actual) | Strategy documented: `expect`/`actual` wrapper (`LottieView` in commonMain, Airbnb `LottieAnimation` actual on Android). 10 files, 21 JSON assets in `androidMain/res/raw/`. Deferred to per-screen CMP migration. |
| 113 | ~~Migrate Coil 2.x to Coil 3.x (KMP-first)~~ | ✅ Done — Coil 3.1.0 (`io.coil-kt.coil3`), 4 files migrated (`coil.*` → `coil3.*`), `coil-network-okhttp` added |
| 114 | Create expect/actual for Google Maps | Map screen — platform-specific map widget |
| 115 | Create expect/actual for photo picker | New catch flow — platform-specific gallery/camera |
| 116 | Create expect/actual for permissions | Accompanist → custom KMP permission handling |

### 7.2 Screen Migration Order

| # | Step | Screens | Key Blockers |
|---|------|---------|--------------|
| 117 | 13a: Shared views | `Buttons`, `Cards`, `DefaultViews`, `AppBar`, `Counters` | `R.string` → CMP resources |
| 118 | 13b: Profile | `Profile`, `EditProfile`, `ProfileViews` | Coil → Coil 3 KMP |
| 119 | 13c: Settings | `SettingsScreen`, `AboutApp` | Minimal |
| 120 | 13d: Notes/Lists | `Notes`, `UserCatchesScreen`, `UserPlacesScreen`, `StatisticsScreen` | Paging Compose |
| 121 | 13e: Catch detail | `Catch`, `CatchScreenDialogs` | Coil for photos |
| 122 | 13f: Place detail | `Place`, `PlaceViews` | Google Maps embed |
| 123 | 13g: New Catch flow | `NewCatchMaster`, pages, weather icons | Photo picker, camera |
| 124 | 13h: Map screen | `MapScreen`, `MapViews`, `NewPlaceDialog`, `MarkerInfoDialog` | Google Maps → KMP |
| 125 | 13i: Login | `LoginScreen` | KMPAuth (already multiplatform) |
| 126 | 13j: App shell | `FishingNotesApp`, `Home`, navigation | Navigation Compose KMP |

### 7.3 Remaining Tier 3 ViewModel Migration

| # | ViewModel | Platform Deps | Migrates With |
|---|-----------|---------------|---------------|
| 127 | `MapViewModel` | Google Maps `LatLng`, `LocationManager`, `SnackbarManager`, `R.string` | Step 13h |
| 128 | `LoginViewModel` | `FirebaseAuth` (Google SDK), `R.string` | Step 13i |
| 129 | `WeatherViewModel` | `LocationManager` | Step 13c |
| 130 | `UserCatchViewModel` | `android.net.Uri`, `R.string`, `SnackbarManager` | Step 13e |
| 131 | `UserPlaceViewModel` | `R.string`, `SnackbarManager`, paged `CatchesRepository` | Step 13f |
| 132 | `UserCatchesViewModel` | paged `CatchesRepository` | Step 13d |
| 133 | `UserPlacesViewModel` | paged `MarkersRepositoryPaged` | Step 13d |
| 134 | `NewCatchMasterViewModel` | `android.net.Uri`, `java.util.Date`, multiple state classes | Step 13g |

---

## Phase 8: New Features — Catch Experience

| # | Feature | Description |
|---|---------|-------------|
| 135 | **Catch Statistics Dashboard** | Charts: catches over time, top species, weight distribution, best months, activity by weather. Use Vico chart library. |
| 136 | **Fish Species Database** | Pre-populated species list with images. Auto-complete on catch creation. Allow custom species. |
| 137 | **Catch Comparison** | Side-by-side compare two catches (weight, conditions, location). |
| 138 | **Personal Records Tracking** | Auto-detect personal bests: heaviest fish, most in a day, longest streak. Badge/achievement system. |
| 139 | **Catch Calendar View** | Monthly calendar with color-coded catch days. Tap day to see catches. |
| 140 | **Fishing Trip / Session Grouping** | Group catches into "trips" with start/end time, total stats, trip notes. |
| 141 | **Quick Catch Mode** | One-tap logging: auto-fill location, date, weather. Just enter species + weight. |
| 142 | **Catch Tags / Labels** | Custom tags ("tournament", "release", "keeper") for filtering and search. |
| 143 | **Voice Notes on Catches** | Audio memo attached to a catch for hands-free notes. |

---

## Phase 9: New Features — Map & Location

| # | Feature | Description |
|---|---------|-------------|
| 144 | **Heatmap Layer** | Catch density overlay on map. Identify hot spots at a glance. |
| 145 | **Cluster Markers** | Group nearby markers into clusters that expand on zoom. Performance improvement for many places. |
| 146 | **Route/Trail Tracking** | Record GPS trail during a fishing trip. Show distance and time. |
| 147 | **Offline Map Areas** | Download specific map regions for remote fishing. |
| 148 | **Place Categories** | Categorize places (lake, river, pond, sea, reservoir). Filter by category. |
| 149 | **Place Rating System** | Rate places 1–5 stars. Sort and filter by rating. |
| 150 | **Nearby Places Discovery** | Show other users' public places within radius. |
| 151 | **Navigation Integration** | Deep link to Google Maps / Waze for turn-by-turn directions. |
| 152 | **Place Photos** | Attach photos to places (not just catches). |
| 153 | **Implement Notes filters** | `Notes.kt:141` (existing TODO) — filter by date, species, weight, place. |

---

## Phase 10: New Features — Weather & Planning

| # | Feature | Description |
|---|---------|-------------|
| 154 | **Best Time to Fish Predictor** | Combine solunar, weather, and personal history to suggest optimal windows. |
| 155 | **Weather Alerts** | Push notifications for ideal conditions at saved places. |
| 156 | **Tide Data** | Tide charts and predictions for coastal spots. |
| 157 | **Water Temperature** | Integrate water temp data where available. |
| 158 | **Multi-Day Trip Planner** | Plan trip: choose dates, weather forecast, suggest best spots. |
| 159 | **Barometric Pressure Trends** | Pressure rising/falling trends (strong fishing indicator). |
| 160 | **Wind Map Overlay** | Visual wind direction/speed overlay on map. |

---

## Phase 11: New Features — Social & Sharing

| # | Feature | Description |
|---|---------|-------------|
| 161 | **Public Feed / Timeline** | Scrollable feed of public catches. Like and comment. |
| 162 | **Follow Other Anglers** | See followed users' public catches in feed. |
| 163 | **Fishing Challenges / Tournaments** | Weekly/monthly challenges with leaderboard. |
| 164 | **Catch of the Day** | Daily featured catch voted by community. |
| 165 | **Share to Social Media** | Generate shareable catch cards (image + stats overlay). |
| 166 | **Group / Club Support** | Fishing clubs with shared places, group stats, events. |
| 167 | **Spot Reviews** | Written reviews on public places. |
| 168 | **Direct Messaging** | Chat between users in same clubs or sharing places. |

---

## Phase 12: Monetization & Growth

| # | Feature | Description |
|---|---------|-------------|
| 169 | **Premium Subscription** | Remove ads, advanced stats, unlimited photos, priority weather. |
| 170 | **Pro Weather** | Hourly forecasts, 14-day extended, historical weather at spots. |
| 171 | **Export Data** | Export catches to CSV/PDF for personal records or tournaments. |
| 172 | **Home Screen Widgets** | Today's fish activity, weather at favorite spot, recent catches. |
| 173 | **Wear OS Companion** | Quick catch logging from smartwatch. Fish activity on wrist. |
| 174 | **iOS Port** | Expand to iOS leveraging completed KMP migration. |
| 175 | **Localization Expansion** | Spanish, Portuguese, German, Finnish, Norwegian. |
| 176 | **Complete Google Sign-In button** | `GoogleButton.kt:22` (existing TODO) |
| 177 | **Ad integration** | `CatchScreenDialogs.kt:94` (existing FIXME) |
| 178 | **Hidden place setting preference** | `NotesViews.kt:205` (existing TODO) |

---

## Phase 13: Android Platform Polish

| # | Task | Details |
|---|------|---------|
| 179 | ~~**Missing `WRITE_EXTERNAL_STORAGE` for Android 11-12**~~ | ✅ Not needed — app targets API 36, uses content URIs + `cacheDir`. Scoped storage handles this. |
| 180 | ~~**Fix navigation backstack on login success**~~ | ✅ Already correct — `popUpTo(0) { inclusive = true }` at `LoginScreen.kt:58-62`. |
| 181 | ~~**Fix deprecated `getFileFromUri()`**~~ | ✅ Already done — `PhotoUtils.kt` uses modern `fileFromContentUri()`. No deprecated usage found. |
| 182 | ~~**Migrate from Accompanist Pager**~~ | ✅ Fixed — `Place.kt` and `PlaceViews.kt` migrated to `androidx.compose.foundation.pager`; `accompanist-pager` and `accompanist-pagerIndicators` dependencies removed. |
| 183 | **Add Baseline Profiles** | Measure and optimize startup + critical user journeys. |
| 184 | ~~**Firebase batch writes**~~ | ✅ Already done in Phase 1 (#14) — `batch { set + update }` used for atomic catch+count operations. |

---

## Priority Summary

| Priority | Phase | Focus | Items | Progress |
|----------|-------|-------|-------|----------|
| **Critical** | 1 | Crashes, data corruption, security | #1–23 | 20/23 done (87%) — #19, #20 require manual key rotation; #22 deferred |
| **High** | 2–3 | Data robustness, testing foundation | #24–48 | 22/25 done (88%) — #26 deferred to Ph13; #42 partial; #44-46 deferred |
| **High** | 4 | Core UX: validation, states, accessibility | #49–70 | 20/22 done (91%) — #68, #70 deferred to Ph13 |
| **Medium** | 5–6 | Architecture cleanup, performance | #71–109 | 34/39 done (87%) — #93 deferred to Ph7; #104, #105, #108, #109 deferred |
| **Medium** | 7 | CMP migration Phase 13 | #110–134 | Prerequisites: #113 done, #112 strategy documented |
| **Medium** | 8–9 | New features: catches, map | #135–153 | Not started |
| **Lower** | 10–11 | Weather tools, social features | #154–168 | Not started |
| **Lower** | 12–13 | Monetization, platform polish | #169–184 | 5/16 done — #179-182, #184 resolved |

---

## Quick Reference: Remaining High-Priority Items

| File | Issues | Priority |
|------|--------|----------|
| `secrets.properties` | #19 — API keys in git history | Critical/Security (manual) |
| `fishing.jks` | #20 — Keystore in git history | Critical/Security (manual) |
| ~~`CachedWeatherRepository.kt`~~ | ~~#40 — Stale data without indicator~~ | ✅ Fixed |
