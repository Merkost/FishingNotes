# FishingNotes — Comprehensive Roadmap

> **Purpose**: Prioritized roadmap covering all bug fixes, improvements, and new features.
> Living document — update as items are completed or priorities shift.
>
> **Last updated**: 2026-03-01
> **Current branch**: `compose_migration` (Phase 12 complete, Phase 13 next)

---

## Phase 1: Critical Bug Fixes (Ship-blocking)

Crashes, data corruption, and broken core functionality.

### 1.1 Crashes & Data Corruption

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 1 | **`ContentState.fold()` calls `onAdded` for ALL branches** | `domain/entity/common/ContentStateOld.kt:33-35` | DELETED and MODIFIED items treated as ADDED. Data corruption in catch/marker lists. Fix: return correct callback per branch. |
| 2 | **`SaveUserMarkerNoteUseCase` crashes on edit** | `domain/use_cases/notes/SaveUserMarkerNoteUseCase.kt:29-30` | `indexOf(find { ... })` returns -1 when note not found, `set(-1, ...)` throws `IndexOutOfBoundsException`. Fix: check null/index before set. |
| 3 | **JSON deserialization crash in Room type converters** | `model/datasource/local/converter/Converters.kt:11-28` | `Json.decodeFromString()` with no try-catch. Corrupt JSON in Room crashes the entire entity read. Fix: wrap in try-catch with empty list fallback. |
| 4 | **JSON deserialization crash in entity mappers** | `model/datasource/local/mapper/EntityMappers.kt:45,99` | Same as above — `decodeFromString` on `downloadPhotoLinks` and `notes` fields. One corrupt record crashes the entire list load. |
| 5 | **Non-null assertion crash in navigation** | `ui/FishingNotesAppStateHolder.kt` | `graph.findNode(graph.startDestinationId)!!` — replace with safe call + fallback. |
| 6 | **Non-null assertion crash on exception** | `model/datasource/firebase/FirebaseCatchesRepositoryImpl.kt:164` | `it.exception!!` — exception can be null. |
| 7 | **UserDatastore crash on corrupt JSON** | `model/datastore/impl/UserDatastoreImpl.kt:27-30` | `Json.decodeFromString<User>(it)` inside Flow with no try-catch. Corrupted DataStore file crashes app permanently. |
| 8 | **DataStore enum valueOf crash** | `model/datastore/impl/WeatherPreferencesImpl.kt:30-53`, `NotesPreferencesImpl.kt:27-39` | `.catch {}` only handles `IllegalArgumentException`, not all exceptions. Flow silently stops on unexpected errors. |
| 9 | **Unsafe cast in UserPlacesViewModel** | `ui/viewmodels/UserPlacesViewModel.kt:53` | `as List<UserMapMarker>` — unsafe cast without verification. Use `filterIsInstance<>()`. |
| 10 | **Missing database migration path** | `model/datasource/local/FishingDatabase.kt:25` | Version hardcoded to 1 with no migration strategy. Any schema change crashes existing installations. Implement Room auto-migrations. |

### 1.2 Silent Failures & Data Loss

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 11 | **Firebase operations swallow exceptions silently** | `FirebaseCatchesRepositoryImpl.kt:100,125,149,158` | Exceptions caught with `println()` — no propagation, no user notification, no retry. Catches/markers silently fail to save/delete. |
| 12 | **`updateUserCatchPhotos` reports success on failure** | `FirebaseCatchesRepositoryImpl.kt:113-130` | Exception branch emits `Progress.Complete` instead of `Progress.Error`. Photos silently lost. |
| 13 | **`addNewUser` race condition** | `FirebaseUserRepositoryImpl.kt:48-78` | Check-then-write without atomicity. Uses `tryEmit()` (lines 60,66,69,74) which silently drops events on buffer overflow. |
| 14 | **Catch count drift** | `FirebaseCatchesRepositoryImpl.kt:86-88,144-160` | `incrementNumOfCatches`/`decrementNumOfCatches` run after the main operation with separate error handling. Failures leave counts permanently out of sync. |
| 15 | **SyncWorker marks failed updates as SYNCED** | `model/datasource/local/sync/SyncWorker.kt:103-118` | `updateUserCatch()` doesn't return Result; code always marks `SYNCED` regardless of actual outcome. |
| 16 | **SyncWorker duplicate operations on retry** | `SyncWorker.kt:48-80` | Partial batch failure causes entire worker retry, re-processing already-succeeded operations. No idempotency. |
| 17 | **Snapshot listeners leak on error paths** | `FirebaseCatchesRepositoryImpl.kt`, `FirebaseMarkersRepositoryImpl.kt:26-40` | No cleanup in catch blocks. Firebase listeners remain registered after errors. |
| 18 | **`setUserListener` fails silently** | `FirebaseUserRepositoryImpl.kt:80-91` | Returns `Unit` — exception caught but not re-thrown. Caller never knows listener setup failed. |

### 1.3 Security

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 19 | **Production API keys committed to repository** | `secrets.properties` | `RAPIDAPI_KEY`, `OPENWEATHER_KEY`, `MAPS_API_KEY` — real keys visible to anyone cloning the repo. **Revoke immediately** and regenerate. Add `secrets.properties` to `.gitignore`. |
| 20 | **Release keystore in repository** | `fishing.jks` | Signing key should be in CI secrets, not version control. |
| 21 | **Signing config missing passwords** | `build.gradle.kts:30-32` | Release builds will fail — passwords not configured. |
| 22 | **Missing certificate pinning** | Ktor/OkHttp config | Weather and solunar API calls lack certificate pinning — vulnerable to MITM. |
| 23 | **`allowBackup` audit** | `AndroidManifest.xml:20` | Currently `false` (good). Verify `fullBackupContent` rules if this changes. |

---

## Phase 2: Data Layer Robustness

Fix sync logic, offline support, and data integrity.

### 2.1 Sync & Offline

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 24 | **Race condition in `addNewCatch`** | `SyncAwareCatchesRepository.kt:165-209` | Connectivity check (line 174) and Firebase send (line 176) are non-atomic. Network change between them causes double-queueing. Add idempotency keys. |
| 25 | **No pending operation deduplication** | `SyncAwareCatchesRepository.kt`, `SyncAwareMarkersRepository.kt` | Rapid save clicks queue duplicate operations. Query for existing pending op before inserting. |
| 26 | **Missing conflict resolution** | All sync-aware repositories | `SyncStatus.CONFLICT` is set but never resolvable by the user. Implement last-write-wins or manual merge UI. |
| 27 | **No transaction consistency across local/remote** | All sync-aware repositories | Room insert succeeds but Firebase fails — crash before pending-op queue leaves inconsistent state. Use Room transactions or write-ahead journaling. |
| 28 | **SyncWorker missing timeout protection** | `SyncWorker.kt:48-80` | No per-operation timeout. Long-running ops cause Android to kill the Worker mid-batch. Process in batches with timeout checks. |
| 29 | **Missing pending operations lock during sync** | `SyncWorker.kt` + sync repositories | New ops can be queued during sync, creating races. Mark ops as "in-progress" during processing. |
| 30 | **`FirebaseOfflineRepository` doesn't handle disable/enable failure** | `FirebaseOfflineRepositoryImpl.kt:19-39` | No try-catch-finally — `enableNetwork()` may not run if collection access fails. |
| 31 | **Show sync status indicator to user** | UI layer | Users have no visibility into pending/synced/conflict state. Add sync badge to catch/marker items. |

### 2.2 Database

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 32 | **Missing index on `CatchEntity.date`** | `model/datasource/local/entity/CatchEntity.kt:9-20` | DAO queries sort by `date DESC` (CatchDao.kt:15,18) — O(n) scan without index. Add `Index("date")` or composite `Index("userId", "date")`. |
| 33 | **Missing composite index on MarkerEntity** | `model/datasource/local/entity/MarkerEntity.kt:8-11` | Only `Index("userId")` but queries also order by `dateOfCreation`. Add composite `Index("userId", "dateOfCreation")`. |
| 34 | **Missing marker color in entity mapper** | `model/datasource/local/mapper/EntityMappers.kt:79-115` | `MarkerEntity.toDomain()` does not map `markerColor` — colors lost on local cache round-trip. |
| 35 | **No data validation on deserialization** | All mappers, type converters | Negative `fishAmount`, future dates, empty `userId`, out-of-range coordinates can persist. Add validation in factory methods. |

### 2.3 Network

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 36 | **Missing request timeout on Ktor clients** | `WeatherRepositoryKtorImpl.kt`, `FreeWeatherRepositoryKtorImpl.kt`, `SolunarRepositoryKtorImpl.kt` | No `HttpTimeout` plugin configured. Requests can hang indefinitely. Add 30s request / 10s connect timeout. |
| 37 | **No retry logic for transient failures** | All Ktor repositories | Single attempt — 5xx errors and timeouts cause immediate failure. Add retry with exponential backoff. |
| 38 | **Hardcoded API base URLs** | `WeatherRepositoryKtorImpl.kt:26`, `FreeWeatherRepositoryKtorImpl.kt:26`, `SolunarRepositoryKtorImpl.kt:23` | Cannot switch environments. Inject via constructor or BuildConfig. |
| 39 | **API keys not validated at injection** | `WeatherRepositoryKtorImpl.kt:19`, `FreeWeatherRepositoryKtorImpl.kt:20` | Empty keys produce cryptic API errors at runtime. Add `require(key.isNotEmpty())` in constructor. |
| 40 | **`CachedWeatherRepository` returns stale data silently** | `CachedWeatherRepository.kt:53-57` | On fetch failure, returns cached data without staleness indicator. Add metadata flag or emit warning. |

---

## Phase 3: Testing Foundation

Current test coverage is effectively 0%. Build a safety net before features.

| # | Task | Scope | Details |
|---|------|-------|---------|
| 41 | Unit tests for all use cases (15+) | `domain/use_cases/` | Focus on validation, error paths, edge cases. Priority: `SaveNewCatchUseCase`, `SaveUserMarkerNoteUseCase`, `GetUserCatchesUseCase`. |
| 42 | Unit tests for core ViewModels | `viewmodels/` | `NewCatchMasterViewModel`, `MapViewModel`, `UserViewModel`, `MainViewModel`, `LoginViewModel`. |
| 43 | Unit tests for entity mappers | `model/mappers/`, `model/datasource/local/mapper/` | Test round-trip fidelity, corrupt JSON handling, null fields. Especially `WeatherParsers.kt` moon phase logic. |
| 44 | Integration tests for Firebase repositories | `model/datasource/firebase/` | Use Firebase Emulator Suite. Test CRUD, error paths, listener cleanup. |
| 45 | Integration tests for sync logic | `SyncWorker.kt`, sync-aware repositories | Test offline queue, retry, conflict detection, partial failures. |
| 46 | Compose UI tests for critical flows | `ui/home/` | New catch wizard, login flow, map marker creation. |
| 47 | Set up CI pipeline (GitHub Actions) | Project root | Test + lint gates on PRs. Include Firebase Emulator for integration tests. |
| 48 | Set up Detekt/ktlint static analysis | Project root | Enforce code style, detect common issues. |

---

## Phase 4: Core UX Improvements

### 4.1 Input Validation

| # | Task | Details |
|---|------|---------|
| 49 | Validate fish weight range (0–500 kg) | `SaveNewCatchUseCase` + `NewCatchMasterViewModel` — show inline error |
| 50 | Validate fish amount range (0–9999) | Same location |
| 51 | Prevent future dates in catch date picker | New catch wizard — disable future dates in picker |
| 52 | Trim whitespace, enforce max length on place names | Place creation dialog — 50 char max |
| 53 | Validate latitude/longitude ranges | `MapViewModel` / `AddNewPlaceUseCase` — ±90° lat, ±180° lng |
| 54 | Validate note title/description not empty | `SaveUserMarkerNoteUseCase` — reject blank notes |

### 4.2 Missing UI States

| # | Task | Screen(s) |
|---|------|-----------|
| 55 | Empty state illustrations for catches/places lists | Notes tab |
| 56 | Error state with retry button on weather fetch | Weather tab |
| 57 | Loading skeleton/shimmer for catch and place lists | Notes tab |
| 58 | Success animation after saving a catch | New catch wizard |
| 59 | Confirmation dialogs before all delete actions | Catch, place, note detail screens |
| 60 | Pull-to-refresh on catches, places, weather | Notes & Weather tabs |
| 61 | Show loading indicator during login | `LoginScreen.kt:50-75` — `BaseViewState.Loading` currently shows nothing |
| 62 | Fix duplicate error display (toast + snackbar) | `LoginScreen.kt:69-73` — choose one notification method |

### 4.3 Photo Improvements

| # | Task | Details |
|---|------|---------|
| 63 | Pinch-to-zoom and rotation on photo viewer | `PhotoViews.kt:395` (existing TODO) |
| 64 | Photo upload progress indicator per photo | New catch wizard, catch edit |
| 65 | Camera capture (not just gallery pick) | New catch flow |
| 66 | Thumbnail generation for faster list loading | Photo grid components |

### 4.4 Accessibility

| # | Task | Details |
|---|------|---------|
| 67 | Add `contentDescription` to all Icon/Image composables | `Buttons.kt:37,98,149,189,215`, `FloatingActionButtons.kt:61,88`, `Counters.kt:39`, `DefaultViews.kt:110,158,205` — currently `null` or empty string |
| 68 | Add semantic grouping modifiers for screen readers | All screens |
| 69 | Ensure minimum 48dp touch targets on all interactive elements | All buttons/icons |
| 70 | Support dynamic text sizing | Verify layouts don't break at large font scales |

---

## Phase 5: Architecture & Code Quality

### 5.1 ViewModel Issues

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 71 | **Memory leak: infinite `collect` without lifecycle** | `UserViewModel.kt:46-49,52-59,62-66` | Uses `.collect {}` blocks that never complete. Replace with `collectLatest {}` or ensure proper cancellation via `viewModelScope`. |
| 72 | **Typo in property name** | `NewCatchMasterViewModel.kt:44` | `fishAndWeightSate` → `fishAndWeightState` |
| 73 | **Hardcoded error string** | `LoginViewModel.kt:80` | `"Google sign-in failed"` — use string resource. |
| 74 | **`println()` for logging in production code** | `StatisticsViewModel.kt:35,50` + multiple Firebase repos | Replace with proper logging abstraction. |
| 75 | **MutableState in ViewModel** | `MapViewModel.kt:99-104` | Using Compose `MutableState` directly instead of `StateFlow` — not lifecycle-aware. |
| 76 | **SnackbarManager called from ViewModel** | `UserCatchViewModel.kt:15` | Tight coupling to UI layer. Use error Flow/channel pattern. |
| 77 | **Hardcoded place name** | `MapViewModel.kt:198` | `"No name place"` — move to resources. |

### 5.2 Domain Layer Cleanup

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 78 | **Mutable domain entities** | `RawUserCatch.kt:3-24`, `RawMapMarker.kt`, `NewCatchWeather.kt` | All properties are `var` — should be `val` for immutability. Use `copy()` for modifications. |
| 79 | **Inconsistent repository return types** | Multiple repository interfaces | Mix of `suspend fun returning Flow`, `fun returning Flow`, `suspend fun returning StateFlow`. Standardize: `Flow<T>` for streams, `suspend fun` for one-shot, return `Result<T>` for fallible. |
| 80 | **Suspicious `Flow<Result<Nothing?>>` return** | `CatchesRepositoryUpdate.kt:15` | Should be `Flow<Result<Unit>>` — `Nothing?` suggests unknown intent. |
| 81 | **`getAllUserCatchesList` uses `collectionGroup`** | `FirebaseCatchesRepositoryImpl.kt:54-62` | Scans entire Firestore database. Should use user-specific subcollection path. Expensive reads and cost. |
| 82 | **O(n²) catch list management** | `GetUserCatchesUseCase.kt:9-28` | `removeAll` inside `forEach` — use a `Map<String, UserCatch>` for O(n) operations. |
| 83 | **Empty `Throwable()` in error emissions** | `GetNewCatchWeatherUseCase.kt:27-28` | `Result.failure(Throwable())` — no error message. Add descriptive message. |
| 84 | **`logoutCurrentUser()` returns `Flow<Boolean>`** | `UserRepository.kt:12` | Suspend function returning Flow is confusing. Should be `suspend fun: Result<Unit>`. |
| 85 | **Add `@Stable`/`@Immutable` to Compose state classes** | `domain/entity/` | Prevent unnecessary recompositions. |

### 5.3 DI & Initialization

| # | Issue | File(s) | Details |
|---|-------|---------|---------|
| 86 | **Missing BillingClient listener binding** | `di/KoinModules.kt:40-47` | `BillingClient.setListener(get())` — no `PurchasesUpdatedListener` provided in DI. Runtime crash when BillingClient instantiated. |
| 87 | **Duplicate `NotesPreferencesImpl` binding** | `di/KoinModules.kt:56-57` | Bound both as itself and as interface. First binding is unused — remove it. |
| 88 | **Deprecated `launchWhenStarted`** | `MainActivity.kt:82` | Deprecated since Lifecycle 2.4. Replace with `repeatOnLifecycle(Lifecycle.State.STARTED)`. |
| 89 | **Debug log left in theme** | `ui/theme/Theme.kt:94` | `Log.e("AppTheme", ...)` — remove or gate behind `DEBUG` flag. |
| 90 | **Static `lastCoordinates` StateFlow** | `LocationManagerImpl.kt:31` | `companion object { val lastCoordinates = MutableStateFlow(...) }` — potential memory leak and global mutable state. Make instance property. |

### 5.4 Tech Debt

| # | Task | Details |
|---|------|---------|
| 91 | Remove all dead/commented-out code | `SafeApiCall.kt`, `PhotoViews.kt`, `CatchScreenDialogs.kt`, `FirebaseCatchesRepositoryImpl.kt` |
| 92 | Fix moon phase logic error | `WeatherParsers.kt:94-95` — unreachable condition after `phase <= 0.55f`. Phase 0.98–1.0 always shows `moon_full` instead of transitioning back to new. |
| 93 | Implement structured logging (Timber or KMP alternative) | Replace all `Log.d`/`println` calls across codebase |
| 94 | Standardize spacing/padding to theme constants | All Compose screens — remove hardcoded `dp` values |
| 95 | Replace hardcoded gradient colors with theme tokens | `ui/home/profile/Profile.kt:71` — `Color(0xFFED2939)` etc. |
| 96 | Fix `InitColorScheme` transparent fallback | `ui/theme/Color.kt:273-278` — transparent colors are invalid theme fallback. Use real defaults. |
| 97 | Fix malformed comment | `PhotoViews.kt:127` — `}*/// FIXME:` |
| 98 | Add `@Preview` annotations to reusable composables | `Buttons.kt`, `FloatingActionButtons.kt`, card components — no previews exist |
| 99 | Remove `@SuppressLint("RestrictedApi")` | `Home.kt:28` — investigate and fix the underlying issue |
| 100 | Audit ProGuard rules | Add rules for Room entities, Ktor models, analytics events |
| 101 | Mixed Russian string in English strings.xml | `res/values/strings.xml:~82` — permission message not translated |

---

## Phase 6: Performance

| # | Task | Details |
|---|------|---------|
| 102 | Paginate catches list (Firestore `.limit()` + Paging 3) | `FirebaseCatchesRepositoryImpl` — also fix `getRefreshKey()` returning null in `CatchesPagingSource.kt:18` |
| 103 | Paginate places list similarly | `FirebaseMarkersRepositoryImpl` — same `getRefreshKey()` issue in `MarkersPagingSource.kt:17` |
| 104 | Implement cache-first read pattern | All repositories — read from Room first, background-sync with Firebase. Reduces cold start time and Firebase reads. |
| 105 | Audit `remember`/`mutableStateOf` for unnecessary recompositions | All Compose screens |
| 106 | Lazy-load photos in catch detail | `PhotoViews.kt` — load on scroll into view |
| 107 | Profile and optimize Firebase snapshot listener count | All repositories — consolidate where possible |
| 108 | Add Baseline Profiles for startup | `androidApp/` module |
| 109 | Replace Firestore `collectionGroup` queries | `FirebaseCatchesRepositoryImpl.kt:54-62` — use user-specific subcollection for O(1) lookup |

---

## Phase 7: CMP Migration Completion (Phase 13)

The largest remaining migration effort — ~85+ UI files from Android Compose to JetBrains Compose Multiplatform. See `CMP_MIGRATION.md` for detailed plan.

### 7.1 Prerequisites

| # | Task | Details |
|---|------|---------|
| 110 | Replace all `R.string` with CMP `composeResources/values/strings.xml` | ~50+ files reference `R.string` |
| 111 | Replace all `R.drawable` with CMP `composeResources/drawable/` | ~20+ files reference `R.drawable` |
| 112 | Replace Lottie with KMP alternative (`compottie` or expect/actual) | Splash screen, loading animations |
| 113 | Migrate Coil 2.x to Coil 3.x (KMP-first) | Image loading layer |
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
| 179 | **Missing `WRITE_EXTERNAL_STORAGE` for Android 11-12** | `AndroidManifest.xml` — photo saving may fail on API 30-31 without this permission. |
| 180 | **Fix navigation backstack on login success** | `MainActivity.kt:60-64` — `popUpTo(0)` may not clear properly on older Android versions. |
| 181 | **Fix deprecated `getFileFromUri()`** | `model/datasource/utils/PhotoUtils.kt:20` — replace with `fileFromContentUri()`. |
| 182 | **Migrate from Accompanist Pager** | Built-in `HorizontalPager` since Compose 1.5+ — Accompanist version is deprecated. |
| 183 | **Add Baseline Profiles** | Measure and optimize startup + critical user journeys. |
| 184 | **Firebase batch writes** | All multi-document operations (add marker + catches) should use `batch { }` for atomicity. |

---

## Priority Summary

| Priority | Phase | Focus | Items |
|----------|-------|-------|-------|
| **Critical** | 1 | Crashes, data corruption, security | #1–23 |
| **High** | 2–3 | Data robustness, testing foundation | #24–48 |
| **High** | 4 | Core UX: validation, states, accessibility | #49–70 |
| **Medium** | 5–6 | Architecture cleanup, performance | #71–109 |
| **Medium** | 7 | CMP migration Phase 13 | #110–134 |
| **Medium** | 8–9 | New features: catches, map | #135–153 |
| **Lower** | 10–11 | Weather tools, social features | #154–168 |
| **Lower** | 12–13 | Monetization, platform polish | #169–184 |

---

## Quick Reference: Files Most in Need of Attention

| File | Issues | Severity |
|------|--------|----------|
| `FirebaseCatchesRepositoryImpl.kt` | Silent exceptions, count drift, collectionGroup perf | Critical |
| `SyncWorker.kt` | False SYNCED status, duplicate ops, no timeout | Critical |
| `SyncAwareCatchesRepository.kt` | Race conditions, no dedup, no transactions | Critical |
| `ContentStateOld.kt` | `fold()` bug — data corruption | Critical |
| `SaveUserMarkerNoteUseCase.kt` | IndexOutOfBoundsException crash | Critical |
| `EntityMappers.kt` | JSON crash, missing markerColor mapping | High |
| `Converters.kt` | JSON crash on corrupt data | High |
| `UserDatastoreImpl.kt` | JSON crash on corrupt DataStore | High |
| `secrets.properties` | Production API keys exposed | Critical/Security |
| `KoinModules.kt` | Missing BillingClient listener, duplicate binding | High |
| `UserViewModel.kt` | Memory leak — infinite collect | High |
| `WeatherParsers.kt` | Moon phase logic error | Medium |
| `MainActivity.kt` | Deprecated API, lifecycle issues | Medium |
